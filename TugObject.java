import java.io.Serializable;

public class TugObject implements Serializable {
    public String type;
    public Position pos;

    public boolean isnum() {
        return type == "num";
    }

    public boolean isstr() {
        return type == "str";
    }

    public Object call(Position pos, TugTable global, TugObject... values) {
        return new TugError(
            String.format("attempt to call %s", type), pos
        );
    }

    public Object add(TugObject value) {
        return new TugError(
            "attempt to add " + type + " with " + value.type, pos
        );
    }

    public Object sub(TugObject value) {
        return new TugError(
            "attempt to sub " + type + " with " + value.type, pos
        );
    }

    public Object mul(TugObject value) {
        return new TugError(
            "attempt to mul " + type + " with " + value.type, pos
        );
    }

    public Object div(TugObject value) {
        return new TugError(
            "attempt to div " + type + " with " + value.type, pos
        );
    }

    public Object pow(TugObject value) {
        return new TugError(
            "attempt to pow " + type + " with " + value.type, pos
        );
    }

    public Object mod(TugObject value) {
        return new TugError(
            "attempt to mod " + type + " with " + value.type, pos
        );
    }

    public Object eq(TugObject value) {
        if (value.equals(this)) return new TugNumber(1);
        return new TugNumber(0);
    }

    public Object neq(TugObject value) {
        if (value.equals(this)) return new TugNumber(0);
        return new TugNumber(1);
    }

    public Object gt(TugObject value) {
        return new TugError(
            "attempt to compare " + type + " with " + value.type, pos
        );
    }

    public Object ge(TugObject value) {
        return new TugError(
            "attempt to compare " + type + " with " + value.type, pos
        );
    }

    public Object lt(TugObject value) {
        return new TugError(
            "attempt to compare " + type + " with " + value.type, pos
        );
    }

    public Object le(TugObject value) {
        return new TugError(
            "attempt to compare " + type + " with " + value.type, pos
        );
    }

    public Object and(TugObject value) {
        return value;
    }

    public Object or(TugObject value) {
        return this;
    }

    public Object not() {
        return new TugNumber(0);
    }

    public Object index(TugObject value) {
        return new TugError(
            "attempt to index " + type, pos
        );
    }

    public Object set(TugObject key, TugObject value) {
        return new TugError(
            "attempt to index " + type, pos
        );
    }
}
