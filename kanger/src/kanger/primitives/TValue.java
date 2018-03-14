package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by murray on 13.12.16.
 */
public class TValue {

    private long id = -1;                   // Идентификатор значения переменной
    private Term value = null;
    private TVariable tVar = null;
    private Domain srcSolve = null;
    private Domain dstSolve = null;

    private Right right = null;             // Ссылка на правило
    private TValue next = null;          // Следующая переменная

    private Mind mind = null;


    public TValue(Mind mind) {
        this.mind = mind;
    }

    public TValue(TVariable tv, Term t, Mind mind) {
        this.mind = mind;
        this.tVar = tv;
        this.value = t;
    }

    public TValue(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        tVar = mind.getTVars().get(dis.readLong());
        value = mind.getTerms().get(dis.readLong());
        long sid = dis.readLong();
        if(sid != -1) {
            srcSolve = mind.getDomains().get(sid);
        }
        sid = dis.readLong();
        if(sid != -1) {
            dstSolve = mind.getDomains().get(sid);
        }
        this.mind = mind;
    }

    public Term getValue() {
        return value;
    }

    public void setValue(Term value) {
        this.value = value;
    }

    public Domain getSrcSolve() {
        return srcSolve;
    }

    public void setSrcSolve(Domain srcSolve) {
        this.srcSolve = srcSolve;
    }

    public Domain getDstSolve() {
        return dstSolve;
    }

    public void setDstSolve(Domain dstSolve) {
        this.dstSolve = dstSolve;
    }

    public Right getRight() {
        return right;
    }

    public void setRight(Right right) {
        this.right = right;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TVariable getTVar() {
        return tVar;
    }

    public void setTVar(TVariable tVar) {
        this.tVar = tVar;
    }

    public TValue getNext() {
        return next;
    }

    public void setNext(TValue next) {
        this.next = next;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeLong(tVar.getId());
        dos.writeLong(value == null ? -1 : value.getId());
        dos.writeLong(srcSolve == null ? -1 : srcSolve.getId());
        dos.writeLong(dstSolve == null ? -1 : dstSolve.getId());
    }

}
