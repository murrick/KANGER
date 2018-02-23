package kanger.primitives;

import kanger.Mind;
import kanger.compiler.Operation;
import kanger.compiler.Parser;
import kanger.enums.Enums;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 *
 * Описатель варианта решения предиката
 */
public class Domain {

    private Predicate p = null;                             // Ссылка на описатель предиката
    private List<Argument> l = new ArrayList<>();   // Массив подстановочных переменных
    private boolean antc = true;                            // ! или ?
//    private boolean loged = false;                          // Писать в лог-файл
//    private boolean cst = false;                            // Правило содержит константы
    private int cuted = 0;                                  // Флаг активности
    private Right right;                                   // Ссылка на правило
    private long id = -1;                                   // id домена
    private Domain next = null;                             // Следующий элемент

    public Domain() {
    }

    public Domain(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        mind.getDomainLinks().put(this, dis.readLong());
        cuted = dis.readInt();
        int flags = dis.readInt();
        antc = (flags & 0x0001) != 0;
//        loged = (flags & 0x0002) != 0;
//        cst = (flags & 0x0004) != 0;
        long id = dis.readLong();
        p = (Predicate) mind.getPredicates().get(id);
        int count = dis.readInt();
        l.clear();
        while (count-- > 0) {
            Argument a = new Argument(dis, mind);
            l.add(a);
        }
    }


    public Predicate getP() {
        return p;
    }

    public void setP(Predicate p) {
        this.p = p;
    }

//    public boolean isLoged() {
//        return loged;
//    }
//
//    public void setLoged(boolean loged) {
//        this.loged = loged;
//    }
//
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

//    public boolean isCst() {
//        return cst;
//    }
//
//    public void setCst(boolean cst) {
//        this.cst = cst;
//    }
//
    public Domain getNext() {
        return next;
    }

    public void setNext(Domain next) {
        this.next = next;
    }

    public Argument get(int i) {
        return l.get(i);
    }

    public void add(Argument t) {
        l.add(t);
    }

    public int getCuted() {
        return cuted;
    }

    public void setCuted(int cuted) {
        this.cuted = cuted;
    }

    public List<Argument> getL() {
        return l;
    }

    public boolean isAntc() {
        return antc;
    }

    public void setAntc(boolean antc) {
        this.antc = antc;
    }

//    String s = String.format("%c%s(", d.isAntc() ? Enums.ANT : Enums.SUC, d.getP().getName());
//    int i = 0;
//    for (TList t : d.getL()) {
//        String name = "_";
//        if (t.isCSet()) name = t.getC().toString();
//        else if (t.isFSet() && t.getF().getR() != null)
//            name = t.getF().toString(); // + "=" + t.getF().getR().toString();
//        else if (t.isTSet() && t.getT().getOwner() != 0) name = t.getT().getValue().getTerm().getName();
//        s += name;
//        if (i + 1 != d.getP().getRange()) {
//            s += String.format("%c", Enums.COMMA);
//        }
//        ++i;
//    }
//    s += ");";
//    return s;
    private String formatParam(Argument t) {
        String s = "";
        if (t.isTSet()) {
            s += t.getT().toString();
        } else if (t.isFSet()) {
            s += t.getF().toString();
        } else  {
                s += t.getValue().toString();
        }
        return s;
    }

    public String toString() {
        String s = String.format("%c", antc ? Enums.ANT : Enums.SUC);
        Operation op = Parser.getOp(p.getName());
        if (op == null) {
            s += p.getName() + "(";
            int i = 0;
            for (Argument t : l) {
                s += formatParam(t);
                if (i + 1 != p.getRange()) {
                    s += (char) Enums.COMMA;
                }
                ++i;
            }
            s += ")";
        } else if (op.getRange() == 1) {
            if (op.isPost()) {
                s += formatParam(l.get(0)) + op.getName();
            } else {
                s += op.getName() + formatParam(l.get(0));
            }
        } else {
            try {
                for (int i = 0; i < op.getRange(); ++i) {
                    s += formatParam(l.get(i));
                    if (i + 1 < op.getRange()) {
                        s += " " + op.getName() + " ";
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                System.out.print(ex);
            }
        }
        return s + ";";
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeLong(right.getRd());
        dos.writeInt(cuted);
        int flags = (antc ? 0x0001 : 0);
//                | (loged ? 0x0002 : 0)
//                | (cst ? 0x0004 : 0);
        dos.writeInt(flags);
        dos.writeLong(p.getId());
        dos.writeInt(l.size());
        for (Argument a : l) {
            a.writeCompiledData(dos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Domain)) {
            return false;
        } else {
            Domain d = (Domain) o;
            if (!p.equals(d.p)) {
                return false;
            }
            if (l.size() != d.l.size()) {
                return false;
            }
            for (int i = 0; i < l.size(); ++i) {
                if (!l.get(i).equals(d.l.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

}
