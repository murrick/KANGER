package kanger.factory;

import kanger.Mind;
import kanger.primitives.Right;
import kanger.primitives.Tree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by murray on 25.05.15.
 */
public class RightFactory {

    private Right root = null;
    private Right saved = null;
    private Right current = null;
    private int cRight = 0, lastRight = 0;

    private Mind mind = null;

    public RightFactory(Mind mind) {
        this.mind = mind;
    }

    public Right add() {
        Right p = new Right();
        p.setRd(++cRight);
        p.setNext(root);
        root = p;
        current = p;
        return p;
    }

    public Right get(long rd) {
        for (Right r = root; r != null; r = r.getNext()) {
            if (r.getRd() == rd) {
                return r;
            }
        }
        return null;
    }

    public int get() {
        return lastRight;
    }

    public Right getRoot() {
        return root;
    }

    public void setRoot(Right o) {
        root = o;
    }

    public Right getCurrent() {
        return current;
    }

    public Right setCurrent(Right c) {
        current = c;
        return current;
//        for(Right r = root; r != null; r = r.getNext()) {
//            if(r.getRd() == (Long)objects[0]) {
//                current = r;
//                return r;
//            }
//        }
//        return null;
    }

    public void reset() {
        root = null;
        saved = null;
        current = null;
        cRight = 0;
        lastRight = 0;
    }

//    public void init() {
//        for (Right r = root; r != null; r = r.getNext()) {
//            for (Tree t = r.getT(); t != null; t = t.getRight()) {
//                for (Tree u = t; u != null; u = u.getDown()) {
//                    u.setCuted(0);
//                    u.setClosed(false);
//                }
//            }
//        }
//    }

    public void mark() {
        lastRight = cRight;
        saved = root;
    }

    public void release() {
        root = saved;
        cRight = lastRight;
    }

    public int size() {
        int cnt = 0;
        for (Right q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeInt(cRight);
        dos.writeInt(size());
        for (Right r = root; r != null; r = r.getNext()) {
            r.writeCompiledData(dos);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException {
        cRight = dis.readInt();
        int count = dis.readInt();
        root = null;
        Right a = null, b;
        while (count-- > 0) {
            b = new Right(dis, mind);
            if (a == null) {
                root = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
    }

}
