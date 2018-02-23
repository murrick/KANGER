package kanger.primitives;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {
    private Term value = null;
    private int level = 0;
    private Solve solve = null;

    public Term getValue() {
        return value;
    }

    public void setValue(Term value) {
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Solve getSolve() {
        return solve;
    }

    public void setSolve(Solve solve) {
        this.solve = solve;
    }
}
