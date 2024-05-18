public class Token {
    public TokenType type;
    public Object value;
    public TugPosition pos;

    public Token(TokenType type, Object value, TugPosition pos) {
        this.type = type;
        this.value = value;
        this.pos = pos;
    }

    public Token(TokenType type, TugPosition pos) {
        this.type = type;
        value = null;
        this.pos = pos;
    }

    public boolean match(TokenType type) {
        return this.type == type;
    }

    public boolean matches(TokenType... types) {
        for (TokenType type : types) {
            if (match(type)) return true;
        }
        return false;
    }

    public boolean isvalue() {
        return matches(TokenType.IDENTIFIER, TokenType.NUM, TokenType.STR, TokenType.NONE);
    }

    public boolean isop() {
        return matches(
            TokenType.ADD,
            TokenType.SUB,
            TokenType.MUL,
            TokenType.DIV,
            TokenType.POW,
            TokenType.MOD
        );
    }
}
