public class TugString extends TugObject {
    public Position pos;
    public String value;

    public TugString(Position pos, String value) {
        this.pos = pos;
        this.value = value;
        super.type = "str";
    }

    public TugString(String value) {
        this.pos = null;
        this.value = value;
        super.type = "str";
    }

    public Object add(TugObject value) {
        if (value instanceof TugString string) {
            return new TugString(pos, this.value + string.value);
        }
        return new TugError(
            "attempt to add " + super.type + " with " + value.type, super.pos
        );
    }

    public Object sub(TugObject value) {
        if (value instanceof TugString string) {
            return new TugString(pos, this.value.replaceAll(string.value, ""));
        } else if (value instanceof TugNumber number) {
            return new TugString(pos, this.value.substring(0, this.value.length() - Double.valueOf(number.value).intValue()));
        }
        return new TugError(
            "attempt to sub " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mul(TugObject value) {
        if (value instanceof TugNumber number) {
            StringBuilder builder = new StringBuilder();
            for (double i = 0; i < number.value; i++) builder.append(this.value);
            return new TugString(pos, builder.toString());
        }
        return new TugError(
            "attempt to mul " + super.type + " with " + value.type, super.pos
        );
    }

    public Object eq(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.equals(string.value) ? 1 : 0);
        }
        return new TugNumber(0);
    }

    public Object neq(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.equals(string.value) ? 0 : 1);
        }
        return new TugNumber(1);
    }

    public Object gt(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.compareTo(string.value) == 1 ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object ge(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.compareTo(string.value) >= 0 ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object lt(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.compareTo(string.value) == -1 ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object le(TugObject value) {
        if (value instanceof TugString string) {
            return new TugNumber(pos, this.value.compareTo(string.value) <= 0 ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object and(TugObject value) {
        if (value instanceof TugString string) {
            return new TugString(pos, this.value == "" ? this.value : string.value);
        } else if (value instanceof TugNumber number) {
            if (this.value == "") {
                return new TugString(pos, this.value);
            }
            return new TugNumber(pos, number.value);
        } else if (value instanceof TugNone) {
            if (this.value == "") {
                return new TugString(pos, this.value);
            }
            return new TugNone(pos);
        }
        return new TugNumber(0);
    }

    public Object or(TugObject value) {
        if (value instanceof TugString string) {
            return new TugString(pos, this.value != "" ? this.value : string.value);
        } else if (value instanceof TugNumber number) {
            if (this.value != "") {
                return new TugString(pos, this.value);
            }
            return new TugNumber(pos, number.value);
        } else if (value instanceof TugNone) {
            if (this.value != "") {
                return new TugString(pos, this.value);
            }
            return new TugNone(pos);
        }
        return new TugNumber(0);
    }

    public Object not() {
        return new TugNumber(this.value == "" ? 1 : 0);
    }

    public Object index(TugObject value) {
        if (value instanceof TugNumber num) {
            if (!num.isint()) return new TugError(
                "attempt to index " + super.type + " with integer", super.pos
            );

            return new TugString(String.valueOf(this.value.charAt(num.value.intValue())));
        }
        return new TugError(
            "attempt to index " + super.type + " with " + value.type, super.pos
        );
    }
}
