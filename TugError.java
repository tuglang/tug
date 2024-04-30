import java.util.ArrayList;

public class TugError extends TugObject {
    public String msg;
    public Position pos;
    public ArrayList<Position> poses;

    public TugError(String msg, Position pos) {
        this.msg = msg;
        this.pos = pos;
        this.poses = new ArrayList<>();
    }

    public String as_string() {
        StringBuilder builder = new StringBuilder();
        if (poses.size() > 0) builder.append("stacktrace:\n");
        for (Position pos : poses) {
            builder.append("  ");
            builder.append(pos.fn);
            builder.append(":");
            builder.append(pos.line);
            builder.append(":");
            builder.append(pos.col + 1);
            builder.append("\n");
        }
        builder.append(this.pos.fn);
        builder.append(":");
        builder.append(this.pos.line);
        builder.append(":");
        builder.append(this.pos.col + 1);
        builder.append(": ");
        builder.append(msg);
        return builder.toString();
    }

    public void add(Position pos) {
        poses.add(pos);
    }
}
