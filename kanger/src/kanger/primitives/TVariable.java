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

    public TSubst setValue(Term value) throws TValueOutOfOrver {
        if (!mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        }
        if (!isInside(value)) {
            if (mind.getTValues().get(this).contains(value) == -1) {
                mind.getSubstituted().add(id);
            }
            return mind.getTValues().get(this).setValue(value);
        } else {
            throw new TValueOutOfOrver(value.toString());
        }
    }

    public void delValue(Domain d) {
        if (mind.getTValues().containsKey(this)) {
            mind.getTValues().get(this).delValue(d);
            mind.getSubstituted().add(id);
        }
    }

    public TSubst addValue(Term value) throws TValueOutOfOrver {
        if (!mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        }
        if (!isInside(value)) {
            if (mind.getTValues().get(this).contains(value) == -1) {
                mind.getSubstituted().add(id);
            }
            return mind.getTValues().get(this).addValue(value);
        } else {
            throw new TValueOutOfOrver(value.toString());
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

    public boolean isSuccess() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).isSuccess();
        } else {
            return false;
        }
    }

    public void setSuccess(boolean on) {
        if (!mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        }
        mind.getTValues().get(this).setSuccess(on);
    }

    public Domain getSrcSolve() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getSrcSolve();
        } else {
            return null;
        }
    }

    public void setSrcSolve(Domain d) {
        if (mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        } else {
            mind.getTValues().get(this).setSrcSolve(d);
        }
    }

    public Domain getSrcValue() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getSrcValue();
        } else {
            return null;
        }
    }

    public Domain getDstSolve() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getDstSolve();
        } else {
            return null;
        }
    }

    public void setDstSolve(Domain d) {
        if (mind.getTValues().containsKey(this)) {
            mind.getTValues().put(this, new TValue());
        } else {
            mind.getTValues().get(this).setDstSolve(d);
        }
    }

    public Domain getDstValue() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).getDstValue();
        } else {
            return null;
        }
    }

    public boolean isDestFor(Domain d) {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).isDestFor(d);
        } else {
            return false;
        }
    }

    //    public Predicate getPredicate() {
//        return p;
//    }
//
//    public void setPredicate(Predicate p) {
//        this.p = p;
//    }
//
    @Override
    public String toString() {
        String v = ((mind.getDebugLevel() &  Enums.DEBUG_OPTION_VALUES) != 0) ? (isEmpty() ? "" : ":" + getValue().toString()) : "";
        switch (mind.getDebugLevel() & 0x00FF) {
            case Enums.DEBUG_LEVEL_INFO:
                return name + v;
            case Enums.DEBUG_LEVEL_DEBUG:
                return String.format("%c%d%s", Enums.TVC, id, v);
            default:
                return name;
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

    public boolean contains(Term value) {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).contains(value) >= 0;
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        if (mind.getTValues().containsKey(this)) {
            return mind.getTValues().get(this).size() == 0;
        } else {
            return true;
        }
    }

    public boolean rewind() {
        if (mind.getTValues().containsKey(this)) {
            if (mind.getTValues().get(this).size() > 0) {
                mind.getTValues().get(this).setCurrent(0);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean next() {
        if (mind.getTValues().containsKey(this)) {
            if (mind.getTValues().get(this).getCurrent() + 1 < mind.getTValues().get(this).size()) {
                mind.getTValues().get(this).setCurrent(mind.getTValues().get(this).getCurrent() + 1);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public List<TSubst> getQueued() {
        List<TSubst> list = new ArrayList<>();
        if (mind.getTValues().containsKey(this) && mind.getTValues().get(this).getCurrent() + 1 < mind.getTValues().get(this).size()) {
            for (int i = mind.getTValues().get(this).getCurrent() + 1; i < mind.getTValues().get(this).size(); ++i) {
                list.add(mind.getTValues().get(this).get(i));
            }
        }
        return list;
    }

    public Set<Domain> getUsage() {
        Set<Domain> set = new HashSet<>();
        for (Domain d = mind.getDomains().getRoot(); d != null; d = d.getNext()) {
            if (d.contains(this)) {
                set.add(d);
            }
        }
        return set;

    }
}
