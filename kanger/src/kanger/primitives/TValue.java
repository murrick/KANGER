package kanger.primitives;

import java.util.*;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {

    private final List<TSubst> values = new ArrayList<>();
    private int current = 0;
    private long id = -1;

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getCurrent() {
        return current;
    }

    public Term getValue() {
        return values.isEmpty() ? null : values.get(current).getValue();
    }

//    public TSubst get(int i) {
//        if (values.isEmpty() || i >= values.size() || i < 0) {
//            return null;
//        }
//        return values.get(i);
//    }
//
//    public TSubst addValue(Term v) {
//        int i = contains(v);
//        if (i == -1) {
//            TSubst s = new TSubst();
//            s.setValue(v);
//            values.add(s);
//            return s;
//        } else {
//            return values.get(i);
//        }
//    }
//
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

    public Domain getSrcSolve() {
        if (!values.isEmpty()) {
            return values.get(current).getSrcSolve();
        } else {
            return null;
        }
    }

//    public Domain getSrcValue() {
//        if (!values.isEmpty()) {
//            return values.get(current).getSrcValue();
//        } else {
//            return null;
//        }
//    }

    public Domain getDstSolve() {
        if (!values.isEmpty()) {
            return values.get(current).getDstSolve();
        } else {
            return null;
        }
    }

//    public Domain getDstValue() {
//        if (!values.isEmpty()) {
//            return values.get(current).getDstValue();
//        } else {
//            return null;
//        }
//    }

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
            if (values.get(i).getValue() != null && values.get(i).getValue().equals(v)) {
                return i;
            }
        }
        return -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int size() {
        return values.size();
    }

    public boolean isDestFor(Domain d) {
        if (!values.isEmpty()) {
            for(TSubst t : values) {
                if(t.getSrcSolve().getId() == d.getId()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public boolean equals(Object t) {
        return !(t == null || !(t instanceof TValue)) && ((TValue) t).id == id;
    }

    public void setDstSolve(Domain d) {
        if (!values.isEmpty()) {
            values.get(current).setSolves(d, null);
        }
    }

    public void setSrcSolve(Domain d) {
        if (!values.isEmpty()) {
            values.get(current).setSolves(null, d);
        }
    }

    public void delValue(Domain d) {
        if (!values.isEmpty()) {
            for(TSubst s : values) {
                if(s.getDstSolve().getId() == d.getId()) {
                    values.remove(s);
                    break;
                }
            }

            if (current > values.size()) {
                current = values.size() - 1;
            }
            if (current < 0) {
                current = 0;
            }
        }
    }

    public void rollback() {
        if(values.size() > 0) {
            values.remove(values.size() -1);
            if (current >= values.size()) {
                current = values.size() - 1;
            }
        }
    }

    public List<TSubst> getValues() {
        return values;
    }
    
    
}
