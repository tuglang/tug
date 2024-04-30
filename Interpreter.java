import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {
    public ArrayList<Task> tasks;
    public TugTable global;

    public Interpreter(ArrayList<Task> tasks, TugTable global) {
        this.tasks = tasks;
        this.global = global;
    }

    public Object start() {
        Object ret = execute(tasks);
        if (ret instanceof Task taskret) {
            if (taskret.match(TaskType.SKIP_STATEMENT)) return new TugError(
                "skip outside loop", ((Token) taskret.values.get(0)).pos
            );
        } else if (ret instanceof TugError) return ret;
        return ret;
    }

    Object execute(ArrayList<Task> tasks) {
        for (Task task : tasks) {
            Object res = null;
            if (task.match(TaskType.VARIABLE_ASSIGNMENT)) {
                res = variable_assignment(task);
            } else if (task.match(TaskType.IF_STATEMENT)) {
                res = if_statement(task);
            } else if (task.match(TaskType.LOOP_STATEMENT)) {
                res = loop_statement(task);
            } else if (task.match(TaskType.LOOPIF_STATEMENT)) {
                res = loopif_statement(task);
            } else if (task.match(TaskType.FOREVERLOOP_STATEMENT)) {
                res = foreverloop_statement(task);
            } else if (task.match(TaskType.BREAK_STATEMENT)) {
                return task;
            } else if (task.match(TaskType.SKIP_STATEMENT)) {
                return task;
            } else if (task.match(TaskType.FUNC_STATEMENT)) {
                res = func_statement(task);
            } else if (task.match(TaskType.CALLFUNC_STATEMENT)) {
                Object res_ = callfunc_statement(task);
                if (res_ instanceof TugError) return res_;
            } else if (task.match(TaskType.RET_STATEMENT)) {
                return refresh(task.values.get(0));
            } else if (task.match(TaskType.ASSIGN_ATTR_STATEMENT)) {
                res = assign_attr_statement(task);
            }
            if (res instanceof TugError) return res;
            if (res instanceof TugObject) return res;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    Object assign_attr_statement(Task task) {
        Object value = task.values.get(0);

        ArrayList<Object> indices = (ArrayList<Object>) task.values.get(1);
        ArrayList<Token> tokens = (ArrayList<Token>) task.values.get(2);
        Token op = (Token) task.values.get(3);

        for (int idx = 0; idx < indices.size() - 1; idx++) {
            value = new Expression(value, tokens.get(idx), indices.get(0));
        }

        Object og_val = new Expression(value, tokens.get(tokens.size() - 1), indices.get(indices.size() - 1));

        Object val = refresh(value);
        if (val instanceof TugError) return val;

        Object key = refresh(indices.get(indices.size() - 1));
        if (key instanceof TugError) return key;

        Object expr = refresh(task.values.get(4));
        if (expr instanceof TugError) return expr;

        TugObject tugobj = (TugObject) expr;

        if (op.match(TokenType.EQ)) ((TugObject) val).set((TugObject) key, tugobj);
        else {
            Object ogval = refresh(og_val);
            if (ogval instanceof TugError) return ogval;
            TugObject tog_val = (TugObject) ogval;
            tog_val.pos = ((Token) task.values.get(0)).pos;

            Object res = null;

            if (op.match(TokenType.ADDEQ)) res = tog_val.add(tugobj);
            else if (op.match(TokenType.SUBEQ)) res = tog_val.sub(tugobj);
            else if (op.match(TokenType.MULEQ)) res = tog_val.mul(tugobj);
            else if (op.match(TokenType.DIVEQ)) res = tog_val.div(tugobj);
            else if (op.match(TokenType.POWEQ)) res = tog_val.pow(tugobj);
            else if (op.match(TokenType.MODEQ)) res = tog_val.mod(tugobj);

            if (res instanceof TugError) return res;

            ((TugObject) val).set((TugObject) key, (TugObject) res);
        }
        return null;
    }

    Position getpos(Object obj) {
        if (obj instanceof Token tok) {
            return tok.pos;
        } else if (obj instanceof Expression expr) {
            return getpos(expr.left);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    Object callfunc_statement(Task task) {
        Position pos = getpos(task.values.get(0));
        Object obj = refresh(task.values.get(0));
        if (obj instanceof Error) return obj;
        TugObject function = (TugObject) obj;
        ArrayList<Object> args = (ArrayList<Object>) task.values.get(1);

        TugObject arguments[] = new TugObject[args.size()];
        for (int idx = 0; idx < args.size(); idx++) {
            Object ret = refresh(args.get(idx));
            if (ret instanceof TugError) return ret;
            arguments[idx] = (TugObject) ret;
        }
        return function.call(pos, global, arguments);
    }

    @SuppressWarnings("unchecked")
    Object func_statement(Task task) {
        Token name = (Token) task.values.get(0);
        ArrayList<Token> args = (ArrayList<Token>) task.values.get(1);
        ArrayList<Task> body = (ArrayList<Task>) task.values.get(2);

        ArrayList<String> arg_names = new ArrayList<>();
        for (Token arg : args) {
            arg_names.add((String) arg.value);
        }

        global.set(name.value, new TugFunction((String) name.value, arg_names, body, global));

        return null;
    }

    Object variable_assignment(Task task) {
        Token identifier = (Token) task.values.get(0);
        Token op = (Token) task.values.get(1);
        Object value = refresh(task.values.get(2));

        if (value instanceof TugError) return value;

        TugObject og_val = (TugObject) refresh(identifier);

        TugObject tugobj = (TugObject) value;
        Object res = null;
        
        if (op.match(TokenType.EQ)) res = tugobj;
        else if (op.match(TokenType.ADDEQ)) res = og_val.add(tugobj);
        else if (op.match(TokenType.SUBEQ)) res = og_val.sub(tugobj);
        else if (op.match(TokenType.MULEQ)) res = og_val.mul(tugobj);
        else if (op.match(TokenType.DIVEQ)) res = og_val.div(tugobj);
        else if (op.match(TokenType.POWEQ)) res = og_val.pow(tugobj);
        else if (op.match(TokenType.MODEQ)) res = og_val.mod(tugobj);

        if (res instanceof TugError) return res;

        global.set(identifier.value, convert((TugObject) res));
        return null;
    }

    @SuppressWarnings("unchecked")
    Object if_statement(Task task) {
        Object condition = refresh(task.values.get(0));
        if (condition instanceof TugError) return condition;
        ArrayList<Task> body = (ArrayList<Task>) task.values.get(1);
        ArrayList<Task> elseif_cases = (ArrayList<Task>) task.values.get(2);
        Task else_case = (Task) task.values.get(3);

        if (check_condition((TugObject) condition)) return execute(body);
        else {
            for (Task elseif_task : elseif_cases) {
                condition = refresh(elseif_task.values.get(0));
                if (condition instanceof TugError) return condition;
                body = (ArrayList<Task>) elseif_task.values.get(1);

                if (check_condition((TugObject) condition)) return execute(body);
            }
        }
        if (else_case == null) return null;
        body = (ArrayList<Task>) else_case.values.get(0);
        return execute(body);
    }

    @SuppressWarnings("unchecked")
    Object loop_statement(Task task) {
        Object amount = refresh(task.values.get(0));
        if (amount instanceof TugError) return amount;
        ArrayList<Task> body = (ArrayList<Task>) task.values.get(1);

        Object value = convert((TugObject) amount);
        double max = 0;
        if (value instanceof Double) {
            max = (Double) value;
        } else if (value instanceof String) {
            max = Double.valueOf(((String)value).length());
        } else {
            max = 0;
        }

        for (double i = 0; i < max; i++) {
            Object ret = execute(body);
            if (ret instanceof TugError) return ret;
            if (ret instanceof Task taskret) {
                if (taskret.match(TaskType.SKIP_STATEMENT)) continue;
                else if (taskret.match(TaskType.BREAK_STATEMENT)) break;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    Object loopif_statement(Task task) {
        Object condition = task.values.get(0);
        ArrayList<Task> body = (ArrayList<Task>) task.values.get(1);

        Object con = refresh(condition);
        TugObject value = (TugObject) con;

        while (check_condition(value)) {
            Object ret = execute(body);
            if (ret instanceof TugError) return ret;
            if (ret instanceof Task taskret) {
                if (taskret.match(TaskType.SKIP_STATEMENT)) continue;
                else if (taskret.match(TaskType.BREAK_STATEMENT)) break;
            }

            con = refresh(condition);
            value = (TugObject) con;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    Object foreverloop_statement(Task task) {
        ArrayList<Task> body = (ArrayList<Task>) task.values.get(0);

        while (true) {
            Object ret = execute(body);
            if (ret instanceof TugError) return ret;
            if (ret instanceof Task taskret) {
                if (taskret.match(TaskType.SKIP_STATEMENT)) continue;
                else if (taskret.match(TaskType.BREAK_STATEMENT)) break;
            }
        }

        return null;
    }

    boolean check_condition(TugObject obj) {
        Object value = convert(obj);
        if (value == null) return false;

        return !value.equals(0d) && !value.equals("");
    }

    public static Object convert(TugObject obj) {
        if (obj instanceof TugNumber num) return num.value;
        if (obj instanceof TugString str) return str.value;
        if (obj instanceof TugNone) return null;
        if (obj instanceof TugFunction func) return func;
        if (obj instanceof TugTable table) return table;
        return null;
    }

    public static TugObject deconvert(Object obj) {
        if (obj instanceof Double val) return new TugNumber(val);
        if (obj instanceof String val) return new TugString(val);
        if (obj instanceof TugFunction func) return func;
        if (obj instanceof TugTable table) return table;
        return new TugNone();
    }

    @SuppressWarnings("unchecked")
    Object refresh(Object value) {
        if (value instanceof Token tok) {
            if (tok.match(TokenType.NUM)) return new TugNumber(tok.pos, (Double) tok.value);
            if (tok.match(TokenType.STR)) return new TugString(tok.pos, (String) tok.value);
            if (tok.match(TokenType.NONE)) return new TugNone(tok.pos);
            if (tok.match(TokenType.IDENTIFIER)) {
                TugObject res = deconvert(global.get(tok.value));
                res.pos = tok.pos;
                return res;
            }
        } else if (value instanceof Expression expr) {
            Object left = refresh(expr.left);
            if (left instanceof TugError) return left;
            Object right = refresh(expr.right);
            if (right instanceof TugError) return right;
            Object result = null;
            Token op = expr.op;
            if (expr.isunary()) {
                if (right instanceof TugError) return right;
                TugObject tugleft = (TugObject) left;

                if (op.match(TokenType.SUB)) {
                    if (!(right instanceof TugNumber)) return new TugError(
                        "attempt to unary " + ((TugObject) right).type,
                        op.pos
                    );
                    right = ((TugNumber) right).mul(new TugNumber(-1));
                } else if (op.match(TokenType.NOT)) {
                    result = tugleft.not();
                }

                return right;
            } else {
                TugObject tugleft = (TugObject) left;
                TugObject tugright = (TugObject) right;
                if (op.match(TokenType.ADD)) {
                    result = tugleft.add(tugright);
                } else if (op.match(TokenType.SUB)) {
                    result = tugleft.sub(tugright);
                } else if (op.match(TokenType.MUL)) {
                    result = tugleft.mul(tugright);
                } else if (op.match(TokenType.DIV)) {
                    result = tugleft.div(tugright);
                } else if (op.match(TokenType.POW)) {
                    result = tugleft.pow(tugright);
                } else if (op.match(TokenType.MOD)) {
                    result = tugleft.mod(tugright);
                } else if (op.match(TokenType.EQEQ)) {
                    result = tugleft.eq(tugright);
                } else if (op.match(TokenType.NEQ)) {
                    result = tugleft.neq(tugright);
                } else if (op.match(TokenType.GT)) {
                    result = tugleft.gt(tugright);
                } else if (op.match(TokenType.GE)) {
                    result = tugleft.ge(tugright);
                } else if (op.match(TokenType.LT)) {
                    result = tugleft.lt(tugright);
                } else if (op.match(TokenType.LE)) {
                    result = tugleft.le(tugright);
                } else if (op.match(TokenType.AND)) {
                    result = tugleft.and(tugright);
                } else if (op.match(TokenType.OR)) {
                    result = tugleft.or(tugright);
                } else if (op.match(TokenType.DOT)) {
                    result = tugleft.index(tugright);
                }

                if (result instanceof TugError err) return err;

                return result;
            }
        } else if (value instanceof Task task) {
            if (task.match(TaskType.CALLFUNC_STATEMENT)) {
                return callfunc_statement(task);
            }
        } else if (value instanceof HashMap) {
            HashMap<Object, Object> map = (HashMap<Object, Object>) value;
            TugTable res = new TugTable();
            for (HashMap.Entry<Object, Object> entry : map.entrySet()) {
                Object key = refresh(entry.getKey());
                if (key instanceof TugError) return key;
                Object value_ = refresh(entry.getValue());
                if (value_ instanceof TugError) return value_;

                res.set(convert((TugObject) key), convert((TugObject) value_));
            }
            return res;
        }
        return null;
    }
}
