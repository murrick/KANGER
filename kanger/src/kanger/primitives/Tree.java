package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 * <p>
 * Элемент ветви дерева
 */
public class Tree {

    private List<Domain> sequence = new ArrayList<>();          // Домены
    private long id = -1;                                       // Идентификатор
    private Tree next = null;

    private Mind mind = null;

//    private boolean closed = false;
//    private boolean used = false;

    public Tree(Mind mind) {
        this.mind = mind;
    }

    public Tree(DataInputStream dis, Mind mind) throws IOException {
        this.mind = mind;
        id = dis.readLong();
        int count = dis.readInt();
        while(count-- > 0) {
            sequence.add(mind.getDomains().get(dis.readLong()));
        }
    }

    public List<Domain> getSequence() {
        return sequence;
    }

    public boolean isClosed() {
        return mind.getClosedTrees().contains(id);
    }

    public void setClosed(boolean closed) {
        if(closed) {
            mind.getClosedTrees().add(id);
        } else {
            mind.getClosedTrees().remove(id);
        }
    }

    public boolean isUsed() {
        return mind.getUsedTrees().contains(id);
    }

    public void setUsed(boolean used) {
        if(used) {
            mind.getUsedTrees().add(id);
        } else {
            mind.getUsedTrees().remove(id);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Tree getNext() {
        return next;
    }

    public void setNext(Tree next) {
        this.next = next;
    }

    public Tree clone() {
        Tree t = mind.getTrees().add();
        t.sequence.addAll(sequence);
        return t;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeInt(sequence.size());
        for (Domain d : sequence) {
            dos.writeLong(d.getId());
        }
    }

}
