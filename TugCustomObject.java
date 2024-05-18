import java.util.HashMap;

public class TugCustomObject extends TugObject {
    public String type;
    TugPosition pos;
    HashMap<String, Object> map;

    public TugCustomObject(String type, TugPosition pos) {
        this.type = type;
        this.pos = pos;
        map = new HashMap<>();
    }

    public TugCustomObject(String type) {
        this.type = type;
        this.pos = null;
        map = new HashMap<>();
    }

    public void setValue(String str, Object val) {
        map.put(str, val);
    }

    public void removeValue(String str) {
        map.remove(str);
    }

    public Object getValue(String str) {
        return map.get(str);
    }
}
