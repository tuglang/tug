import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

public class TugFunction extends TugObject {
    public TugPosition pos;
    public String name;
    boolean args = false;
    ArrayList<String> arg_names;
    ArrayList<Task> body;
    TugTable global = new TugTable();
    String builtin = null;
    SerializableMethod method = null;
    TugTable self = null;

    public static Object print(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) {
            System.out.println("");
            return new TugNone();
        }
        
        Object str = TugFunction.tostr(pos, global, values[0]);
        String string = ((TugString) str).value;
        
        if (values.length == 1) string += "\n";
        else string += ((TugString) TugFunction.tostr(pos, global, values[1])).value;

        System.out.print(string);

        return new TugNone();
    }

    @SuppressWarnings("resource")
    public static Object input(TugPosition pos, TugTable global, TugObject... values) {
        Scanner scanner = new Scanner(System.in);
        if (values.length == 0) {
            return new TugString(scanner.nextLine());
        }
        
        Object str = TugFunction.tostr(pos, global, values[0]);
        System.out.print(((TugString) str).value);
        return new TugString(scanner.nextLine());
    }

    public static Object type(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'type'", pos
        );
        return new TugString(values[0].type);
    }

    public static Object assert_(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'assert'", pos
        );
        String msg = "assertion failed";
        if (values.length == 2) msg = ((TugString) TugFunction.tostr(pos, global, values[1])).value;
        TugNumber num = new TugNumber(1);
        Object result = values[0].and(num);
        if (result.equals(num)) return new TugNone();
        if (values.length == 1) return new TugError(msg, pos);
        return new TugError(((TugString) TugFunction.tostr(pos, global, values[1])).value, pos);
    }

    public static Object error(TugPosition pos, TugTable global, TugObject... values) {
        return new TugError(((TugString) TugFunction.tostr(pos, global, values[0])).value, pos);
    }

    public static Object exec(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'exec'", pos
        );
        Tug.run(((TugString) TugFunction.tostr(pos, global, values[0])).value, global, "string");
        return new TugNone();
    }

    public static Object exit(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) {
            System.exit(0);
            return new TugNone();
        }
        if (((TugNumber) values[0].not()).value.equals(0d)) System.exit(1);
        else System.exit(0);
        return new TugNone();
    }

    public static Object tick(TugPosition pos, TugTable global, TugObject... values) {
        return new TugNumber(System.nanoTime() / 1000000000d - Interpreter.start_time);
    }

    public static String checkPath(String name) {
        if (fileExists(name)) {
            return name;
        }

        if (fileExists(name + ".tug")) {
            return name + ".tug";
        }

        if (fileExists(name + ".tugb")) {
            return name + ".tugb";
        }

        String tugHome = System.getenv("TUG_HOME");

        if (tugHome != null) {
            String paths[] = tugHome.split(";");
            for (String path : paths) {
                if (fileExists(path + "/library/" + name)) {
                    return path + "/library/" + name;
                }

                if (fileExists(tugHome + "/modules/" + name)) {
                    return path + "/modules/" + name;
                }

                if (fileExists(path + "/library/" + name + ".tug")) {
                    return path + "/library/" + name + ".tug";
                }

                if (fileExists(path + "/modules/" + name + ".tug")) {
                    return path + "/modules/" + name + ".tug";
                }

                if (fileExists(path + "/library/" + name + ".tugb")) {
                    return path + "/library/" + name + ".tugb";
                }

                if (fileExists(path + "/modules/" + name + ".tugb")) {
                    return path + "/modules/" + name + ".tugb";
                }
            }
        }

        return null;
    }

    private static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    static byte[] decode(String val) {
        try {
            return Base64.getDecoder().decode(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Object import_(TugPosition pos, TugTable global, TugObject... values) {
        if (Interpreter.skip_eviron) return new TugError(
            "'skip-eviron' option is set to true, unable to import modules", pos
        );
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'import'", pos
        );

        String filename = ((TugString) TugFunction.tostr(pos, global, values[0])).value;
        
        filename = Paths.get(filename).toString();
        Object res = checkPath(filename);
        if (res == null) return new TugError(
            "no such module", pos
        );
        filename = (String) res;
        Path path = Paths.get(filename).toAbsolutePath();
        String str;
        byte bytes[];
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            return new TugError(
                "unreadable file", pos
            );
        }
        byte decoded[];
        try {
            decoded = decode(new String(bytes, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            return new TugError("decoding failed", pos);
        }
        if (decoded == null);
        else bytes = decoded;

        if (bytes.length >= 3) {
            String magic = new String(new byte[]{bytes[0], bytes[1], bytes[2]});

            if ((int) bytes[3] == Interpreter.compile_version) {
                if (magic.equals("TUG")) {
                    byte objbytes[] = new byte[bytes.length-4];
                    for (int idx = 4; idx < bytes.length; idx++) {
                        objbytes[idx-4] = bytes[idx];
                    }
                    String og = System.getProperty("user.dir");
                    System.setProperty("user.dir", path.getParent().toString());
                    Object obj = Tug.deserialize(objbytes);
                    System.setProperty("user.dir", og);
                    if (obj == null) return new TugError(
                        "deserialization failed", pos
                    );
                    if (!(obj instanceof TugTable)) return new TugError(
                        "malformed tug bytes file",
                        pos
                    );
                    return (TugTable) obj;
                }
            }
        }
        str = new String(bytes);
        Object r = Tug.run(str, TugTable.newDefault(), path.toAbsolutePath().toString());
        
        return r;
    }

    public static Object pairs(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected table for argument #1 to 'exec'", pos
        ); else if (!(values[0] instanceof TugTable)) return new TugError(
            "expected table for argument #1 to 'pairs'", pos
        );
        TugTable table = (TugTable) values[0];
        TugTable res = new TugTable();
        for (HashMap.Entry<Object, Object> entry : table.map.entrySet()) {
            TugTable item = new TugTable();
            item.set("key", entry.getKey());
            item.set("value", entry.getValue());
            res.set(Double.valueOf(res.size()), item);
        }
        return res;
    }

    public static Object len(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected str or table for argument #1 to 'len'", pos
        );
        if (values[0] instanceof TugString val) return new TugNumber(val.value.length());
        if (values[0] instanceof TugTable val) {
            if (val.object) {
                Object v = val.get("__len");
                if (v == null) return new TugNumber(val.size());
                TugObject func = Interpreter.deconvert(v);
                return func.call(pos, global, new TugNone());
            } else return new TugNumber(val.size());
        }
        return new TugError(
            "expected str or table for argument #1 to 'len'", pos
        );
    }

    public static Object tostr(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugNone();
        Object obj = Interpreter.convert(values[0]);
        if (obj instanceof Double val) {
            if (String.valueOf(val) == "Infinity") return new TugString("inf");
            if (Math.floor(val) == val) return new TugString(String.valueOf(val.longValue()).toLowerCase());
            else return new TugString(String.valueOf(val).toLowerCase());
        } else if (obj instanceof TugFunction func)
        return new TugString(String.format("func: %s", Integer.toHexString(func.hashCode())));
        else if (obj instanceof TugTable table) {
            if (table.object) {
                Object v = table.get("__type");
                if (v == null) {
                    return new TugString(String.format("table: %s", Integer.toHexString(table.hashCode())));
                }
                TugObject val = Interpreter.deconvert(v);
                return new TugString(String.format(
                    "%s: %s",
                    ((TugString) tostr(pos, global, val)).value,
                    Integer.toHexString(table.hashCode())
                ));
            } else
            return new TugString(String.format("table: %s", Integer.toHexString(table.hashCode())));
        }
        else if (obj == null) return new TugString("none");
        else if (obj instanceof TugNone) return new TugString("none");
        else if (obj instanceof TugCustomObject cobj)
        return new TugString(String.format("%s: %s", cobj.type, Integer.toHexString(cobj.hashCode())));
        else if (obj instanceof String s) return new TugString(s);
        else return new TugString(String.valueOf(obj));
    }

    public static Object tonum(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugNone();
        Object obj = Interpreter.convert(values[0]);
        if (!(obj instanceof String)) return new TugNone();
        try {
            return new TugNumber(Double.parseDouble((String) obj));
        } catch (NumberFormatException e) {
            return new TugNone();
        }
    }

    public static Object _call(TugPosition pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected func for argument #1 to 'call'", pos
        );
        if (values[0] instanceof TugFunction func) {
            TugObject args[] = new TugObject[values.length-1];
            for (int i = 1; i < values.length; i++) {
                args[i-1] = values[i];
            }
            Object ret = func.call(pos, global, args);
            if (ret instanceof TugError err) {
                return new TugTable(0d, err.msg);
            }
            return new TugTable(1d, Interpreter.convert((TugObject) ret));
        }
        return new TugError(
            "expected func for argument #1 to 'call'", pos
        );
    }

    public TugFunction(TugPosition pos, String name, ArrayList<String> arg_names, ArrayList<Task> body, TugTable global) {
        this.pos = pos;
        this.name = name;
        this.arg_names = arg_names;
        this.body = body;
        this.global = global.copy();
        super.type = "func";
    }

    public TugFunction(String name, ArrayList<String> arg_names, ArrayList<Task> body, TugTable global) {
        this.name = name;
        this.arg_names = arg_names;
        this.body = body;
        this.global = global.copy();
        super.type = "func";
    }

    public TugFunction(TugTable global, Class<?> main_class, Method method) {
        this.method = new SerializableMethod(method, main_class);
        this.global = global.copy();
    }

    public TugFunction(String name, ArrayList<String> arg_names, ArrayList<Task> body, TugTable global, boolean args) {
        this.name = name;
        this.arg_names = arg_names;
        this.body = body;
        this.args = args;
        this.global = global.copy();
        super.type = "func";
    }

    public TugFunction setbuiltin(String builtin) {
        this.builtin = builtin;
        return this;
    }

    public TugFunction setmethod(SerializableMethod method) {
        this.method = method;
        return this;
    }

    public TugFunction setself(TugTable self) {
        this.self = self;
        return this;
    }

    @SuppressWarnings("unchecked")
    public TugFunction copy() {
        if (builtin != null || method != null) return this;
        return new TugFunction(
            name,
            (ArrayList<String>) arg_names.clone(),
            (ArrayList<Task>) body.clone(),
            global.copy()
        );
    }

    public Object call(TugPosition pos, TugTable global, TugObject... values) {
        if (builtin != null) {
            Class<?> tugfunc = this.getClass();
            try {
                Method method = tugfunc.getDeclaredMethod(builtin, new Class<?>[]{TugPosition.class, TugTable.class, TugObject[].class});
                Object res = method.invoke(null, pos, global, values);
                if (res instanceof TugError err) {
                    err.add(pos);
                }
                return res;
            } catch (NoSuchMethodException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "NoSuchMethodException occurred", pos
                );
            } catch (IllegalAccessException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "IllegalAccessException occurred", pos
                );
            } catch (InvocationTargetException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "InvocationTargetException occurred", pos
                );
            }
        }
        if (method != null) {
            try {
                Object res = method.toMethod().invoke(null, pos, global, new TugArgs(values));
                if (res instanceof TugError err) {
                    err.add(pos);
                }
                return res;
            } catch (IllegalAccessException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "IllegalAccessException occurred", pos
                );
            } catch (IllegalArgumentException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "IllegalArgumentException occurred", pos
                );
            } catch (InvocationTargetException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "InvocationTargetException occurred", pos
                );
            } catch (NoSuchMethodException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "NoSuchMethodException occurred", pos
                );
            } catch (SecurityException e) {
                if (Interpreter.java_stacktrace) e.printStackTrace();
                return new TugError(
                    "SecurityException occurred", pos
                );
            } catch (OutOfMemoryError e) {
                return new TugError(
                    "out of memory", pos
                );
            } catch (StackOverflowError e) {
                return new TugError(
                    "stack overflow", pos
                );
            }
        }
        if (self != null) {
            TugObject cvalues[] = new TugObject[values.length + 1];
            cvalues[0] = self;
            for (int i = 0; i < values.length; i++) {
                cvalues[i+1] = values[i];
            }
            values = cvalues;
        }
        global = this.global.combine(global);
        if (args) {
            TugTable passed_args = new TugTable();
            for (int idx = 0; idx < values.length; idx++) {
                if (idx >= arg_names.size() - 1) {
                    passed_args.set(Double.valueOf(passed_args.size()), Interpreter.convert(values[idx]));
                } else {
                    global.set(arg_names.get(idx), Interpreter.convert(values[idx]));
                }
            }
            for (int idx = values.length ; idx < arg_names.size() - 1; idx++) {
                global.set(arg_names.get(idx), null);
            }
            global.set(arg_names.get(arg_names.size() - 1), passed_args);
        } else {
            for (int idx = 0; idx < values.length; idx++) {
                if (idx >= arg_names.size()) break;
                global.set(arg_names.get(idx), Interpreter.convert(values[idx]));
            }
            for (int idx = values.length; idx < arg_names.size(); idx++) {
                global.set(arg_names.get(idx), null);
            }
        }
        Interpreter interpreter = new Interpreter(body, global);
        Object ret;
        try {
            ret = interpreter.start();
        } catch (StackOverflowError e) {
            ret = new TugError(
                "stack overflow", pos
            );
        } catch (OutOfMemoryError e) {
            return new TugError(
                "out of memory", pos
            );
        }
        if (ret instanceof TugError err) {
            err.add(pos);
            return err;
        }
        return ret;
    }

    public Object add(TugObject value) {
        return new TugError(
            "attempt to add " + super.type + " with " + value.type, super.pos
        );
    }

    public Object sub(TugObject value) {
        return new TugError(
            "attempt to sub " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mul(TugObject value) {
        return new TugError(
            "attempt to mul " + super.type + " with " + value.type, super.pos
        );
    }

    public Object div(TugObject value) {
        return new TugError(
            "attempt to div " + super.type + " with " + value.type, super.pos
        );
    }

    public Object pow(TugObject value) {
        return new TugError(
            "attempt to pow " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mod(TugObject value) {
        return new TugError(
            "attempt to mod " + super.type + " with " + value.type, super.pos
        );
    }

    public Object eq(TugObject value) {
        return new TugNumber(value == this ? 1 : 0);
    }

    public Object neq(TugObject value) {
        return new TugNumber(value == this ? 0 : 1);
    }

    public Object gt(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object ge(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object lt(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object le(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }
}
