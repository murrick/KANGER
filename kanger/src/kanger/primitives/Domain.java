package kanger.primitives;

import kanger.Mind;
import kanger.compiler.Operation;
import kanger.compiler.Parser;
import kanger.enums.Enums;
import kanger.enums.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import kanger.exception.*;

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


    public boolean isQueued() {
        return mind.getQueuedDomains().contains(id);
    }

//    public boolean isAcceptor() {
//        return mind.getAcceptorDomains().contains(id);
//    }
//
//    public void setAcceptor(boolean on) {
//        if (on) {
//            mind.getAcceptorDomains().add(id);
//        } else {
//            mind.getAcceptorDomains().remove(id);
//        }
//    }
//

    public void setQueued() {
        mind.getQueuedDomains().add(id);
    }

    public List<Domain> getCauses() {
        List<Domain> list = new ArrayList<>();
        for (TVariable t : getTVariables(true)) {
            if (t.getDstSolve() != null && t.getSrcSolve() != null && t.getDstSolve().getId() == id && t.getSrcSolve().getId() != id) {
                list.add(t.getSrcSolve());
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
//        else if (t.isFSet() && t.getF().getResult() != null)
//            name = t.getF().toString(); // + "=" + t.getF().getResult().toString();
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
        if (t.isFSet()) {
            s += t.getF().toString();
        } else if (t.isTSet()) {
            s += t.getT().toString();
        } else if (!t.isEmpty()) {
            s += t.getValue().toString();
        } else {
            s += "_";
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

        String suffix = "";
        if ((mind.getDebugLevel() & Enums.DEBUG_OPTION_STATUS) != 0) {
            suffix = isDest() || isQuery() || /*isUsed() ||*/ isClosed()
                    ? " " + (isDest() ? "A" : "") + (isQuery() ? "Q" : "") + /*(isUsed() ? "U" : "") +*/ (isClosed() ? "C" : "") + " "
                    : "";
        }
        return s + ";" + suffix;
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

    public boolean equalsBase(Object o) {
        if(o == null || !(o instanceof Domain)) {
            return false;
        }
        if (!predicate.equals(((Domain) o).predicate)) {
            return false;
        }
        if (arguments.size() != ((Domain) o).arguments.size()) {
            return false;
        }
        for (int i = 0; i < arguments.size(); ++i) {
            if (arguments.get(i).isEmpty() || ((Domain) o).arguments.get(i).isEmpty()) {
                return false;
            }
            if (arguments.get(i).getValue().getId() != ((Domain) o).arguments.get(i).getValue().getId()) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(TVariable t) {
        for (TVariable x : getTVariables(true)) {
            if (x.getId() == t.getId()) {
                return true;
            }
        }
        return false;
    }

//    public boolean isDestFor(Domain d) {
//        return mind.getSources().containsKey(this) && mind.getSources().get(this).contains(d);
//    }
//
//    public void setDestFor(Domain d) {
//        if (!mind.getDestinations().containsKey(d)) {
//            mind.getDestinations().put(d, new HashSet<>());
//        }
//        if (!mind.getSources().containsKey(this)) {
//            mind.getSources().put(this, new HashSet<>());
//        }
//        mind.getDestinations().get(d).add(this);
//        mind.getSources().get(this).add(d);
//    }

    public boolean isDest() {
        for (TVariable t : getTVariables(false)) {
            if (!t.isEmpty() && /*contains(t)) {*/ t.getDstSolve().getId() == id) {
                return true;
            }
        }
        return false;
        //
        // return mind.getSources().containsKey(this) && !mind.getSources().get(this).isEmpty();
    }

    public List<TVariable> getTVariables(boolean full) {
        return Tools.getTVariables(arguments, full);
    }

    public List<Function> getFunctions() {
        List<Function> list = new ArrayList<>();
        for (Argument a : arguments) {
            if (a.isFSet()) {
                list.add(a.getF());
            }
        }
        return list;
    }

    private boolean isEqualsArguments(List<Long> params) {
        for (int i = 0; i < predicate.getRange(); ++i) {
            if (arguments.get(i).getValue().getId() != params.get(i)) {
                return false;
            }
        }
        return true;
    }

    private List<Long> convertArguments() {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < predicate.getRange(); ++i) {
            list.add(arguments.get(i).getValue().getId());
        }
        return list;
    }

    public boolean isClosed() {
        if (mind.getClosedDomains().containsKey(id)) {
            for (List<Long> list : mind.getClosedDomains().get(id)) {
                if (isEqualsArguments(list)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setClosed() {
        if (!mind.getClosedDomains().containsKey(id)) {
            mind.getClosedDomains().put(id, new HashSet<>());
        }
        if (!isClosed()) {
            mind.getClosedDomains().get(id).add(convertArguments());
        }
    }


    public boolean isUsed() {
        if (mind.getClosedDomains().containsKey(id)) {
            for (List<Long> list : mind.getClosedDomains().get(id)) {
                if (isEqualsArguments(list)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setUsed() {
        if (!mind.getUsedDomains().containsKey(id)) {
            mind.getUsedDomains().put(id, new HashSet<>());
        }
        if (!isClosed()) {
            mind.getUsedDomains().get(id).add(convertArguments());
        }
    }


    public boolean isSystem() {
        return Parser.getOp(predicate.getName()) != null;
    }

    public int execSystem() {
        if (isSystem()) {
            try {
                return mind.getCalculator().execute(this);

            } catch (RuntimeErrorException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean recalculate() throws RuntimeErrorException {
        boolean occurrs = false;
        for (Argument a : arguments) {
            if (a.isFSet() /*&& a.getF().isSubstituted()*/) {
//                a.getF().clearResult();
                if (mind.getCalculator().calculate(a.getF()) > 0) {
                    occurrs = true;
                }
            }
        }
        return occurrs;
    }

    public boolean isPairedWith(Domain d) {
        for (TVariable t : getTVariables(false)) {
            if (d.getTVariables(false).contains(t)) {
                return true;
            }
        }
        return false;
    }

    public boolean isQuery() {
        if (right.isQuery()) {
            return true;
        } else {
            for (TVariable t : getTVariables(true)) {
                if (t.isQuery()) {
                    return true;
                }
            }
            return false;
        }
    }

//    public void setQuery() {
//        if (!right.isQuery()) {
//            for (TVariable t : getTVariables(true)) {
//                if (!mind.getQueryValues().containsKey(t.getId())) {
//                    mind.getQueryValues().put(t.getId(), new HashSet<>());
//                }
//                mind.getQueryValues().get(t.getId()).add(t.getValue().getId());
//            }
//        }
//    }
}
