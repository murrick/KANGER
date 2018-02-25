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

    private List<Domain> sequence = new ArrayList<>();            // Домены
    private boolean closed = false;
    private boolean used = false;

    public Tree() {
    }

    public Tree(DataInputStream dis, Mind mind) throws IOException {
        closed = (dis.readInt() & 0x0002) != 0;
        int count = dis.readInt();
        while(count-- > 0) {
            sequence.add(mind.getDomains().get(dis.readLong()));
        }
    }

    public List<Domain> getSequence() {
        return sequence;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Tree clone() {
        Tree t = new Tree();
        t.closed = false;
        t.sequence.addAll(sequence);
        return t;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        int flags = (closed ? 0x0002 : 0);
        dos.writeInt(flags);
        dos.writeInt(sequence.size());
        for (Domain d : sequence) {
            dos.writeLong(d.getId());
        }
    }

}
