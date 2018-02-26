package kanger.primitives;

import java.util.*;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {

    private List<Term> value = new ArrayList<>();
    private int current = 0;
    private Set<Integer> success = new HashSet<>();
    private Domain solve = null;

    public Set<Integer> getSuccess() {
        return success;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getCurrent() {
        return current;
    }

    public Term getValue() {
        return value.isEmpty() ? null : value.get(current);
    }

    public void addValue(Term v) {
        int i = exists(v);
        if (i == -1) {
            value.add(v);
        }
    }

    public int exists(Term v) {
        for (int i = 0; i < value.size(); ++i) {
            if (value.get(i).equals(v)) {
                return i;
            }
        }
        return -1;
    }

    public Domain getSolve() {
        return solve;
    }

    public void setSolve(Domain solve) {
        this.solve = solve;
    }
}
