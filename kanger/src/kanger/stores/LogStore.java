package kanger.stores;

import kanger.Screen;
import kanger.primitives.LogEntry;
import kanger.primitives.Tree;
import kanger.enums.LogMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by murray on 28.05.15.
 */
public class LogStore {

    private List<LogEntry> root = null;
    private boolean enableLogging = true;

    public LogEntry add(LogMode m, Tree t) {
        if (!enableLogging) {
            return null;
        }
        if (root == null) {
            root = new ArrayList<>();
            root.add(new LogEntry(LogMode.ANALIZER, "LOG START AT " + new Date(System.currentTimeMillis()) + " --"));
        }
        LogEntry log = null;
        List<List<String>> net = Screen.formatTree(t);
        for (int i = 0; i < net.get(0).size(); ++i) {
            String s = "";
            for (int k = 0; k < net.size(); ++k) {
                s += net.get(k).get(i);
                if (k + 1 < net.size()) {
                    s += " ";
                }
            }
            log = new LogEntry(m, s);
            root.add(log);
        }
        return log;
    }

    public LogEntry add(LogMode m, String s) {
        if (!enableLogging) {
            return null;
        }
        if (root == null) {
            root = new ArrayList<>();
            root.add(new LogEntry(LogMode.ANALIZER, "LOG START AT " + new Date(System.currentTimeMillis()) + " --"));
        }
        LogEntry log = null;
        log = new LogEntry(m, s);
        root.add(log);
        return log;
    }

    public void enable(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public boolean isEnabled() {
        return enableLogging;
    }

    public LogEntry get(int index) {
        return root.get(index);
    }

    public Object find(Object... objects) {
        return root.indexOf(objects[0]);
    }

    public List<LogEntry> getRoot() {
        return root;
    }

    public LogEntry getCurrent() {
        if (root == null || root.size() == 0) {
            return null;
        } else {
            return root.get(root.size() - 1);
        }
    }

    public void reset() {
        if (enableLogging) {
            root = null;
        }
    }

    public int size() {
        return root == null ? 0 : root.size();
    }

}
