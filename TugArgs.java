import java.util.ArrayList;

public class TugArgs {
    ArrayList<TugObject> args;
    public int length;

    public TugArgs(TugObject... vals) {
        args = new ArrayList<>();
        for (TugObject val : vals) args.add(val);
        length = args.size();
    }

    public TugObject get(int idx) {
        try {
            return args.get(idx);
        } catch (IndexOutOfBoundsException e) {
            return new TugNone();
        }
    }

    public TugObject[] values() {
        Object arr[] = args.toArray();
        TugObject res[] = new TugObject[arr.length];
        for (int i = 0; i < arr.length; i++) res[i] = (TugObject) arr[i];
        return res;
    }
}
