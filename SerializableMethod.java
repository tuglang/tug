import java.io.Serializable;
import java.lang.reflect.Method;

public class SerializableMethod implements Serializable {
    String name;
    Class<?> parameter_types[];
    Class<?> main_class;

    public SerializableMethod(Method method, Class<?> cls) {
        name = method.getName();
        parameter_types = method.getParameterTypes();
        main_class = cls;
    }

    public Method toMethod() throws NoSuchMethodException, SecurityException {
        return main_class.getDeclaredMethod(name, parameter_types);
    }
}
