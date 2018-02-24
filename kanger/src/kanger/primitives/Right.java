package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 *
 * Список правил
 */
public class Right {

    private List<Tree> tree = new ArrayList<>();      // Ссылка на дерево правила
    private long id = -1;                       // ID Правила
    private Right next = null;                  // Следующее правило
    private String orig = "";                   // Оригинальная строка


    public Right() {
    }

    public Right(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        orig = dis.readUTF();
        int count = dis.readInt();
        while (count-- > 0) {
			tree.add(new Tree(dis, mind));
        }
    }

    public List<Tree> getTree() {
        return tree;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Right getNext() {
        return next;
    }

    public void setNext(Right next) {
        this.next = next;
    }

    public String getOrig() {
        return orig;
    }

    public void setOrig(String orig) {
        this.orig = orig;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeUTF(orig);
        dos.writeInt(tree.size());
        for (Tree r : tree) {
			r.writeCompiledData(dos);

        }
    }

    public int size() {
        return tree.size();
    }

    public Tree cloneTree(Tree t) {
        Tree x = t.clone();
        tree.add(x);
        return x;
    }
	
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || !(o instanceof Right)) {
//            return false;
//        } else {
//            Right r = (Right) o;
//            if (r.size() != size()) {
//                return false;
//            }
//            for () {
//				if (!h1.getD().equals(h2.getD())) {
//					return false;
//				}
//
//            }
//            return true;
//        }
//    }
}
