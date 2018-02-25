package kanger.primitives;

import kanger.Mind;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    private Predicate next = null;          // Следующий предикат

    private Mind mind = null;

    public Predicate(Mind mind) {
        this.mind = mind;
    }

    public Predicate(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        name = dis.readUTF();
        range = dis.readInt();
        this.mind = mind;
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
    }

    public List<Domain> getSolves() {
        List<Domain> list = new ArrayList<>();
        for(Domain d = mind.getDomains().getRoot(); d != null; d = d.getNext()) {
            if(this.equals(d.getPredicate())) {
                list.add(d);
            }
        }
        return list;
    }

    public String toString() {
        return name + "(" + range + ")";
    }

    @Override
    public boolean equals(Object t) {
        return !(t == null || !(t instanceof Predicate)) && ((Predicate) t).id == id;
    }
}
