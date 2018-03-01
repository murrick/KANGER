package kanger.factory;

import kanger.Mind;
import kanger.enums.Enums;
import kanger.primitives.Term;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by murray on 25.05.15.
 */
public class DictionaryFactory {

    private Term root = null;
    private Term saved = null;
    private long lastID = 0, saveLastID;
    private int cCvar = 0, saveCcvar;           // Счетчик C-переменных

    private Mind mind = null;

    public DictionaryFactory(Mind mind) {
        this.mind = mind;
    }

    public Term add(Object o) {
        Term p = find(o);
        if (p != null) {
            return p;
        } else {
            p = new Term(o, mind);
            p.setNext(root);
            root = p;
            p.setRight(mind.getRights().getRoot());
            p.setId(lastID++);
            return p;
        }
    }

    public Term find(Object o) {
        Term t = new Term(o, mind);
        for (Term dic = root; dic != null; dic = dic.getNext()) {
            if (dic.compareTo(t) == 0) {
                return dic;
            }
        }
        return null;
    }

    public Term get(String name) {
        String temp = String.format("%c%d", Enums.CVC, ++cCvar);
        Term t = add(temp);
        t.setName(name);
        return t;
    }

    public Term get(long id) {
        for (Term dic = root; dic != null; dic = dic.getNext()) {
            if (id == dic.getId()) {
                return dic;
            }
        }
        return null;
    }

    public Term getRoot() {
        return root;
    }

    public void setRoot(Term o) {
        root = o;
    }

    public void reset() {
        root = null;
        saved = null;
        lastID = 0;
        saveLastID = 0;
        cCvar = 0;
        saveCcvar = 0;
    }

    public void mark() {
        saved = root;
        saveCcvar = cCvar;
        saveLastID = lastID;
    }

    public void release() {
        if(root != null && saved != null && root.getId() != saved.getId()) {
            for (Term t = root; t != null; t = t.getNext()) {
                if(t.getNext() != null && t.getNext().getId() == saved.getId()) {
                    t.setNext(null);
                    break;
                }
            }
        }
        root = saved;
        cCvar = saveCcvar;
        lastID = saveLastID;
    }

    public int size() {
        int cnt = 0;
        for (Term q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(lastID);
        dos.writeInt(cCvar);
        int count = size();
        dos.writeInt(count);
        for (Term d = root; d != null; d = d.getNext()) {
            d.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException, ClassNotFoundException {
        lastID = dis.readLong();
        cCvar = dis.readInt();
        int count = dis.readInt();
        root = null;
        saved = null;
        Term a = null, b;
        while (count-- > 0) {
            b = new Term(dis, mind);
            if (a != null) {
                a.setNext(b);
            } else {
                root = b;
            }
            a = b;
        }
    }

}
