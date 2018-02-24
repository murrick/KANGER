package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 *
 * Список правил
 */
public class Right {

    private Tree t = null;              // Ссылка на дерево правила
//    private int pos = 0;                // Editor line reference
    private long rd = -1;               // ID Правила
    private boolean cst = false;        // Правило содержит константы
    private Right next = null;          // Следующее правило
    private String orig = "";           // Оригинальная строка


    public Right() {
    }

    public Right(DataInputStream dis, Mind mind) throws IOException {
        rd = dis.readLong();
//        pos = dis.readInt();
        int flags = dis.readInt();
        cst = (flags & 0x0001) != 0;
        orig = dis.readUTF();
        int width = dis.readInt();
        t = null;
        Tree a = null, b = null;
        while (width-- > 0) {
			Tree d = new Tree(dis, mind);
			d.setNext(t);
			t = d;
        }
    }

    public Tree getT() {
        return t;
    }

    public void setT(Tree t) {
        this.t = t;
    }

    public long getRd() {
        return rd;
    }

    public void setRd(long rd) {
        this.rd = rd;
    }

    public boolean isCst() {
        return cst;
    }

    public void setCst(boolean cst) {
        this.cst = cst;
    }

    public Right getNext() {
        return next;
    }

    public void setNext(Right next) {
        this.next = next;
    }

//    public int getPos() {
//        return pos;
//    }
//
//    public void setPos(int pos) {
//        this.pos = pos;
//    }
//
    public String getOrig() {
        return orig;
    }

    public void setOrig(String orig) {
        this.orig = orig;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(rd);
//        dos.writeInt(pos);
        dos.writeInt(cst ? 0x0001 : 0);
        dos.writeUTF(orig);

        int width = 0;
        for (Tree r = t; r != null; r = r.getNext()) {
            ++width;
        }
        dos.writeInt(width);
        for (Tree r = t; r != null; r = r.getNext()) {
			r.writeCompiledData(dos);

        }
    }

    public int size() {
        int cnt = 0;
        for (Tree x = t; x != null; x = x.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Right)) {
            return false;
        } else {
            Right r = (Right) o;
            if (r.size() != size()) {
                return false;
            }
            for (Tree h1 = t, h2 = r.t; h1 != null && h2 != null; h1 = h1.getRight(), h2 = h2.getRight()) {
                for (Tree v1 = h1, v2 = h2; v1 != null && v2 != null; v1 = v1.getDown(), v2 = v2.getDown()) {
                    if (!v1.getD().equals(v2.getD())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
