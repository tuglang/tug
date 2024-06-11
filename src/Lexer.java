import java.util.ArrayList;

public class Lexer {
    public String text;
    public TugPosition pos;
    public char current_char;

    public Lexer(String text, String fn) {
        this.text = text;
        pos = new TugPosition(-1, -1, 1, fn);
        advance();
    }

    void advance() {
        pos.advance();
        if (pos.idx >= text.length()) {
            current_char = '\0';
            return;
        } else {
            current_char = text.charAt(pos.idx);
        }
        
        pos.check(current_char);
    }

    public Object start() {
        ArrayList<Token> tokens = new ArrayList<>();

        while (current_char != '\0') {
            if (current_char == '+') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '+') tokens.add(new Token(TokenType.ADDADD, cpos));
                else if (current_char == '=') tokens.add(new Token(TokenType.ADDEQ, cpos));
                else {tokens.add(new Token(TokenType.ADD, cpos)); continue;}
            } else if (current_char == '-') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '-') tokens.add(new Token(TokenType.SUBSUB, cpos));
                else if (current_char == '=') tokens.add(new Token(TokenType.SUBEQ, cpos));
                else {tokens.add(new Token(TokenType.SUB, cpos)); continue;}
            } else if (current_char == '*') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '=') tokens.add(new Token(TokenType.MULEQ, cpos));
                else {tokens.add(new Token(TokenType.MUL, cpos)); continue;}
            } else if (current_char == '/') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '=') tokens.add(new Token(TokenType.DIVEQ, cpos));
                else {tokens.add(new Token(TokenType.DIV, cpos)); continue;}
            } else if (current_char == '^') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '=') tokens.add(new Token(TokenType.POWEQ, cpos));
                else {tokens.add(new Token(TokenType.POW, cpos)); continue;}
            } else if (current_char == '%') {
                TugPosition cpos = pos.copy();
                advance();
                if (current_char == '=') tokens.add(new Token(TokenType.MODEQ, cpos));
                else {tokens.add(new Token(TokenType.MOD, cpos)); continue;}
            } else if (current_char == '=') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '=') {
                    tokens.add(new Token(TokenType.EQEQ, cpos));
                } else {
                    tokens.add(new Token(TokenType.EQ, cpos));
                    continue;
                }
            } else if (current_char == '!') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '=') {
                    tokens.add(new Token(TokenType.NEQ, cpos));
                } else {
                    tokens.add(new Token(TokenType.NOT, cpos));
                    continue;
                }
            } else if (current_char == '>') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '=') {
                    tokens.add(new Token(TokenType.GE, cpos));
                } else {
                    tokens.add(new Token(TokenType.GT, cpos));
                    continue;
                }
            } else if (current_char == '<') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '=') {
                    tokens.add(new Token(TokenType.LE, cpos));
                } else {
                    tokens.add(new Token(TokenType.LT, cpos));
                    continue;
                }
            } else if (current_char == '&') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '&') {
                    tokens.add(new Token(TokenType.AND, cpos));
                } else {
                    return new TugError(
                        "invalid syntax", cpos
                    );
                }
            } else if (current_char == '|') {
                TugPosition cpos = this.pos.copy();
                advance();
                if (current_char == '|') {
                    tokens.add(new Token(TokenType.OR, cpos));
                } else {
                    return new TugError(
                        "invalid syntax", cpos
                    );
                }
            } else if (current_char == '.') {
                tokens.add(new Token(TokenType.DOT, pos.copy()));
            } else if (current_char == ':') {
                tokens.add(new Token(TokenType.COLON, pos.copy()));
            } else if (current_char == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, pos.copy()));
            } else if (current_char == '(') {
                tokens.add(new Token(TokenType.LPAREN, pos.copy()));
            } else if (current_char == ')') {
                tokens.add(new Token(TokenType.RPAREN, pos.copy()));
            } else if (current_char == '[') {
                tokens.add(new Token(TokenType.LSQUARE, pos.copy()));
            } else if (current_char == ']') {
                tokens.add(new Token(TokenType.RSQUARE, pos.copy()));
            } else if (current_char == '{') {
                tokens.add(new Token(TokenType.LCURLY, pos.copy()));
            } else if (current_char == '}') {
                tokens.add(new Token(TokenType.RCURLY, pos.copy()));
            } else if (current_char == ',') {
                tokens.add(new Token(TokenType.COMMA, pos.copy()));
            } else if (current_char == '"' || current_char == '\'') {
                Object res = make_str();
                if (res instanceof TugError err) return err;
                tokens.add((Token) res);
            } else if (current_char >= '0' && current_char <= '9') {
                Object res = make_num();
                if (res instanceof TugError err) return err;
                tokens.add((Token) res);
                continue;
            } else if (
                (current_char >= 'a' && current_char <= 'z')
                || (current_char >= 'A' && current_char <= 'Z')
                || current_char == '_'
            ) {
                tokens.add(make_identifier());
                continue;
            } else if (current_char == ' ' || current_char == '\n' || current_char == '\t' || current_char == 13) {
                // Pass
            } else if (current_char == '#') {
                while (current_char != '\0' && current_char != '\n') advance();
            } else return new TugError(
                String.format("illegal character '%s'", current_char),
                pos
            );
            advance();
        }

        tokens.add(new Token(TokenType.EOF, pos));

        return tokens;
    }

    Object make_str() {
        TugPosition pos = this.pos.copy();
        char deli = current_char;
        advance();

        StringBuilder builder = new StringBuilder();

        while (current_char != '\0' && current_char != deli) {
            if (current_char == '\\') {
                advance();
                if (current_char == 'n') builder.append("\n");
                else if (current_char == 't') builder.append("\t");
                else if (current_char == '0') builder.append("\0");
                else if (current_char == 'r') builder.append("\r");
                else if (current_char == 'f') builder.append("\f");
                else if (current_char == 'b') builder.append("\b");
                else if (current_char == '\\') builder.append("\\");
                else if (current_char == '\'') builder.append("'");
                else if (current_char == '"') builder.append("\"");
                else builder.append("\\".concat(String.valueOf(current_char)));
                advance();
                continue;
            } else if (current_char == '\n') {
                builder.append("\n");
                advance();
                continue;
            }
            builder.append(current_char);
            advance();
        }

        if (current_char != deli) return new TugError(
            "unfinished string", pos
        );

        return new Token(TokenType.STR, builder.toString(), pos);
    }

    Object make_num() {
        TugPosition pos = this.pos.copy();
        StringBuilder builder = new StringBuilder();
        builder.append(current_char);
        advance();

        int dots = 0;
        while (current_char != '\0' && current_char >= '0' && current_char <= '9' || current_char == '.') {
            if (current_char == '.') dots++;
            builder.append(current_char);
            advance();
        }

        if (dots > 1) return new TugError(
            String.format("malformed number '%s'", builder.toString()),
            pos
        );

        return new Token(TokenType.NUM, Double.parseDouble(builder.toString()), pos);
    }

    Token make_identifier() {
        TugPosition pos = this.pos.copy();
        StringBuilder builder = new StringBuilder();
        builder.append(current_char);
        advance();

        while (current_char != '\0' && (
            (current_char >= 'a' && current_char <= 'z')
            || (current_char >= 'A' && current_char <= 'Z')
            || (current_char >= '0' && current_char <= '9')
            || current_char == '_'
        )) {
            builder.append(current_char);
            advance();
        }

        String res = builder.toString();
        if (res.equals("if")) return new Token(TokenType.KW_IF, pos);
        if (res.equals("else")) return new Token(TokenType.KW_ELSE, pos);
        if (res.equals("loop")) return new Token(TokenType.KW_LOOP, pos);
        if (res.equals("skip")) return new Token(TokenType.KW_SKIP, pos);
        if (res.equals("break")) return new Token(TokenType.KW_BREAK, pos);
        if (res.equals("func")) return new Token(TokenType.KW_FUNC, pos);
        if (res.equals("ret")) return new Token(TokenType.KW_RET, pos);
        if (res.equals("none")) return new Token(TokenType.NONE, pos);

        return new Token(TokenType.IDENTIFIER, res, pos);
    }
}
