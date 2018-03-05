package kanger.factory;

import kanger.Mind;
import kanger.enums.Enums;
import kanger.primitives.TValue;
import kanger.primitives.Term;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * Created by murray on 25.05.15.
 */
public class TValueFactory {

    private TValue root = null;
    private long current = -1;
    private long lastID = 0;

    private Stack<Object[]> stack = new Stack<>();

    private Mind mind = null;

    public TValueFactory(Mind mind) {
        this.mind = mind;
        reset();
    }

    public TValue add(Term o) {
        TValue t = find(o);
        if (t != null) {
            return t;
        } else {
            t = new TValue(o, mind);
            t.setNext(root);
            root = t;
            t.setId(lastID++);
            return t;
        }
    }

    public TValue get() {
        if (root == null) {
            return null;
        }
        if (current == -1) {
            current = root.getId();
        }
        TValue v = get(current);
        if (v == null) {
            v = root;
            current = root.getId();
        }
        return v;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean rewind() {
        if (root == null) {
            return false;
        }
        current = root.getId();
        return true;
    }

    public boolean next() {
        if (root == null) {
            return false;
        }
        TValue v = get(current);
        if (v == null) {
            current = root.getId();
            return true;
        } else if (v.getNext() != null) {
            current = v.getNext().getId();
            return true;
        } else {
            return false;
        }
    }

    public TValue find(Term v) {
        for (TValue t = root; t != null; t = t.getNext()) {
            if (t.getValue().getId() == v.getId()) {
                return t;
            }
        }
        return null;
    }

    public TValue get(long id) {
        for (TValue t = root; t != null; t = t.getNext()) {
            if (id == t.getId()) {
                return t;
            }
        }
        return null;
    }

    public TValue getRoot() {
        return root;
    }

    public void setRoot(TValue o) {
        root = o;
    }

    public void reset() {
        while(stack.size() > 1) {
            stack.pop();
        }
        release();
    }

    public void clear() {
        root = null;
        lastID = 0;
        stack.clear();
        mark();
    }

    public void mark() {
        stack.push(new Object[]{root, lastID});
    }


    public void commit() {
        stack.clear();
        mark();
    }

    public void release() {
        if (!stack.empty()) {
            Object[] pop = stack.pop();
            TValue saved = (TValue) pop[0];
            lastID = (long) pop[1];
            if (root != null && saved != null && root.getId() != saved.getId()) {
                for (TValue t = root; t != null; t = t.getNext()) {
                    if (t.getNext() != null && t.getNext().getId() == saved.getId()) {
                        t.setNext(null);
                        break;
                    }
                }
            }
            root = saved;
        }
        if(stack.isEmpty()) {
            mark();
        }
    }

    public int size() {
        int cnt = 0;
        for (TValue q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(lastID);
        int count = size();
        dos.writeInt(count);
        for (TValue d = root; d != null; d = d.getNext()) {
            d.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException, ClassNotFoundException {
        clear();
        lastID = dis.readLong();
        int count = dis.readInt();
        TValue a = null, b;
        while (count-- > 0) {
            b = new TValue(dis, mind);
            if (a != null) {
                a.setNext(b);
            } else {
                root = b;
            }
            a = b;
        }
    }

}
