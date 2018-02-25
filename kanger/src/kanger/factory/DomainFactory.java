package kanger.factory;

import kanger.Mind;
import kanger.primitives.Argument;
import kanger.primitives.Domain;
import kanger.primitives.Predicate;
import kanger.primitives.Right;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by murray on 25.05.15.
 */
public class DomainFactory {

    private Domain root = null;
    private Domain saved = null;
    private long lastID = 0, saveLastID;

    private Mind mind = null;

    public DomainFactory(Mind mind) {
        this.mind = mind;
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
        saved = null;
        lastID = 0;
        saveLastID = 0;
    }

    public void init() {
        for (Domain d = root; d != null; d = d.getNext()) {
            d.setUsed(false);
//            d.setLoged(false);
        }
    }

    public void mark() {
        saved = root;
        saveLastID = lastID;
    }

    public void release() {
        if(root != null && saved != null && root.getId() != saved.getId()) {
            for (Domain t = root; t != null; t = t.getNext()) {
                if(t.getNext() != null && t.getNext().getId() == saved.getId()) {
                    t.setNext(null);
                    break;
                }
            }
            root = saved;
            lastID = saveLastID;
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
