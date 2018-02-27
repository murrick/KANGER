package kanger.primitives;

import kanger.Mind;
import kanger.compiler.Operation;
import kanger.compiler.Parser;
import kanger.enums.Enums;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 * <p>
 * Описатель варианта решения предиката
 */
public class Domain {

    private Predicate predicate = null;                             // Ссылка на описатель предиката
    private List<Argument> arguments = new ArrayList<>();           // Массив подстановочных переменных
    private boolean antc = true;                            // ! или ?
    private Right right;                                    // Ссылка на правило
    private long id = -1;                                   // id домена
    private Domain next = null;                             // Следующий элемент

    private Mind mind = null;

    public Domain(Mind mind) {
        this.mind = mind;
    }

    public Domain(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        mind.getDomainLinks().put(this, dis.readLong());
        int flags = dis.readInt();
        antc = (flags & 0x0001) != 0;
//        used = (flags & 0x0002) != 0;
        long id = dis.readLong();
        predicate = mind.getPredicates().get(id);
        int count = dis.readInt();
        arguments.clear();
        while (count-- > 0) {
            Argument a = new Argument(dis, mind);
            arguments.add(a);
        }
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
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

    public Domain getNext() {
        return next;
    }

    public void setNext(Domain next) {
        this.next = next;
    }

    public Argument get(int i) {
        return arguments.get(i);
    }

    public void add(Argument t) {
        arguments.add(t);
    }

    public boolean isUsed() {
        return mind.getUsedDomains().contains(id);
    }

    public void setUsed(boolean used) {
        if (used) {
            mind.getUsedDomains().add(id);
        } else {
            mind.getUsedDomains().remove(id);
        }
    }

    public boolean isAcceptor() {
        return mind.getAcceptorDomains().contains(id);
    }

    public boolean isQueued() {
        return mind.getQueuedDomains().contains(id);
    }

    public void setAcceptor(boolean on) {
        if (on) {
            mind.getAcceptorDomains().add(id);
        } else {
            mind.getAcceptorDomains().remove(id);
        }
    }

    public void setQueued(boolean on) {
        if (on) {
            mind.getQueuedDomains().add(id);
        } else {
            mind.getQueuedDomains().remove(id);
        }
    }

    public List<Domain> getCauses() {
        List<Domain> list = new ArrayList<>();
        for (Argument a : arguments) {
            if (a.isTSet() && !a.isEmpty()) {
                TVariable t = a.getT();
                if (t.rewind()) {
                    do {
                        if (t.getDstSolve().getId() == id) {
                            list.add(t.getSrcValue());
                            break;
                        }
                    } while (t.next());
                }
            }
        }
        return list;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public boolean isAntc() {
        return antc;
    }

    public void setAntc(boolean antc) {
        this.antc = antc;
    }

    //    String s = String.format("%c%s(", d.isAntc() ? Enums.ANT : Enums.SUC, d.getPredicate().getName());
//    int i = 0;
//    for (TList t : d.getArguments()) {
//        String name = "_";
//        if (t.isCSet()) name = t.getC().toString();
//        else if (t.isFSet() && t.getF().getR() != null)
//            name = t.getF().toString(); // + "=" + t.getF().getR().toString();
//        else if (t.isTSet() && t.getT().getOwner() != 0) name = t.getT().getValue().getTerm().getName();
//        s += name;
//        if (i + 1 != d.getPredicate().getRange()) {
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
        } else {
            s += t.getValue().toString();
        }
        return s;
    }

    public String toString() {
        String s = String.format("%c", antc ? Enums.ANT : Enums.SUC);
        Operation op = Parser.getOp(predicate.getName());
        if (op == null) {
            s += predicate.getName() + "(";
            int i = 0;
            for (Argument t : arguments) {
                s += formatParam(t);
                if (i + 1 != predicate.getRange()) {
                    s += (char) Enums.COMMA;
                }
                ++i;
            }
            s += ")";
        } else if (op.getRange() == 1) {
            if (op.isPost()) {
                s += formatParam(arguments.get(0)) + op.getName();
            } else {
                s += op.getName() + formatParam(arguments.get(0));
            }
        } else {
            try {
                for (int i = 0; i < op.getRange(); ++i) {
                    s += formatParam(arguments.get(i));
                    if (i + 1 < op.getRange()) {
                        s += " " + op.getName() + " ";
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                System.out.print(ex);
            }
        }
        return s + ";" + (this.isAcceptor() ? " A " : "") + (right.isQuery() ? " Q " : "");
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeLong(right.getId());
        int flags = (antc ? 0x0001 : 0);
//                | (used ? 0x0002 : 0);
//                | (cst ? 0x0004 : 0);
        dos.writeInt(flags);
        dos.writeLong(predicate.getId());
        dos.writeInt(arguments.size());
        for (Argument a : arguments) {
            a.writeCompiledData(dos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Domain)) {
            return false;
        } else {
            Domain d = (Domain) o;
            if (!predicate.equals(d.predicate)) {
                return false;
            }
            if (arguments.size() != d.arguments.size()) {
                return false;
            }
            for (int i = 0; i < arguments.size(); ++i) {
                if (!arguments.get(i).equals(d.arguments.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean contains(TVariable t) {
        for (Argument a : arguments) {
            if (a.isTSet() && a.getT().getId() == t.getId()) {
                return true;
            }
        }
        return false;
    }
}
