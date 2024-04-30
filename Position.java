import java.io.Serializable;

public class Position implements Serializable {
    public int idx;
    public int col;
    public int line;
    public String fn;

    public Position(int idx, int col, int line, String fn) {
        this.idx = idx;
        this.col = col;
        this.line = line;
        this.fn = fn;
    }

    public void advance() {
        idx++;
        col++;
    }

    public void check(char char_) {
        if (char_ == '\n') {
            col = -1;
            line++;
        }
    }

    public Position copy() {
        return new Position(idx, col, line, fn);
    }
}
