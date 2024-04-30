import java.util.HashMap;

public class TugTable extends TugObject {
    public HashMap<Object, Object> map;

    public TugTable() {
        map = new HashMap<>();
        super.type = "table";
    }

    public TugTable(HashMap<Object, Object> map) {
        this.map = map;
        super.type = "table";
    }

    public static TugTable newDefault() {
        TugTable map = new TugTable();
        map.set("print", new TugFunction(null, null, null, map).setbuiltin("print"));
        map.set("input", new TugFunction(null, null, null, map).setbuiltin("input"));
        map.set("tostr", new TugFunction(null, null, null, map).setbuiltin("tostr"));
        map.set("tonum", new TugFunction(null, null, null, map).setbuiltin("tonum"));
        map.set("assert", new TugFunction(null, null, null, map).setbuiltin("assert_"));
        map.set("type", new TugFunction(null, null, null, map).setbuiltin("type"));
        map.set("len", new TugFunction(null, null, null, map).setbuiltin("len"));
        map.set("error", new TugFunction(null, null, null, map).setbuiltin("error"));
        map.set("exec", new TugFunction(null, null, null, map).setbuiltin("exec"));
        map.set("pairs", new TugFunction(null, null, null, map).setbuiltin("pairs"));
        map.set("import", new TugFunction(null, null, null, map).setbuiltin("import_"));
        return map;
    }

    public void set(Object key, Object value) {
        map.put(key, value);
    }

    public TugObject set(TugObject key, TugObject value) {
        map.put(Interpreter.convert(key), Interpreter.convert(value));
        return new TugNone();
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public int size() {
        return map.size();
    }

    public TugTable clone() {
        HashMap<Object, Object> map = new HashMap<>();
        for (HashMap.Entry<Object, Object> entry : this.map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            TugCopy copiedKey = new TugCopy(key);
            TugCopy copiedValue = new TugCopy(value);

            key = copiedKey.start();
            value = copiedValue.start();
        }
        return new TugTable(map);
    }

    public TugTable combine(TugTable table) {
        TugTable clone = this.clone();

        for (HashMap.Entry<Object, Object> entry : table.map.entrySet()) {
            clone.set(entry.getKey(), entry.getValue());
        }

        return clone;
    }

    public void acombine(TugTable table) {
        TugTable result = combine(table);

        for (HashMap.Entry<Object, Object> entry : result.map.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public Object index(TugObject value) {
        return Interpreter.deconvert(map.get(Interpreter.convert(value)));
    }
}
