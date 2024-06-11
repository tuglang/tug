public class TugNumber extends TugObject {
    public TugPosition pos;
    public Double value;

    public TugNumber(TugPosition pos, Number value) {
        this.pos = pos;
        this.value = value.doubleValue();
        super.type = "num";
    }

    public TugNumber(Number value) {
        this.pos = null;
        this.value = value.doubleValue();
        super.type = "num";
    }

    public TugNumber set_pos(TugPosition pos) {
        this.pos = pos;
        return this;
    }

    public boolean isint() {
        return value.equals(Math.floor(value));
    }

    public Object add(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value + number.value);
        }
        return new TugError(
            "attempt to add " + super.type + " with " + value.type, super.pos
        );
    }

    public Object sub(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value - number.value);
        }
        return new TugError(
            "attempt to sub " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mul(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value * number.value);
        }
        return new TugError(
            "attempt to mul " + super.type + " with " + value.type, super.pos
        );
    }

    public Object div(TugObject value) {
        if (value instanceof TugNumber number) {
            if (number.value == 0.0d) return new TugError(
                "zero division", super.pos
            );
            return new TugNumber(pos, this.value / number.value);
        }
        return new TugError(
            "attempt to div " + super.type + " with " + value.type, super.pos
        );
    }

    public Object pow(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, Math.pow(this.value, number.value));
        }
        return new TugError(
            "attempt to pow " + super.type + " with " + value.type, super.pos
        );
    }

    public Object mod(TugObject value) {
        if (value instanceof TugNumber number) {
            if (number.value == 0.0d) return new TugError(
                "zero modulus", super.pos
            );
            return new TugNumber(pos, this.value % number.value);
        }
        return new TugError(
            "attempt to mod " + super.type + " with " + value.type, super.pos
        );
    }

    public Object eq(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value.equals(number.value) ? 1 : 0);
        }
        return new TugNumber(0);
    }

    public Object neq(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value.equals(number.value) ? 0 : 1);
        }
        return new TugNumber(1);
    }

    public Object gt(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value > number.value ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object ge(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value >= number.value ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object lt(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value < number.value ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object le(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value <= number.value ? 1 : 0);
        }
        return new TugError(
            "attempt to compare " + super.type + " with " + value.type, super.pos
        );
    }

    public Object and(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value == 0 ? this.value : number.value);
        } else if (value instanceof TugString string) {
            if (this.value == 0) {
                return new TugNumber(pos, this.value);
            }
            return new TugString(pos, string.value);
        } else if (value instanceof TugNone) {
            if (this.value == 0) {
                return new TugNumber(pos, this.value);
            }
            return new TugNone(pos);
        }
        return new TugNumber(0);
    }

    public Object or(TugObject value) {
        if (value instanceof TugNumber number) {
            return new TugNumber(pos, this.value != 0 ? this.value : number.value);
        } else if (value instanceof TugString string) {
            if (this.value != 0) {
                return new TugNumber(pos, this.value);
            }
            return new TugString(pos, string.value);
        } else if (value instanceof TugNone) {
            if (this.value != 0) {
                return new TugNumber(pos, this.value);
            }
            return new TugNone(pos);
        }
        return new TugNumber(0);
    }

    public Object not() {
        return new TugNumber(this.value == 0 ? 1 : 0);
    }
}
