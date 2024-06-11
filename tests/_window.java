import javax.swing.JFrame;

public class _window {
    public static TugObject _new(TugPosition pos, TugTable global, TugArgs args) {
        if (args.length == 0) {
            TugCustomObject window = new TugCustomObject("window");
            JFrame res = new JFrame();
            res.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setValue("frame", res);
            return window;
        } else if (args.get(0) instanceof TugString str) {
            TugCustomObject window = new TugCustomObject("window");
            JFrame res = new JFrame(str.value);
            res.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setValue("frame", res);
            return window;
        }
        return new TugError(
            "expected str for argument #1 to 'window'", pos
        );
    }

    public static TugObject visible(TugPosition pos, TugTable global, TugArgs args) {
        if (args.length == 0) return new TugError(
            "expected window for argument #1 to 'show'", pos
        );
        if (args.length == 1) return new TugError(
            "expected num for argument #2 to 'show'", pos
        );
        if (args.get(0) instanceof TugCustomObject cobj) {
            if (!cobj.type.equals("window")) return new TugError(
                "expected window for argument #1 to 'show'", pos
            );
            if (args.get(1) instanceof TugNumber num) {
                ((JFrame) cobj.getValue("frame")).setVisible(num.value == 1d ? true : false);
                return new TugNone();
            }
            return new TugError(
                "expected num for argument #2 to 'show'", pos
            );
        }
        return new TugError(
            "expected window for argument #1 to 'show'", pos
        );
    }

    public static TugObject resize(TugPosition pos, TugTable global, TugArgs args) {
        if (args.length == 0) return new TugError(
            "expected window for argument #1 to 'resize'", pos
        );
        if (args.length == 1) return new TugError(
            "expected num for argument #2 to 'resize'", pos
        );
        if (args.length == 2) return new TugError(
            "expected num for argument #3 to 'resize'", pos
        );
        if (args.get(0) instanceof TugCustomObject cobj) {
            if (!cobj.type.equals("window")) return new TugError(
                "expected window for argument #1 to 'resize'", pos
            );
            if (args.get(1) instanceof TugNumber width) {
                if (args.get(2) instanceof TugNumber height) {
                    JFrame frame = (JFrame) cobj.getValue("frame");
                    frame.setBounds(frame.getX(), frame.getY(), width.value.intValue(), height.value.intValue());
                    return new TugNone();
                }
                return new TugError(
                    "expected num for argument #3 to 'resize'", pos
                );
            }
            return new TugError(
                "expected num for argument #2 to 'resize'", pos
            );
        }
        return new TugError(
            "expected window for argument #1 to 'resize'", pos
        );
    }

    public static TugObject pos(TugPosition pos, TugTable global, TugArgs args) {
        if (args.length == 0) return new TugError(
            "expected window for argument #1 to 'pos'", pos
        );
        if (args.length == 1) return new TugError(
            "expected num for argument #2 to 'pos'", pos
        );
        if (args.length == 2) return new TugError(
            "expected num for argument #3 to 'pos'", pos
        );
        if (args.get(0) instanceof TugCustomObject cobj) {
            if (!cobj.type.equals("window")) return new TugError(
                "expected window for argument #1 to 'pos'", pos
            );
            if (args.get(1) instanceof TugNumber x) {
                if (args.get(2) instanceof TugNumber y) {
                    JFrame frame = (JFrame) cobj.getValue("frame");
                    frame.setBounds(x.value.intValue(), y.value.intValue(), frame.getWidth(), frame.getHeight());
                    return new TugNone();
                }
                return new TugError(
                    "expected num for argument #3 to 'pos'", pos
                );
            }
            return new TugError(
                "expected num for argument #2 to 'pos'", pos
            );
        }
        return new TugError(
            "expected window for argument #1 to 'pos'", pos
        );
    }
}
