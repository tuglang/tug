import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SerializableMethod implements Serializable {
    String name;
    ArrayList<Class<?>> parameter_types;
    Class<?> main_class;

    public SerializableMethod(Method method, Class<?> cls) {
        name = method.getName();
        parameter_types = new ArrayList<>(List.of(method.getParameterTypes()));
        main_class = cls;
    }

    public Method toMethod() throws NoSuchMethodException, SecurityException {
        Class<?> res[] = new Class<?>[parameter_types.size()];
        for (int i = 0; i < parameter_types.size(); i++) res[i] = parameter_types.get(i);
        return main_class.getDeclaredMethod(name, res);
    }
}
