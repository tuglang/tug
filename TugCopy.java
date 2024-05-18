public class TugCopy {
    public Object obj;

    public TugCopy(Object obj) {
        this.obj = obj;
    }

    public Object start() {
        if (obj instanceof Double val) return Double.valueOf(val);
        if (obj instanceof String val) return String.valueOf(val);
        if (obj instanceof TugTable val) return val.copy();
        if (obj instanceof TugFunction val) return val.copy();
        return null;
    }
}
