import java.util.ArrayList;
import java.util.List;

public class Task {
    public TaskType type;
    public ArrayList<Object> values;

    public Task(TaskType type, Object... values) {
        this.type = type;
        this.values = new ArrayList<>(List.of(values));
    }

    public boolean match(TaskType type) {
        return this.type == type;
    }
}
