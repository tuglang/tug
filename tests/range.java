public class range {
    public static TugObject range(TugPosition pos, TugTable global, TugArgs args) {
        if (args.length == 0) return new TugError(
            "bad argument #1 for range (expected value)", pos
        );
        if (args.get(0) instanceof TugNumber num) {
            TugTable res = new TugTable();
            for (long i=0;i < num.value;i++) {
                res.set(Double.valueOf(res.size()), i);
            }
            return res;
        }
        return new TugError(
            "bad argument #1 for range (expected num)", pos
        );
    }
}