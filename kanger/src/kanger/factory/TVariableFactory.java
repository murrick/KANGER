package kanger.factory;

import kanger.Mind;
import kanger.primitives.TVariable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by murray on 25.05.15.
 */
public class TVariableFactory {

    private TVariable root = null;
    private TVariable saved = null;
    private int cTvar = 0, saveCtvar;            /* Счетчик T-переменных */

    private Mind mind = null;

    public TVariableFactory(Mind mind) {
        this.mind = mind;
    }

    public TVariable add() {
        TVariable p = new TVariable(mind);
        p.setId(++cTvar);
        p.setOwner(0);
        p.setS(null);
        p.setArea(mind.getTerms().getRoot());
//        p.setValue(null);
        p.setRight(mind.getRights().getRoot());
        p.setNext(root);
        root = p;
        return p;
    }

    public TVariable get(long id) {
        for (TVariable t = root; t != null; t = t.getNext()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public TVariable getRoot() {
        return root;
    }

    public void setRoot(TVariable o) {
        root = o;
    }

    public void reset() {
        root = null;
        saved = null;
        cTvar = 0;
    }

    public void init() {
        for (TVariable v = root; v != null; v = v.getNext()) {
            v.setOwner(0);
            v.setS(null);
        }
    }

    public void mark() {
        saved = root;
        saveCtvar = cTvar;
    }

    public void release() {
        root = saved;
        cTvar = saveCtvar;
    }

    public int size() {
        int cnt = 0;
        for (TVariable q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeInt(cTvar);
        int sz = size();
        dos.writeInt(sz);
        for (TVariable t = root; t != null; t = t.getNext()) {
            t.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException {
        cTvar = dis.readInt();
        int count = dis.readInt();
        TVariable a = null, b;
        root = null;
        while (count-- > 0) {
            b = new TVariable(dis, mind);
            if (a == null) {
                root = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
    }

}
