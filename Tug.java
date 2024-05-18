import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Tug {
    public static String TUG_HOME;

    @SuppressWarnings("unchecked")
    public static Object run(String text, TugTable global, String fn) {
        Lexer lexer = new Lexer(text, fn);
        Object tokens = lexer.start();
        if (tokens instanceof TugError err) return err;

        Parser parser = new Parser((ArrayList<Token>) tokens);
        Object tasks = parser.start();
        if (tasks instanceof TugError err) return err;

        Interpreter interpreter = new Interpreter((ArrayList<Task>) tasks, global);
        Object res = interpreter.start();
        if (res instanceof TugError err) return err;

        return res;
    }

    @SuppressWarnings({"resource"})
    public static void shell() {
        System.out.println("Tug v0.1.0  Copyright (C) 2024 Morlus");
        Scanner scanner = new Scanner(System.in);
        TugTable global = TugTable.newDefault();
        while (true) {
            System.out.print("> ");
            String line;
            try {
                line = scanner.nextLine();
            } catch (NoSuchElementException e) {
                return;
            }
            Object res = Tug.run(line, global, "stdin");
            if (res instanceof TugError err) {
                System.out.println(err.as_string());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static void jcompile(String filename) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("java compiler is not available, make sure you are using JDK.");
            System.exit(1);
        }

        File file = new File(filename);
        Path path = Paths.get(filename);
        path = path.toAbsolutePath();
        Path abspath = path.toAbsolutePath();
        Path parentdir = abspath.getParent();
        System.setProperty("user.dir", parentdir.toString());
        String source;
        try {
            source = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            System.out.println("no such file");
            System.exit(1);
            return;
        }
        try {
            Files.write(file.toPath(), source.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("unable to create new file");
            System.exit(1);
            return;
        }

        if (compiler.run(null, null, null, file.getPath()) != 0) {
            System.out.println("compile error");
            System.exit(1);
            return;
        }

        String name = path.getFileName().toString();
        name = name.substring(0, name.lastIndexOf("."));

        URLClassLoader classLoader;
        try {
            classLoader = URLClassLoader.newInstance(new URL[] { parentdir.toUri().toURL() });
        } catch (MalformedURLException e) {
            System.out.println("malform url");
            System.exit(1);
            return;
        }
        Class<?> cls;
        try {
            cls = classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException occurred");
            System.exit(1);
            return;
        }
        
        ArrayList<Object> res = new ArrayList<>();
        initialize(cls, res);
        ArrayList<Method> methods = (ArrayList<Method>) res.get(0);
        ArrayList<Field> fields = (ArrayList<Field>) res.get(1);

        TugTable result = new TugTable();

        int total = 0;
        for (Method method : methods) {
            result.set(
                method.getName(),
                new TugFunction(method.getName(), null, null, new TugTable()).setmethod(
                    new SerializableMethod(method, cls)
                )
            );
            System.out.println("loaded method '" + method.getName() + "'");
            total++;
        }
        String totalM = "total methods: " + total;
        
        total = 0;
        for (Field field : fields) {
            try {
                result.set(field.getName(), Interpreter.convert((TugObject) field.get(null)));
            } catch (IllegalArgumentException e) {
                System.out.println("IllegalArgumentException occurred");
                System.exit(1);
                return;
            } catch (IllegalAccessException e) {
                System.out.println("IllegalAccessException occurred");
                System.exit(1);
                return;
            }
            System.out.println("loaded field '" + field.getName() + "'");
            total++;
        }
        System.out.println(totalM);
        System.out.println("total fields: " + total);

        Path result_path = Paths.get(parentdir.toString(), name + ".tugb");
        System.out.println("writing " + result_path.toString() + "...");

        byte bytes[] = Tug.serialize(result);
        byte r[] = new byte[bytes.length+4];
        r[0] = (byte) (0x74 << 8);
        r[1] = (byte) (0x75 << 8);
        r[2] = (byte) (0x67 << 4);
        r[3] = (byte) Interpreter.compile_version;
        for (int idx = 0; idx < bytes.length; idx++) {
            r[idx + 4] = bytes[idx];
        }

        if (!Interpreter.no_base64) r = Base64.getEncoder().encode(r);

        try {
            Files.write(result_path, r);
        } catch (IOException e) {
            System.out.println("unable to write data to file");
            System.exit(1);
            return;
        }

        System.out.println("\neverything is done!");
    }

    static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            System.out.println("serialization failed");
            System.exit(1);
            return null;
        }
    }

    static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis) {
            @Override
            protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                Path p = Paths.get(System.getProperty("user.dir"));
                URLClassLoader cl = new URLClassLoader(new URL[] {p.toUri().toURL()});
                Class<?> res;
                try {
                    res = cl.loadClass(desc.getName());
                } catch (ClassNotFoundException e) {
                    cl.close();
                    ClassLoader cls = Thread.currentThread().getContextClassLoader();
                    if (cls == null) return super.resolveClass(desc);
                    return cls.loadClass(desc.getName());
                }
                cl.close();
                return res;
            }
        }) {
            return in.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    static void initialize(Class<?> cls, ArrayList<Object> res) {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : cls.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) continue;
            if (!(method.getReturnType().equals(Object.class) || method.getReturnType().equals(TugObject.class))) continue;
            if (!Arrays.equals(method.getParameterTypes(), new Class<?>[]{TugPosition.class, TugTable.class, TugArgs.class})) continue;

            methods.add(method);
        }
        res.add(methods);

        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            if (
                !field.getType().equals(TugFunction.class) &&
                !field.getType().equals(TugTable.class) &&
                !field.getType().equals(TugString.class) &&
                !field.getType().equals(TugNumber.class) &&
                !field.getType().equals(TugNone.class) &&
                !field.getType().equals(TugError.class) &&
                !field.getType().equals(TugObject.class)
            ) continue;
            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) continue;

            fields.add(field);
        }
        res.add(fields);
    }

    public static void main(String[] args) {
        Interpreter.start_time = System.nanoTime() / 1000000000d;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--skip-eviron")) {
                Interpreter.skip_eviron = true;
            } else if (arg.equals("--java-stacktrace")) {
                Interpreter.java_stacktrace = true;
            } else if (arg.equals("--no-base64")) {
                Interpreter.no_base64 = true;
            }
        }
        if (System.getenv("TUG_HOME") == null && !Interpreter.skip_eviron) {
            System.out.println("evironment variable 'TUG_HOME' isn't set");
            System.exit(1);
            return;
        }
        if (args.length >= 1) {
            if (args[0].equals("run")) {
                if (args.length < 2) {
                    System.out.println("missing arguments");
                } else if (args.length > 2) {
                }
                Path path = Paths.get(args[args.length-1]);
                String str;
                try {
                    str = new String(Files.readAllBytes(path));
                } catch (IOException e) {
                    System.out.println("no such file or directory");
                    System.exit(1);
                    return;
                }
                Path abspath = path.toAbsolutePath();
                Path parentdir = abspath.getParent();
                System.setProperty("user.dir", parentdir.toString());
                Object res = Tug.run(str, TugTable.newDefault(), abspath.toString());
                if (res instanceof TugError err) {
                    System.out.println(err.as_string());
                    System.exit(1);
                    return;
                }
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                ArrayList<Thread> aliveThreads = new ArrayList<>();
                for (Thread t : threadSet) {
                    if (t.getName().startsWith("TugThread-")) {
                        if (t.isAlive()) {
                            aliveThreads.add(t);
                        }
                    }
                }
                while (true) {
                    boolean alives = false;
                    for (Thread t : aliveThreads) {
                        if (t.isAlive()) alives = true;
                    }
                    if (!alives) break;
                }
            } else if (args[0].equals("jcompile")) {
                Tug.jcompile(args[1]);
            } else if (args[0].equals("version")) {
                System.out.println("Tug v0.1.0  Copyright (C) 2024 Morlus");
                System.out.println("Interpreter: v" + Interpreter.version);
                System.out.println("Compiler: v" + Interpreter.compile_version);
            } else if (args[0].equals("license")) {
                
            } else if (args[0].equals("dir")) {
                System.out.print("home: ");
                System.out.println(System.getenv("TUG_HOME") == null ? "none" : System.getenv("TUG_HOME"));
                System.out.print("current: ");
                System.out.println(new File("").getAbsolutePath());
            } else if (args[0].equals("help")) {
                System.out.println("Commands:");
                System.out.println("  run - Run a tug program from specified file");
                System.out.println("  jcompile - Compile java code into tug module (.tugb file)");
                System.out.println("  version - Compile java code into tug module (.tugb file)");
            } else if (args[0].startsWith("-") || args[0].startsWith("--")) {
                Tug.shell();
            } else {
                System.out.println("no such command");
                System.exit(1);
            }
            System.exit(0);
        }
        Tug.shell();
    }
}