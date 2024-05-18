import java.util.HashMap;

public class TugTable extends TugObject {
    public HashMap<Object, Object> map;
    boolean object = false;

    public TugTable() {
        map = new HashMap<>();
        super.type = "table";
    }

    public TugTable(HashMap<Object, Object> map) {
        this.map = map;
        super.type = "table";
    }

    public TugTable(Object... objs) {
        map = new HashMap<>();
        for (Object obj : objs) {
            map.put(Double.valueOf(map.size()), obj);
        }
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
        map.set("exit", new TugFunction(null, null, null, map).setbuiltin("exit"));
        map.set("tick", new TugFunction(null, null, null, map).setbuiltin("tick"));
        map.set("pairs", new TugFunction(null, null, null, map).setbuiltin("pairs"));
        map.set("import", new TugFunction(null, null, null, map).setbuiltin("import_"));
        return map;
    }

    public void set(Object key, Object value) {
        map.put(key, value);
    }

    public TugObject set(TugObject key, TugObject value) {
        Object val = Interpreter.convert(value);
        if (val == null) map.remove(Interpreter.convert(key));
        else map.put(Interpreter.convert(key), val);
        return new TugNone();
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public int size() {
        return map.size();
    }

    public TugTable copy() {
        HashMap<Object, Object> map = new HashMap<>();
        for (HashMap.Entry<Object, Object> entry : this.map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            TugCopy copiedKey = new TugCopy(key);
            TugCopy copiedValue = new TugCopy(value);

            key = copiedKey.start();
            value = copiedValue.start();
            map.put(key, value);
        }
        return new TugTable(map);
    }

    public TugTable combine(TugTable table) {
        TugTable clone = this.copy();

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

    public Object call(TugPosition pos, TugTable global, TugObject... values) {
        TugTable clone = this.copy();
        TugObject func = Interpreter.deconvert(clone.map.get("__init"));
        if (!(func instanceof TugFunction || func instanceof TugNone)) return new TugError(
            String.format("attempt to call %s", func.type), pos
        );
        for (Object val : clone.map.values()) {
            if (val instanceof TugFunction f) {
                f.setself(clone);
            }
        }
        if (func instanceof TugFunction) 
        func.call(pos, global, values);
        clone.object = true;
        return clone;
    }
}
