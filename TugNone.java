public class TugNone extends TugObject {
    public TugPosition pos;

    public TugNone(TugPosition pos) {
        this.pos = pos;
        super.type = "none";
    }

    public TugNone() {
        super.type = "none";
    }

    public Object add(TugObject value) {
        return new TugError(
            "attempt to add " + super.type + " with " + value.type, super.pos
        );
    }

    public Object sub(TugObject value) {
        return new TugError(
            "attempt to sub " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mul(TugObject value) {
        return new TugError(
            "attempt to mul " + super.type + " with " + value.type, super.pos
        );
    }

    public Object div(TugObject value) {
        return new TugError(
            "attempt to div " + super.type + " with " + value.type, super.pos
        );
    }

    public Object pow(TugObject value) {
        return new TugError(
            "attempt to pow " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mod(TugObject value) {
        return new TugError(
            "attempt to mod " + super.type + " with " + value.type, super.pos
        );
    }

    public Object eq(TugObject value) {
        return new TugNumber(value instanceof TugNone ? 1 : 0);
    }

    public Object neq(TugObject value) {
        return new TugNumber(value instanceof TugNone ? 0 : 1);
    }

    public Object gt(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object ge(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object lt(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object le(TugObject value) {
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object and(TugObject value) {
        return new TugNone(pos);
    }

    public Object or(TugObject value) {
        return value;
    }

    public Object not() {
        return new TugNumber(1);
    }
}
