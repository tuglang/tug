import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TugFunction extends TugObject {
    public Position pos;
    public String name;
    ArrayList<String> arg_names;
    ArrayList<Task> body;
    TugTable global = new TugTable();
    String builtin = null;
    SerializableMethod method = null;

    public static Object print(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) {
            System.out.println("");
            return new TugNone();
        }
        
        Object str = TugFunction.tostr(pos, global, values[0]);
        System.out.print(((TugString) str).value);
        
        if (values.length == 1) System.out.print("\n");
        else System.out.print(((TugString) TugFunction.tostr(pos, global, values[1])).value);

        return new TugNone();
    }

    @SuppressWarnings("resource")
    public static Object input(Position pos, TugTable global, TugObject... values) {
        Scanner scanner = new Scanner(System.in);
        if (values.length == 0) {
            return new TugString(scanner.nextLine());
        }
        
        Object str = TugFunction.tostr(pos, global, values[0]);
        System.out.print(((TugString) str).value);
        return new TugString(scanner.nextLine());
    }

    public static Object type(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'type'", pos
        );
        return new TugString(values[0].type);
    }

    public static Object assert_(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'assert'", pos
        );
        TugNumber num = new TugNumber(1);
        Object result = values[0].and(num);
        if (result.equals(num)) return new TugNone();
        if (values.length == 1) return new TugError("assertion failed", pos);
        return new TugError(((TugString) TugFunction.tostr(pos, global, values[1])).value, pos);
    }

    public static Object error(Position pos, TugTable global, TugObject... values) {
        return new TugError(((TugString) TugFunction.tostr(pos, global, values[0])).value, pos);
    }

    public static Object exec(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'exec'", pos
        );
        Tug.run(((TugString) TugFunction.tostr(pos, global, values[0])).value, global, "string");
        return new TugNone();
    }

    public static Object import_(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected value for argument #1 to 'import'", pos
        );

        String filename = ((TugString) TugFunction.tostr(pos, global, values[0])).value;
        
        File file = new File(filename);
        if (!file.exists()) {
            file = new File(filename + ".tug");
            if (!file.exists()) {
                file = new File(filename + ".tugb");
                if (!file.exists()) return new TugError(
                    "module not found", pos
                );
                else filename = filename + ".tugb";
            }
            else filename = filename + ".tug";
        }
        Path path = Paths.get(filename);
        String str;
        byte bytes[];
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            return new TugError(
                "unreadable file", pos
            );
        }
        if (bytes.length >= 3) {
            int magic = bytes[0] | bytes[1] | bytes[2];
            
            if (magic == 112) {
                byte objbytes[] = new byte[bytes.length-3];
                for (int idx = 3; idx < bytes.length; idx++) {
                    objbytes[idx-3] = bytes[idx];
                }
                Object obj = Tug.deserialize(objbytes);
                if (!(obj instanceof TugTable)) return new TugError(
                    "malformed tug bytes file",
                    pos
                );
                return (TugTable) obj;
            }
        }
        str = new String(bytes);
        Object res = Tug.run(str, TugTable.newDefault(), path.toAbsolutePath().toString());
        
        return res;
    }

    public static Object pairs(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected table for argument #1 to 'exec'", pos
        ); else if (values[0].type != "table") return new TugError(
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

    public static Object len(Position pos, TugTable global, TugObject... values) {
        if (values.length == 0) return new TugError(
            "expected str or table for argument #1 to 'len'", pos
        );
        if (values[0] instanceof TugString val) return new TugNumber(val.value.length());
        if (values[0] instanceof TugTable val) return new TugNumber(val.size());
        return new TugError(
            "expected str or table for argument #1 to 'len'", pos
        );
    }

    public static Object tostr(Position pos, TugTable global, TugObject... values) {
        Object obj = Interpreter.convert(values[0]);
        if (obj instanceof Double val) {
            if (String.valueOf(val) == "Infinity") return new TugString("inf");
            if (Math.floor(val) == val) return new TugString(String.valueOf(val.longValue()).toLowerCase());
            else return new TugString(String.valueOf(val).toLowerCase());
        } else if (obj instanceof TugFunction func)
        return new TugString(String.format("func: %s", Integer.toHexString(func.hashCode())));
        else if (obj instanceof TugTable table)
        return new TugString(String.format("table: %s", Integer.toHexString(table.hashCode())));
        else if (obj == null) return new TugString("none");
        else return new TugString(String.valueOf(obj));
    }

    public static Object tonum(Position pos, TugTable global, TugObject... values) {
        Object obj = Interpreter.convert(values[0]);
        if (!(obj instanceof String)) return new TugNone();
        try {
            return new TugNumber(Double.parseDouble((String) obj));
        } catch (NumberFormatException e) {
            return new TugNone();
        }
    }

    public TugFunction(Position pos, String name, ArrayList<String> arg_names, ArrayList<Task> body, TugTable global) {
        this.pos = pos;
        this.name = name;
        this.arg_names = arg_names;
        this.body = body;
        this.global = global.clone();
        super.type = "func";
    }

    public TugFunction(String name, ArrayList<String> arg_names, ArrayList<Task> body, TugTable global) {
        this.name = name;
        this.arg_names = arg_names;
        this.body = body;
        this.global = global.clone();
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

    @SuppressWarnings("unchecked")
    public TugFunction clone() {
        if (builtin != null || method != null) return this;
        return new TugFunction(
            new String(name),
            (ArrayList<String>) arg_names.clone(),
            (ArrayList<Task>) body.clone(),
            global.clone()
        );
    }

    public Object call(Position pos, TugTable global, TugObject... values) {
        if (builtin != null) {
            Class<?> tugfunc = this.getClass();
            try {
                Method method = tugfunc.getDeclaredMethod(builtin, new Class<?>[]{Position.class, TugTable.class, TugObject[].class});
                return method.invoke(null, pos, global, values);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // Pass
            }
        }
        if (method != null) {
            try {
                return method.toMethod().invoke(null, pos, global, values);
            } catch (IllegalAccessException e) {
                return new TugError(
                    "IllegalAccessException occurred", pos
                );
            } catch (IllegalArgumentException e) {
                return new TugError(
                    "IllegalArgumentException occurred", pos
                );
            } catch (InvocationTargetException e) {
                return new TugError(
                    "InvocationTargetException occurred", pos
                );
            } catch (NoSuchMethodException e) {
                return new TugError(
                    "NoSuchMethodException occurred", pos
                );
            } catch (SecurityException e) {
                return new TugError(
                    "SecurityException occurred", pos
                );
            }
        }
        global = this.global.combine(global);
        for (int idx = 0; idx < values.length; idx++) {
            if (idx >= arg_names.size()) break;
            global.set(arg_names.get(idx), Interpreter.convert(values[idx]));
        }
        for (int idx = values.length; idx < arg_names.size(); idx++) {
            global.set(arg_names.get(idx), null);
        }
        Interpreter interpreter = new Interpreter(body, global);
        Object ret = interpreter.start();
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
