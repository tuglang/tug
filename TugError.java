import java.util.ArrayList;

public class TugError extends TugObject {
    public String msg;
    public TugPosition pos;
    public ArrayList<TugPosition> poses;

    public TugError(String msg, TugPosition pos) {
        this.msg = msg;
        this.pos = pos;
        this.poses = new ArrayList<>();
    }

    public String as_string() {
        StringBuilder builder = new StringBuilder();
        if (poses.size() > 0) builder.append("stacktrace:\n");
        for (TugPosition pos : poses) {
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

    public void add(TugPosition pos) {
        poses.add(pos);
    }
}
