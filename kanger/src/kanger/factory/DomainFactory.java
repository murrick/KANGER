package kanger.factory;

import kanger.Mind;
import kanger.primitives.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * Created by murray on 25.05.15.
 */
public class DomainFactory {

    private Domain root = null;
    private long lastID = 0;

    private Stack<Object[]> stack = new Stack<>();

    private Mind mind = null;

    public DomainFactory(Mind mind) {
        this.mind = mind;
    }

    public Domain add(Right r) {
        Domain p = new Domain(mind);
        p.setNext(root);
        p.setRight(r);
        p.setId(lastID++);
        root = p;
        return p;
    }


    public Domain add(Predicate pred, boolean antc, List<Argument> arg, Right r) {
        Domain p = find(pred, antc, arg);
        if (p != null) {
            return p;
        } else {
            p = new Domain(mind);
            p.setNext(root);
            p.setPredicate(pred);
            p.setAntc(antc);
            p.setRight(r);
            p.setId(lastID++);
            if (arg != null) {
                for (Argument t : arg) {
                    p.add(t);
                }
            }
            root = p;
            return p;
        }
    }

    public Domain find(Predicate pred, boolean antc, List<Argument> arg) {
        for (Domain p = root; p != null; p = p.getNext()) {
            if (p.isAntc() == antc && p.getPredicate() == pred && p.getPredicate().getRange() == pred.getRange() && !p.getArguments().isEmpty()) {
                int i = 0;
                for (; i < pred.getRange(); ++i) {
                    if (p.get(i).getValue() != arg.get(i).getValue() || p.get(i).getT() != arg.get(i).getT() || p.get(i).getF() != arg.get(i).getF()) {/*??? .f*/

                        break;
                    }
                }
                if (i == pred.getRange()) {
                    return p;
                }
                //return p;
            }
        }
        return null;
    }

    public Domain get(long id) {
        for (Domain p = root; p != null; p = p.getNext()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public Domain getRoot() {
        return root;
    }

    public void setRoot(Domain o) {
        root = o;
    }

    public void reset() {
        root = null;
        lastID = 0;
        stack.clear();
    }

    public void mark() {
        stack.push(new Object[]{root, lastID});
    }

    public void commit() {
        stack.pop();
    }

    public void release() {
        if(!stack.empty()) {
            Object[] pop = stack.pop();
            Domain saved = (Domain) pop[0];
            lastID = (long) pop[1];
            if (root != null && saved != null && root.getId() != saved.getId()) {
                for (Domain t = root; t != null; t = t.getNext()) {
                    if (t.getNext() != null && t.getNext().getId() == saved.getId()) {
                        t.setNext(null);
                        break;
                    }
                }
            }
        }
    }

    public int size() {
        int cnt = 0;
        for (Domain q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(lastID);
        dos.writeInt(size());
        for (Domain d = root; d != null; d = d.getNext()) {
            d.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException {
        lastID = dis.readLong();
        int count = dis.readInt();
        root = null;
        Domain a = null, b;
        while (count-- > 0) {
            b = new Domain(dis, mind);
            if (a == null) {
                root = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
    }

}
