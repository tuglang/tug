public class Expression {
    public Object left;
    public Token op;
    public Object right;

    public Expression(Object left, Token op, Object right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expression(Token op, Object right) {
        this.left = null;
        this.op = op;
        this.right = right;
    }

    public boolean isunary() {
        return this.left == null;
    }
}
