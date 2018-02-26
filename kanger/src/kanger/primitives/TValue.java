package kanger.primitives;

import java.util.*;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {

    private List<TSubst> values = new ArrayList<>();
    private int current = 0;

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getCurrent() {
        return current;
    }

    public Term getValue() {
        return values.isEmpty() ? null : values.get(current).getValue();
    }

    public TSubst get(int i) {
        if (values.isEmpty() || i >= values.size() || i < 0) {
            return null;
        }
        return values.get(i);
    }

    public TSubst addValue(Term v) {
        int i = contains(v);
        if (i == -1) {
            TSubst s = new TSubst();
            s.setValue(v);
            values.add(s);
            return s;
        } else {
            return values.get(i);
        }
    }

    public TSubst setValue(Term v) {
        int i = contains(v);
        if (i == -1) {
            TSubst s = new TSubst();
            s.setValue(v);
            values.add(s);
            current = values.size() - 1;
        } else {
            current = i;
        }
        return values.get(current);
    }

    public void setSrcSolve(Domain solve) {
        if (!values.isEmpty()) {
            values.get(current).setSrcSolve(solve);
        }
    }

    public Domain getSrcSolve() {
        if (!values.isEmpty()) {
            return values.get(current).getSrcSolve();
        } else {
            return null;
        }
    }

    public void setDstSolve(Domain solve) {
        if (!values.isEmpty()) {
            values.get(current).setDstSolve(solve);
        }
    }

    public Domain getDstSolve() {
        if (!values.isEmpty()) {
            return values.get(current).getDstSolve();
        } else {
            return null;
        }
    }
    
    public void setSuccess(boolean success) {
        if (!values.isEmpty()) {
            values.get(current).setSuccess(success);
        }
    }

    public boolean isSuccess() {
        if (!values.isEmpty()) {
            return values.get(current).isSuccess();
        } else {
            return false;
        }
    }

    public int contains(Term v) {
        for (int i = 0; i < values.size(); ++i) {
            if (values.get(i).getValue().equals(v)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

}
