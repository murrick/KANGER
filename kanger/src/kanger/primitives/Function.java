package kanger.primitives;

import kanger.Mind;
import kanger.compiler.Operation;
import kanger.compiler.Parser;
import kanger.enums.Enums;
import kanger.enums.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitry G. Qusnetsov on 26.05.15.
 * <p>
 * Домен для функции. Может быть рекурсивным на уровне структуры TList.
 */
public class Function {

    private Term name = null;
    private int range = 0;
    private final List<Argument> arguments = new ArrayList<>();     // Параметры
    private boolean calculated = false;
    private boolean busy = false;                       // Предотвращение бесконечной рекурсии

    private Domain owner = null;

    private Mind mind = null;

    public Function(Domain d, Mind mind) {
        this.owner = d;
        this.mind = mind;
    }

    public Function(DataInputStream dis, Mind mind) throws IOException {
        long id = dis.readLong();
        name = mind.getTerms().get(id);
        range = dis.readInt();
        this.mind = mind;

//        f = (FunctionDescriptor) mind.getFunctions().get(id);
//        line.clear();
//        int count = dis.readInt();
//        while (count-- > 0) {
//            Argument a = new Argument(dis, mind);
//            line.add(a);
//        }
        arguments.clear();
        int count = dis.readInt();
        while (count-- > 0) {
            Argument a = new Argument(dis, mind);
            arguments.add(a);
        }
    }

//    public FunctionDescriptor getF() {
//        return f;
//    }
//
//    public void setF(FunctionDescriptor f) {
//        this.f = f;
//    }
//
//    public void add(Argument t) {
//        line.add(t);
//    }

    public Argument get(int i) {
        if (i == range && range == arguments.size()) {
            arguments.add(new Argument());
        }
        return arguments.get(i);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

//    public void setL(List<Argument> list) {
//        line = list;
//    }

//    public void setA(List<Argument> list) {
//        arguments = list;
//    }

    //    public List<Argument> getArguments() {
//        return arguments;
//    }
//
    public List<Argument> getArguments() {
        return arguments;
    }

    public Term getResult() {
        while (range + 1 > arguments.size()) {
            arguments.add(new Argument());
        }
        if (range + 1 == arguments.size()) {
            return arguments.get(range).getValue();
        } else {
            return null;
        }
    }

//    public void setResult(Domain d, Argument r) {
//        if (range + 1 > arguments.size()) {
//            arguments.add(new Argument());
//        }
//        arguments.get(range).setValue(d, r.getValue());
////        arguments.get(range).setC(r.getC());
////        arguments.get(range).setT(r.getT());
////        arguments.get(range).setF(r.getF());
//    }

    public boolean setResult(Term r) {
        while (range + 1 > arguments.size()) {
            arguments.add(new Argument());
        }

//        if ((arguments.get(range).isEmpty() && r != null)
//                || !arguments.get(range).isEmpty() && r == null
//                || (!arguments.get(range).isEmpty() && r != null && arguments.get(range).getValue().getId() != r.getId())) {
//            mind.getCalculated().add(d.getId());
//        }
//        arguments.get(range).setValue(d, r);

        return setParameter(range, r);
    }

    public boolean setParameter(int i, Term r) {
        if ((arguments.get(i).isEmpty() && r != null)
                || !arguments.get(i).isEmpty() && r == null
                || (!arguments.get(i).isEmpty() && r != null && arguments.get(i).getValue().getId() != r.getId())) {
            mind.getCalculated().add(owner.getId());
        }
        return arguments.get(i).setValue(owner, r);
    }

//    public boolean isCalculated() {
//        int i = 0;
//        for (; i <= range; ++i) {
//            if (arguments.get(i) == null || !arguments.get(i).isCSet()) {
//                return false;
//            }
//        }
//        return true;
//    }


    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    private String formatParam(Argument t) {
        Operation op = Parser.getOp(name.toString());
        boolean isOp = op != null && op.getRange() == range;
        String s = "";
        if (t.isFSet()) {
            s += (isOp ? "(" : "") + t.getF().toString() + (isOp ? ")" : "");
        } else if (t.isTSet()) {
            s += t.getT().toString();
        } else if (!t.isEmpty()) {
            s += t.getValue().toString();
        } else {
            s += "_";
        }
        return s;
    }

    public Term getName() {
        return name;
    }

    public void setName(Term name) {
        this.name = name;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Domain getOwner() {
        return owner;
    }

    public void setOwner(Domain owner) {
        this.owner = owner;
    }

    public String toString() {
        Operation op = Parser.getOp(name.toString());
        String s = "";
        if (op == null || op.getRange() != range) {
            s = String.format("%s(", name.toString());
            for (int i = 0; i < range; ++i) {
                s += formatParam(arguments.get(i));
                if (i + 1 < range) {
                    s += (char) Enums.COMMA;
                }
            }
            s += ")";
        } else if (op.getRange() == 1) {
            if (op.isPost()) {
                s = formatParam(arguments.get(0)) + op.getName();
            } else {
                s = op.getName() + formatParam(arguments.get(0));
            }
        } else {
            for (int i = 0; i < op.getRange(); ++i) {
                s += formatParam(arguments.get(i));
                if (i + 1 < op.getRange()) {
                    s += " " + op.getName() + " ";
                }
            }
        }
        Argument r = range < arguments.size() ? arguments.get(range) : null;
        return s /*+ (r != null && r.getValue() != null ? (" = " + r.getValue()) : "")*/;
    }

    //    public void setResult(Term c) {
//        if(f != null) {
//            while(f.getRange() >= arguments.size()) {
//                arguments.add(new TList());
//            }
//            arguments.get(f.getRange()).setC(c);
//        }
//
//    }
    void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(name.getId());
//        dos.writeInt(line.size());
//        for (Argument a : line) {
//            a.writeCompiledData(dos);
//        }
        dos.writeInt(range);
        dos.writeInt(arguments.size());
        for (Argument a : arguments) {
            a.writeCompiledData(dos);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Function)) {
            return false;
        } else {
            Function fo = (Function) o;
            if (!fo.name.equals(name)) {
                return false;
            }
            if (range != fo.getRange()) {
                return false;
            }
            if (fo.arguments.size() != arguments.size()) {
                return false;
            }
            for (int i = 0; i < arguments.size(); ++i) {
                if (!fo.arguments.get(i).equals(arguments.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }


    public void clearResult(Domain d) {
        if (range + 1 > arguments.size()) {
            arguments.add(new Argument());
        }
        if (!arguments.get(range).isEmpty()) {
            mind.getCalculated().add(d.getId());
        }
        arguments.get(range).delValue(d);
    }

    public List<TVariable> getTVariables() {
        return Tools.getTVariables(arguments, true);
    }

}
