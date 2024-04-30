import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Tug {
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
        System.out.println("Tug v0.1.0  Copyright (C) 2024 Tylon");
        Scanner scanner = new Scanner(System.in);
        TugTable global = TugTable.newDefault();
        while (true) {
            System.out.print("> ");
            Object res = Tug.run(scanner.nextLine(), global, "stdin");
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
        if (!file.exists()) {
            System.out.println("no such java file");
        }
        Path path = Paths.get(filename);
        path = path.toAbsolutePath();
        String source;
        try {
            source = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            System.out.println("no such file or directory");
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

        compiler.run(null, null, null, file.getPath());

        String name = path.getFileName().toString();
        name = name.substring(0, name.lastIndexOf("."));

        URLClassLoader classLoader;
        try {
            classLoader = URLClassLoader.newInstance(new URL[] { file.getParentFile().toURI().toURL() });
        } catch (MalformedURLException e) {
            System.out.println("malform url");
            System.exit(1);
            return;
        }
        Class<?> cls;
        try {
            cls = Class.forName(name, true, classLoader);
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

        for (Method method : methods) {
            result.set(
                method.getName(),
                new TugFunction(method.getName(), null, null, result).setmethod(
                    new SerializableMethod(method, cls)
                )
            );
        }
        
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
        }

        Path result_path = Paths.get(name + ".tugb");

        byte bytes[] = Tug.serialize(result);
        byte r[] = new byte[bytes.length+3];
        r[0] = (byte) (0x74 << 8);
        r[1] = (byte) (0x75 << 8);
        r[2] = (byte) (0x67 << 4);
        for (int idx = 0; idx < bytes.length; idx++) {
            r[idx + 3] = bytes[idx];
        }

        try {
            Files.write(result_path, r);
        } catch (IOException e) {
            System.out.println("unable to write data to file");
            System.exit(1);
            return;
        }
    }

    static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.out.println("serialization failed");
            System.exit(1);
            return null;
        }
    }

    static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            System.out.println("deserialization failed");
            System.exit(1);
            return null;
        }
    }

    static void initialize(Class<?> cls, ArrayList<Object> res) {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : cls.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) continue;
            if (!(method.getReturnType().equals(Object.class) || method.getReturnType().equals(TugObject.class))) continue;
            if (!Arrays.equals(method.getParameterTypes(), new Class<?>[]{Position.class, TugTable.class, TugObject[].class})) continue;

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
        if (args.length >= 1) {
            if (args[0].equals("run")) {
                if (args.length < 2) {
                    System.out.println("missing arguments");
                }
                Path path = Paths.get(args[1]);
                String str;
                try {
                    str = new String(Files.readAllBytes(path));
                } catch (IOException e) {
                    System.out.println("no such file or directory");
                    System.exit(1);
                    return;
                }
                Object res = Tug.run(str, TugTable.newDefault(), path.toAbsolutePath().toString());
                if (res instanceof TugError err) {
                    System.out.println(err.as_string());
                    System.exit(1);
                    return;
                }
            } else if (args[0].equals("jcompile")) {
                Tug.jcompile(args[1]);
            } else {
                System.out.println("no such command");
                System.exit(1);
            }
            System.exit(0);
        }
        Tug.shell();
    }
}