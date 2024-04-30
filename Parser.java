import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    public ArrayList<Token> tokens;
    public int idx;
    public Token tok;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        idx = -1;
        advance();
    }

    void advance() {
        idx++;
        if (idx >= tokens.size()) tok = null;
        else tok = tokens.get(idx);
    }

    Object start() {
        ArrayList<Task> tasks = new ArrayList<>();

        while (tok != null) {
            if (tok.match(TokenType.EOF)) break;
            
            Object err = parse(tasks);
            if (err instanceof TugError) return err;

            if (err.equals(true)) continue;

            advance();
        }

        return tasks;
    }

    Object parse(ArrayList<Task> tasks) {
        if (tok.match(TokenType.IDENTIFIER)) {
            Token identifier = tok;
            advance();
            if (tok.matches(
                TokenType.EQ,
                TokenType.ADDEQ,
                TokenType.SUBEQ,
                TokenType.MULEQ,
                TokenType.DIVEQ,
                TokenType.POWEQ,
                TokenType.MODEQ
            )) {
                Token op = tok;
                advance();
                Object expr = get_expr();
                if (expr instanceof TugError err) return err;
                tasks.add(new Task(TaskType.VARIABLE_ASSIGNMENT, identifier, op, expr));
                return true;
            } else if (tok.match(TokenType.LPAREN)) {
                Object args = get_args();
                if (args instanceof TugError) return args;
                tasks.add(new Task(TaskType.CALLFUNC_STATEMENT, identifier, args));
            } else if (tok.matches(TokenType.DOT, TokenType.LSQUARE)) {
                ArrayList<Object> indices = new ArrayList<>();
                ArrayList<Token> tokens = new ArrayList<>();
                while (tok.matches(TokenType.DOT, TokenType.LSQUARE)) {
                    if (tok.match(TokenType.DOT)) {
                        Token op = tok;
                        advance();
                        if (!tok.match(TokenType.IDENTIFIER)) return new TugError(
                            "expected identifier", tok.pos
                        );
                        indices.add(new Token(TokenType.STR, tok.value, tok.pos));
                        tokens.add(op);
                        advance();
                    } else if (tok.match(TokenType.LSQUARE)) {
                        Token square = tok;
                        advance();
        
                        Object expr = this.expr();
                        if (expr instanceof TugError) return expr;
        
                        if (!tok.match(TokenType.RSQUARE)) return new TugError(
                            "unclosed '['", square.pos
                        );
                        indices.add(expr);
                        tokens.add(new Token(TokenType.DOT, square.pos));
                        advance();
                    }
                }

                Token op = tok;
                
                if (!tok.matches(
                    TokenType.EQ,
                    TokenType.ADDEQ,
                    TokenType.SUBEQ,
                    TokenType.MULEQ,
                    TokenType.DIVEQ,
                    TokenType.POWEQ,
                    TokenType.MODEQ
                )) return new TugError(
                    "expected assign operator", tok.pos
                );

                advance();

                Object value = expr();
                if (value instanceof TugError) return value;

                tasks.add(new Task(TaskType.ASSIGN_ATTR_STATEMENT, identifier, indices, tokens, op, value));
                return true;
            }
        } else if (tok.match(TokenType.KW_IF)) {
            Object if_expr = this.if_expr();
            if (if_expr instanceof TugError err) return err;
            tasks.add((Task) if_expr);
            return true;
        } else if (tok.match(TokenType.KW_LOOP)) {
            Object loop_expr = this.loop_expr();
            if (loop_expr instanceof TugError err) return err;
            tasks.add((Task) loop_expr);
            return true;
        } else if (tok.match(TokenType.KW_BREAK)) tasks.add(new Task(
            TaskType.BREAK_STATEMENT, tok)
        ); else if (tok.match(TokenType.KW_SKIP)) tasks.add(new Task(
            TaskType.SKIP_STATEMENT, tok)
        ); else if (tok.match(TokenType.KW_FUNC)) {
            Object func_expr = this.func_expr();
            if (func_expr instanceof TugError err) return err;
            tasks.add((Task) func_expr);
            return true;
        } else if (tok.match(TokenType.KW_RET)) {
            advance();
            Object expr = this.get_expr();
            if (expr instanceof TugError) return expr;
            tasks.add(new Task(TaskType.RET_STATEMENT, expr));
            return true;
        } else return new TugError(
            "unexpected token", tok.pos
        );

        return false;
    }

    Object get_args() {
        advance();
        ArrayList<Object> args = new ArrayList<>();
        
        while (!tok.match(TokenType.RPAREN)) {
            Object value = get_expr();
            if (value instanceof TugError) return value;

            args.add(value);

            if (tok.match(TokenType.COMMA)) advance();
            else if (!tok.match(TokenType.RPAREN)) {
                return new TugError(
                "expected ')'", tok.pos
            );}
        }

        return args;
    }

    Object get_stmts(TokenType... types) {
        ArrayList<Task> code = new ArrayList<>();
        while (!tok.matches(types)) {
            Object err = parse(code);
            if (err instanceof TugError) return err;
            if (err.equals(true)) continue;
            advance();
        }
        return code;
    }

    @SuppressWarnings("unchecked")
    Object func_expr() {
        advance();
        if (!tok.match(TokenType.IDENTIFIER)) return new TugError(
            "expected identifier", tok.pos
        );
        Token name = tok;

        advance();
        if (!tok.match(TokenType.LPAREN)) return new TugError(
            "expected '('", tok.pos
        );

        advance();
        ArrayList<Token> args = new ArrayList<>();
        while (!tok.match(TokenType.RPAREN)) {
            if (!tok.match(TokenType.IDENTIFIER)) return new TugError(
                "expected identifier or ')'", tok.pos
            );
            args.add(tok);
            advance();
            if (tok.match(TokenType.COMMA)) advance();
            else if (!tok.match(TokenType.RPAREN)) return new TugError(
                "expected ',' or ')'", tok.pos
            );
        }
        advance();
        
        if (!tok.match(TokenType.LCURLY)) return new TugError(
            "expected '{'", tok.pos
        );

        Token curly = tok;

        advance();
        Object stmts = get_stmts(TokenType.EOF, TokenType.RCURLY);
        if (stmts instanceof TugError) return stmts;

        if (!tok.match(TokenType.RCURLY)) return new TugError(
            "unclosed '{'", curly.pos
        );
        advance();

        ArrayList<Task> body = (ArrayList<Task>) stmts;

        return new Task(TaskType.FUNC_STATEMENT, name, args, body);
    }

    @SuppressWarnings("unchecked")
    Object loop_expr() {
        advance();
        boolean loopif = false;
        Token curly = null;
        Object amount = null;

        if (!tok.match(TokenType.LCURLY)) {
            if (tok.match(TokenType.KW_IF)) {
                loopif = true;
                advance();
            }

            amount = get_expr();
            if (amount instanceof TugError) return amount;

            curly = tok;

            if (!tok.match(TokenType.LCURLY)) return new TugError(
                "expected '{'", tok.pos
            );
            
            advance();
        }

        Object stmts = get_stmts(TokenType.EOF, TokenType.RCURLY);
        if (stmts instanceof TugError) return stmts;

        if (!tok.match(TokenType.RCURLY)) return new TugError(
            "unclosed '{'", curly.pos
        );
        advance();

        ArrayList<Task> body = (ArrayList<Task>) stmts;

        if (loopif) {
            return new Task(TaskType.LOOPIF_STATEMENT, amount, body);
        } else if (amount == null) {
            return new Task(TaskType.FOREVERLOOP_STATEMENT, body);
        }

        Task loop_statement = new Task(TaskType.LOOP_STATEMENT, amount, body);

        return loop_statement;
    }

    @SuppressWarnings("unchecked")
    Object if_expr() {
        ArrayList<Task> elseif_cases = new ArrayList<>();
        Task else_case = null;

        if (!tok.match(TokenType.KW_IF)) return new TugError(
            "expected if keyword", tok.pos
        );

        advance();

        Object condition = get_expr();
        if (condition instanceof TugError) return condition;

        Token curly = tok;

        if (!tok.match(TokenType.LCURLY)) return new TugError(
            "expected '{'", tok.pos
        );
        
        advance();
        Object stmts = get_stmts(TokenType.EOF, TokenType.RCURLY);
        if (stmts instanceof TugError) return stmts;

        if (!tok.match(TokenType.RCURLY)) return new TugError(
            "unclosed '{'", curly.pos
        );
        advance();

        ArrayList<Task> body = (ArrayList<Task>) stmts;
        Task if_case = new Task(TaskType.IF_STATEMENT, condition, body);

        while (tok.match(TokenType.KW_ELSE)) {
            advance();
            if (tok.match(TokenType.KW_IF)) {
                advance();

                condition = get_expr();
                if (condition instanceof TugError) return condition;

                curly = tok;

                if (!tok.match(TokenType.LCURLY)) return new TugError(
                    "expected '{'", tok.pos
                );
                
                advance();
                stmts = get_stmts(TokenType.EOF, TokenType.RCURLY);
                if (stmts instanceof TugError) return stmts;

                if (!tok.match(TokenType.RCURLY)) return new TugError(
                    "unclosed '{'", curly.pos
                );
                advance();

                body = (ArrayList<Task>) stmts;
                Task elseif_case = new Task(TaskType.ELSEIF_STATEMENT, condition, body);
                elseif_cases.add(elseif_case);
            } else if (tok.match(TokenType.LCURLY)) {
                curly = tok;
                advance();
                stmts = get_stmts(TokenType.EOF, TokenType.RCURLY);
                if (stmts instanceof TugError) return stmts;

                if (!tok.match(TokenType.RCURLY)) return new TugError(
                    "unclosed '{'", curly.pos
                );
                advance();

                body = (ArrayList<Task>) stmts;
                else_case = new Task(TaskType.ELSE_STATEMENT, body);
                break;
            } else return new TugError(
                "expected if keyword or '{'", tok.pos
            );
        }

        if_case.values.add(elseif_cases);
        if_case.values.add(else_case);

        return if_case;
    }

    Object get_expr() {
        Object expr = this.expr();
        return expr;
    }

    Object factor() {
        Token tok = this.tok;

        if (tok.matches(TokenType.ADD, TokenType.SUB)) {
            advance();
            Object factor = this.factor();
            if (factor instanceof TugError) return factor;
            return new Expression(tok, attrs(factor));
        } else if (tok.isvalue()) {
            advance();
            Object value = attrs(tok);
            if (this.tok.match(TokenType.LPAREN)) {
                Object args = get_args();
                if (args instanceof TugError) return args;
                advance();
                return new Task(TaskType.CALLFUNC_STATEMENT, value, args);
            }
            return value;
        } else if (tok.match(TokenType.LPAREN)) {
            advance();
            Object expr = this.expr();
            if (expr instanceof TugError) return expr;
            if (this.tok.match(TokenType.RPAREN)) {
                advance();
                return attrs(expr);
            } else return new TugError(
                "unclosed '('", tok.pos
            );
        } else if (tok.match(TokenType.LCURLY)) {
            Object table_expr = this.table_expr();
            if (table_expr instanceof TugError) return table_expr;
            advance();
            return attrs(table_expr);
        }

        return new TugError(
            "unexpected token",
            tok.pos
        );
    }

    Object table_expr() {
        Token curly = tok;
        advance();

        HashMap<Object, Object> map = new HashMap<>();

        while (!tok.match(TokenType.RCURLY)) {
            if (tok.match(TokenType.IDENTIFIER)) {
                Token identifier = tok;
                advance();
                if (!tok.match(TokenType.EQ)) return new TugError(
                    "expected '='", tok.pos
                );
                advance();
                Object expr = this.expr();
                if (expr instanceof TugError) return expr;
                map.put(new Token(TokenType.STR, identifier.value, identifier.pos), expr);
            } else if (tok.match(TokenType.LSQUARE)) {
                Token square = tok;
                advance();
                Object key = this.expr();
                if (key instanceof TugError) return key;
                if (!tok.match(TokenType.RSQUARE)) return new TugError(
                    "unclosed '['", square.pos
                );
                advance();
                if (!tok.match(TokenType.EQ)) return new TugError(
                    "expected '='", tok.pos
                );
                advance();
                Object value = this.expr();
                if (value instanceof TugError) return value;
                map.put(key, value);
            } else {
                Object expr = this.expr();
                if (expr instanceof TugError) return expr;

                map.put(new Token(TokenType.NUM, Double.valueOf(map.size()), null), expr);
            }
            
            if (tok.match(TokenType.COMMA)) advance();
            else if (!tok.match(TokenType.RCURLY)) return new TugError(
                "unclosed '{'", curly.pos
            );
        }

        return map;
    }

    Object term() {
        return bin_op("factor", TokenType.MUL, TokenType.DIV, TokenType.MOD, TokenType.POW);
    }

    Object comp_expr() {
        if (tok.match(TokenType.NOT)) {
            Token op_tok = tok;
            advance();

            Object comp_expr = this.comp_expr();
            if (comp_expr instanceof TugError) return comp_expr;

            return new Expression(op_tok, comp_expr);
        }

        Object arith_comp = bin_op("arith_comp", 
            TokenType.EQEQ,
            TokenType.NEQ,
            TokenType.GT,
            TokenType.GE,
            TokenType.LT,
            TokenType.LE
        );

        if (arith_comp instanceof TugError) return new TugError(
            "expected value or operator", tok.pos
        );

        return arith_comp;
    }

    Object attrs(Object left) {
        while (tok.matches(TokenType.DOT, TokenType.LSQUARE)) {
            if (tok.match(TokenType.DOT)) {
                Token op = tok;
                advance();
                if (!tok.match(TokenType.IDENTIFIER)) return new TugError(
                    "expected identifier", tok.pos
                );
                left = new Expression(left, op, new Token(TokenType.STR, tok.value, tok.pos));
                advance();
            } else if (tok.match(TokenType.LSQUARE)) {
                Token square = tok;
                advance();

                Object expr = this.expr();
                if (expr instanceof TugError) return expr;

                if (!tok.match(TokenType.RSQUARE)) return new TugError(
                    "unclosed '['", square.pos
                );
                left = new Expression(left, new Token(TokenType.DOT, square.pos), expr);
                advance();
            }
        }

        return left;
    }

    Object expr() {
        return bin_op("comp_expr",
            TokenType.AND,
            TokenType.OR
        );
    }

    Object arith_expr() {
        return bin_op("term", TokenType.ADD, TokenType.SUB);
    }

    Object bin_op(String func, TokenType... ops) {
        Object left = null;

        if (func.equals("factor")) left = factor();
        else if (func.equals("term")) left = term();
        else if (func.equals("comp_expr")) left = comp_expr();
        else if (func.equals("arith_comp")) left = arith_expr();
        else return null;

        if (left instanceof TugError) return left;

        while (tok.matches(ops)) {
            Token op = tok;
            advance();

            Object right = null;
            if (func.equals("factor")) right = factor();
            else if (func.equals("term")) right = term();
            else if (func.equals("comp_expr")) right = comp_expr();
            else if (func.equals("arith_comp")) right = arith_expr();
            if (right instanceof TugError) return right;

            left = new Expression(left, op, right);
        }

        return left;
    }
}
