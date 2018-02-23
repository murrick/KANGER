package kanger.stores;

import kanger.primitives.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murray on 28.05.15.
 */
public class HypotesesStore {

    private List<Hypotese> root = null;
    private boolean enableStore = true;

    public Hypotese add(Solve solve) {
        if (!enableStore) {
            return null;
        }
        if (root == null) {
            root = new ArrayList<>();
        }
        Hypotese h = find(solve);
        if (h != null) {
            return h;
        } else {
            h = new Hypotese(solve);
            h.getRights().addAll(solve.getCauses().keySet());
            root.add(h);
            return h;
        }
    }

//    public Hypotese add(Predicate pred, List<Argument> arg, Right r) {
//        if (!enableStore) {
//            return null;
//        }
//        if (root == null) {
//            root = new ArrayList<>();
//        }
//        Hypotese h = find(pred, arg);
//        if (h != null) {
//            return h;
//        } else {
//            h = new Hypotese(pred, arg);
//            h.setRight(r);
//            root.add(h);
//            return h;
//        }
//
//    }

    public void enable(boolean enable) {
        enableStore = enable;
    }

    public boolean isEnabled() {
        return enableStore;
    }

    public Hypotese get(int index) {
        return root.get(index);
    }

    public Hypotese find(Solve solve) {
        for (Hypotese h : root) {
            if (h.getPredicate().equals(solve.getPredicate())) {
                int i = 0;
                if (solve.getL().size() == h.getSolve().size()) {
                    for (; i < h.getSolve().size(); ++i) {
                        if (h.getSolve().get(i) != null && !h.getSolve().get(i).equals(solve.get(i))) {
                            break;
                        }
                    }
                }
                if (i == h.getSolve().size()) {
                    return h;
                }
            }
        }
        return null;
    }

    public Hypotese find(Predicate pred, List<Argument> arg) {
        for (Hypotese h : root) {
            if (h.getPredicate().equals(pred)) {

                int i = 0;
                if (arg.size() == h.getSolve().size()) {
                    for (; i < h.getSolve().size(); ++i) {
                        if (h.getSolve().get(i) != null && arg.get(i) != null && !h.getSolve().get(i).equals(arg.get(i).getValue())) {
                            break;
                        }
                    }
                }
                if (i == h.getSolve().size()) {
                    return h;
                }
            }
        }
        return null;
    }

    public List<Hypotese> getRoot() {
        return root;
    }

    public void reset() {
        if (enableStore) {
            root = null;
        }
    }

    public int size() {
        return root == null ? 0 : root.size();
    }

    public void pack() {
        if (root != null) {
            List<Hypotese> temp = new ArrayList<>();
            for (Hypotese h : root) {
                if (!h.isDeleted()) {
                    temp.add(h);
                }
            }
            root = temp;
        }
    }
}
