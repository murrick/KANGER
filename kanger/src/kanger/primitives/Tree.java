package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 * <p>
 * Элемент ветви дерева
 */
public class Tree {

    private Domain d = null;        // Домен
    //    private int cuted = 0;          // Признак закрытия ветки
//    private boolean closed = false; //
    private boolean used = false;   //
    private Tree right = null;      // Вправо
    private Tree down = null;       // Вниз

    public Tree() {
    }

    public Tree(DataInputStream dis, Mind mind) throws IOException {
//        cuted = dis.readInt();
        int flags = dis.readInt();
//        closed = (flags & 0x0001) != 0;
        used = (flags & 0x0002) != 0;
        long id = dis.readLong();
        d = (Domain) mind.getDomains().get(id);
    }

    public Domain getD() {
        return d;
    }

    public void setD(Domain d) {
        this.d = d;
    }

//    public boolean isClosed() {
//        return closed;
//    }
//
//    public void setClosed(boolean closed) {
//        this.closed = closed;
//    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Tree getRight() {
        return right;
    }

    public void setRight(Tree right) {
        this.right = right;
    }

    public Tree getDown() {
        return down;
    }

    public void setDown(Tree down) {
        this.down = down;
    }

//    public int getCuted() {
//        return cuted;
//    }
//
//    public void setCuted(int cuted) {
//        this.cuted = cuted;
//    }

    public Tree cloneDown() {
        Tree n = null;
        for (Tree p = this; p != null; p = p.getDown()) {
            Tree x = new Tree();
            x.setD(p.getD());
            x.setDown(n);
            n = x;
        }
        return n;
    }

    public Tree appendDown(Tree nn) {
        Tree n = this;
        while (n.getDown() != null) {
            n = n.getDown();
        }
        n.setDown(nn);
        return this;
    }

    public Tree appendRight(Tree nn) {
        Tree n = this;
        while (n.getRight() != null) {
            n = n.getRight();
        }
        n.setRight(nn);
        return this;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
//        dos.writeInt(cuted);
        int flags = //(closed ? 0x0001 : 0)
//                |
                (used ? 0x0002 : 0);
        dos.writeInt(flags);
        dos.writeLong(d.getId());
    }

}
