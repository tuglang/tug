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
        map.set("call", new TugFunction(null, null, null, map).setbuiltin("_call"));
        map.set("exit", new TugFunction(null, null, null, map).setbuiltin("exit"));
        map.set("tick", new TugFunction(null, null, null, map).setbuiltin("tick"));
        map.set("pairs", new TugFunction(null, null, null, map).setbuiltin("pairs"));
        map.set("import", new TugFunction(null, null, null, map).setbuiltin("import_"));
        return map;
    }

    public void set(Object key, Object value) {
        if (value == null) {
            map.remove(key);
            return;
        }
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

    public Object index(TugPosition pos, TugTable global, TugObject value) {
        return Interpreter.deconvert(map.get(Interpreter.convert(value)));
    }

    public Object call(TugPosition pos, TugTable global, TugObject... values) {
        if (object) {
            Object func = get("__call");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, values);
            }
            return new TugError(
                "attempt to call 'table'", pos
            );
        }
        TugTable clone = this.copy();
        Object func = clone.map.get("__init");
        if (!(func instanceof TugFunction || func == null)) return new TugError(
            "attempt to call 'table'", pos
        );
        for (Object val : clone.map.values()) {
            if (val instanceof TugFunction f) {
                f.setself(clone);
            }
        }
        if (func instanceof TugFunction f) 
        f.call(pos, global, values);
        clone.object = true;
        return clone;
    }

    public Object add(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__add");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to add " + super.type + " with " + value.type, pos
        );
    }

    public Object sub(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__sub");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to sub " + super.type + " with " + value.type, pos
        );
    }

    public Object mul(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__mul");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to mul " + super.type + " with " + value.type, pos
        );
    }

    public Object div(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__div");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to div " + super.type + " with " + value.type, pos
        );
    }

    public Object pow(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__pow");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to pow " + super.type + " with " + value.type, pos
        );
    }

    public Object mod(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__mod");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to mod " + super.type + " with " + value.type, pos
        );
    }

    public Object eq(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__eq");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        if (value.equals(this)) return new TugNumber(1);
        return new TugNumber(0);
    }

    public Object neq(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__neq");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        if (value.equals(this)) return new TugNumber(0);
        return new TugNumber(1);
    }

    public Object gt(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__gt");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, pos
        );
    }

    public Object ge(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__ge");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, pos
        );
    }

    public Object lt(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__lt");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, pos
        );
    }

    public Object le(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__le");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, pos
        );
    }

    public Object and(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__and");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return value;
    }

    public Object or(TugPosition pos, TugTable global, TugObject value) {
        if (object) {
            Object func = get("__or");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{value});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return this;
    }

    public Object not(TugPosition pos, TugTable global) {
        if (object) {
            Object func = get("__not");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugNumber(0);
    }

    public Object neg(TugPosition pos, TugTable global) {
        if (object) {
            Object func = get("__neg");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to unary " + super.type, pos
        );
    }

    public Object pos(TugPosition pos, TugTable global) {
        if (object) {
            Object func = get("__pos");
            if (func instanceof TugFunction f) {
                return f.call(pos, global, new TugObject[]{});
            }
            if (func != null) return new TugError(
                String.format("attempt to call '%s'", Interpreter.deconvert(func).type), pos
            );
        }
        return new TugError(
            "attempt to unary " + super.type, pos
        );
    }
}
