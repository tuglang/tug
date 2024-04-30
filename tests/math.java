public class math {
    public TugObject sin(Position pos, TugTable global, TugObject... args) {
        if (args.length == 0) return new TugError(
            "expected number for argument #1 to 'sin'", pos
        );
        if (args[0] instanceof TugNumber num) return new TugNumber(Math.sin(num.value));
        return new TugError(
            "expected number for argument #1 to 'sin'", pos
        );
    }
}