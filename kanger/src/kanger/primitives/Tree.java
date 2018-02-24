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

    private Domain d = null;        	// Домен
    private boolean closed = false;   	//
    private Tree next = null;      		// Вп
	private Object source = null;		// Указатель на PTree в процессе компиляции

    public Tree() {
    }

    public Tree(DataInputStream dis, Mind mind) throws IOException {
        int flags = dis.readInt();
        closed = (flags & 0x0002) != 0;
        long id = dis.readLong();
        d = (Domain) mind.getDomains().get(id);
    }

    public Domain getD() {
        return d;
    }

    public void setD(Domain d) {
        this.d = d;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Tree getNext() {
        return next;
    }

    public void setNext(Tree right) {
        this.next = right;
    }
	
	public Tree clone(Right r, Object source) {
		Tree t = new Tree();
		t.closed = false;
		t.d = d;
		t.source = source;
		t.setNext(r.getTree());
		r.setTree(t);
		return t;
	}

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        int flags = (closed ? 0x0002 : 0);
        dos.writeInt(flags);
        dos.writeLong(d.getId());
    }

}
