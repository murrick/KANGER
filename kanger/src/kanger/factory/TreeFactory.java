package kanger.factory;

import kanger.Mind;
import kanger.primitives.Tree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by murray on 25.05.15.
 */
public class TreeFactory {

    private Tree root = null;
    private Tree saved = null;
    private long lastID = 0, saveLastID;

    private Mind mind = null;

    public TreeFactory(Mind mind) {
        this.mind = mind;
    }

    public Tree add() {
        Tree p = new Tree(mind);
        p.setId(++lastID);
        p.setRight(mind.getRights().getRoot());
        p.setNext(root);
        root = p;
        return p;
    }

    public Tree get(long id) {
        for (Tree r = root; r != null; r = r.getNext()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public Tree getRoot() {
        return root;
    }

    public void setRoot(Tree o) {
        root = o;
    }

    public void reset() {
        root = null;
        saved = null;
        lastID = 0;
        saveLastID = 0;
    }

    public void mark() {
        saved = root;
        saveLastID = lastID;
    }

    public void release() {
        if(root != null && saved != null && root.getId() != saved.getId()) {
            for (Tree t = root; t != null; t = t.getNext()) {
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
        for (Tree q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(lastID);
        dos.writeInt(size());
        for (Tree r = root; r != null; r = r.getNext()) {
            r.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException {
        lastID = dis.readLong();
        int count = dis.readInt();
        root = null;
        Tree a = null, b;
        while (count-- > 0) {
            b = new Tree(dis, mind);
            if (a == null) {
                root = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
    }

}
