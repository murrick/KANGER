package kanger.primitives;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {
    private Term value = null;
    private int level = 0;
    private Domain solve = null;

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

    public Domain getSolve() {
        return solve;
    }

    public void setSolve(Domain solve) {
        this.solve = solve;
    }
}
