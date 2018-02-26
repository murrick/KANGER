package kanger.primitives;

import java.io.*;
import java.util.*;
import kanger.*;
import kanger.enums.*;
import kanger.exception.*;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 * <p>
 * Элемент подстановочной переменной
 */
public class TVariable {
//    private String name = "";               // #-имя, или подстановочное имя

    private String name = "";               // Оригинальное подкванторное имя
    private Term area = null;               // Ссылка на область определения

//    private Term value = null;            // Текущее подставленное значение
//    private int owner = 0;                  // level подставившего значение
//    private Solve s = null;                 // Текущее подставленное решение

    private Right right = null;             // Ссылка на правило
    private long id = -1;                   // Идентификатор переменной
    //    private Predicate p = null;             // Предикат в котором произошла подстановка
    private TVariable next = null;          // Следующая переменная

    private Mind mind = null;

    //    public String getName() {
//        return String.format("%c%d", Enums.TVC, id);
//    }
//    public void setName(String name) {
//        this.name = name;
//    }
    public TVariable(Mind mind) {
        this.mind = mind;
    }

    public TVariable(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        mind.gettVariableLinks().put(this, dis.readLong());
//        owner = dis.readInt();
        long did = dis.readLong();
        if (did != -1) {
            area = mind.getTerms().get(did);
        } else {
            area = null;
        }
        did = dis.readLong();
        right = mind.getRights().get(did);
//        if (did != -1) {
//            value = mind.getTerms().get(did);
//        } else {
//            value = null;
//        }
        name = dis.readUTF();
        this.mind = mind;
    }

    public String getName() {
        return name;
    }

    public void setName(String tName) {
        this.name = tName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Term getArea() {
        return area;
    }

    public void setArea(Term area) {
        this.area = area;
    }

    public Term getValue() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getValue();
        } else {
            return null;
        }
    }

    public void setValue(Term value) throws TValueOutOfOrver {
        if (!mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        }
        if (value != null) {
            if (!isInside(value)) {
                mind.getTValues().get(this).addValue(value);
//                setOwner(mind.getCurrentLevel());
//                setSolve(mind.getCurrentSolve());
                mind.incSubstCount();
            } else {
                throw new TValueOutOfOrver(value.toString());
            }
        } else if (value == null) {
            mind.getTValues().remove(this);
        }
    }

//    public int getOwner() {
//        if (mind.getTValues().containsKey(this)) {
//            return mind.getTValues().get(this).getLevel();
//        } else {
//            return 0;
//        }
//    }
//
//    public void setOwner(int owner) {
//        if (!mind.getTValues().containsKey(this)) {
//            mind.getTValues().put(this, new TValue());
//        }
//        mind.getTValues().get(this).setLevel(owner);
//
//    }

    public Right getRight() {
        return right;
    }

    public void setRight(Right right) {
        this.right = right;
    }

    public TVariable getNext() {
        return next;
    }

    public void setNext(TVariable next) {
        this.next = next;
    }

    public Domain getSolve() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getSolve();
        } else {
            return null;
        }
    }

    public void setSolve(Domain s) {
        if (!mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        }
        mind.getTValues().get(this).setSolve(s);
    }

    //    public Predicate getPredicate() {
//        return p;
//    }
//
//    public void setPredicate(Predicate p) {
//        this.p = p;
//    }
//
    public String toString() {
        if (/*getOwner() == 0 || */getValue() == null) {
            return String.format("%c%d", Enums.TVC, id);
        } else {
            return String.format("%c%d:%s", Enums.TVC, id, getValue().toString());
        }
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeLong(right.getId());
//        dos.writeInt(owner);
        dos.writeLong(area == null ? -1 : area.getId());
        dos.writeLong(right == null ? -1 : right.getId());
        dos.writeUTF(name);
    }

    @Override
    public boolean equals(Object t) {
        return !(t == null || !(t instanceof TVariable)) && ((TVariable) t).id == id;
    }

    public boolean isInside(Term c) {

        return (c != null
                && c.isCVar()
                && c.getRight() == getRight()
                && (getArea() == null || getArea().getId() < c.getId()));
    }

}
