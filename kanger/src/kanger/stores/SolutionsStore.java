package kanger.stores;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murray on 28.05.15.
 */
public class SolutionsStore {

    private List<String> root = null;
    private boolean enableStore = true;

    public Boolean add(String s) {
        if (!enableStore) {
            return false;
        }
        if (root == null) {
            root = new ArrayList<>();
        }
        if (!root.contains(s)) {
            root.add(s);
            return true;
        }
        return false;
    }

    public void enable(boolean e) {
        enableStore = e;
    }

    public boolean isEnabled() {
        return enableStore;
    }

    public String get(int index) {
        return root.get(index);
    }

    public int find(String o) {
        return root.indexOf(o);
    }

    public Object getRoot() {
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
}
