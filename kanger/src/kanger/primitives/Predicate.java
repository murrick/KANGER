package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 *
 * Описание предиката. Голова предиката ссылается на список решений для него.
 * Список решений состоит из строк со значениями параметров. Флажок cuted служит
 * для отмены какого-либо решения. Собственно говоря предикат со списком решений
 * является единицей базы данных.
 */
public class Predicate {

    private String name = "";               // Имя предиката
    private int range = 0;                  // К-во параметров
    private long id = -1;                   // Идентификатор
    private Solve solve = null;             // Список решений
    private Solve dummy = null;             // Временная база
    private Solve hypo = null;              // Гипотезы
//    private Solve saveHypo = null;          // Сохранение гипотез
//    private Solve backupHypo = null;
    private Predicate next = null;                  // Следующий предикат

    public Predicate() {
    }

    public Predicate(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        name = dis.readUTF();
        range = dis.readInt();
        int count = dis.readInt();
        Solve a = null, b = null;
        while (count-- > 0) {
            b = new Solve(dis, mind);
            b.setPredicate(this);
            if (a == null) {
                solve = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Solve getSolve() {
        return solve;
    }

    public void setSolve(Solve slove) {
        this.solve = slove;
    }

    public Solve getDummy() {
        return dummy;
    }

    public void setDummy(Solve dummy) {
        this.dummy = dummy;
    }

    public Solve getHypo() {
        return hypo;
    }

    public void setHypo(Solve hypo) {
        this.hypo = hypo;
    }

    public Solve getSolve(long id) {
        for(Solve s = solve; s!=null; s=s.getNext()) {
            if(s.getId() == id) {
                return s;
            }
        }
        return null;
    }

//    public Solve getSaveHypo() {
//        return saveHypo;
//    }
//
//    public void setSaveHypo(Solve saveHypo) {
//        this.saveHypo = saveHypo;
//    }
//
//    public Solve getBackupHypo() {
//        return backupHypo;
//    }
//
//    public void setBackupHypo(Solve backupHypo) {
//        this.backupHypo = backupHypo;
//    }
//
    public Predicate getNext() {
        return next;
    }

    public void setNext(Predicate next) {
        this.next = next;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeUTF(name);
        dos.writeInt(range);
        int count = 0;
        for (Solve s = solve; s != null; s = s.getNext()) {
            ++count;
        }
        dos.writeInt(count);
        for (Solve s = solve; s != null; s = s.getNext()) {
            s.writeCompiledData(dos);
        }
    }

    public Solve deleteSolve(List target) {
        Solve c = null;
        Solve ret = null;
        for (Solve s = solve; s != null; s = s.getNext()) {
            if (/*s.isAntc() && */s.equals(target)) {
                ret = s;
                if (c == null) {
                    solve = s.getNext();
                } else {
                    c.setNext(s.getNext());
                }
            } else {
                c = s;
            }
        }
        return ret;
    }

    public Solve addSolve() {
        long id = solve == null ? 1 : solve.getId() + 1;
        Solve s = new Solve();
        s.setPredicate(this);
        s.setId(id);
        s.setNext(solve);
        solve = s;
        return s;
    }

    public Solve addHypo() {
        long id = hypo == null ? 1 : hypo.getId() + 1;
        Solve s = new Solve();
        s.setPredicate(this);
        s.setId(id);
        s.setNext(hypo);
        hypo = s;
        return s;
    }

    public String toString() {
        return name + "(" + range + ")";
    }

    @Override
    public boolean equals(Object t) {
        return !(t == null || !(t instanceof Predicate)) && ((Predicate) t).id == id;
    }
}
