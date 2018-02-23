package kanger.primitives;

import kanger.Mind;
import kanger.compiler.Operation;
import kanger.compiler.Parser;
import kanger.enums.Enums;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 * <p>
 * Вариант решения предиката.
 */
public class Solve {

    Predicate predicate = null;                                 // Ссылка на предикат
    private final List<Argument> line = new ArrayList<>();                      // Массив с решениями. Разм.из predicate->range
    //    private final List<Pair> stack = new ArrayList<>();                                 // Массив с подстановками на момент формирования записи
    private boolean antc = true;                                // current ! или ?
    private boolean cuted = false;                              // Флаг активности
    private int loged = 0;                              // Было причиной получения нового решения
    private boolean obtained = false;                           // Получено в роцессе вывода
    private boolean last = false;                               // Решение добавлено при последнем проходе
    private Map<Right, List<Solve>> causes = new HashMap<>();                                 // Ссылка на правило и список прешений, добавившее решение в базу
    private long id = -1;                                       // ID решения
    private Solve next = null;                                  // Следующее решение

    private Mind mind;


    public Solve() {
    }

    public Solve(DataInputStream ids, Mind mind) throws IOException {
        id = ids.readLong();
        //TODO: Запись causes
//        mind.getSolveLinks().put(this, ids.readLong());
        line.clear();
        int size = ids.readInt();
        while (size-- > 0) {
            Argument a = new Argument(ids, mind);
            line.add(a);
        }

        int flag = ids.readInt();
        antc = (0x0002 & flag) != 0;
    }

    public boolean isAntc() {
        return antc;
    }

    public void setAntc(boolean antc) {
        this.antc = antc;
    }

    public boolean isCuted() {
        return cuted;
    }

    public void setCuted(boolean cuted) {
        this.cuted = cuted;
    }

    public int getLoged() {
        return loged;
    }

    public void setLoged(int loged) {
        this.loged = loged;
        for (Argument a : line) {
            if (a.isTSet() && a.getT().getS() != null && a.getT().getS().getLoged() != loged) {
                a.getT().getS().setLoged(loged);
            }
        }
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public Solve getNext() {
        return next;
    }

    public void setNext(Solve next) {
        this.next = next;
    }

    public boolean isObtained() {
        return obtained;
    }

    public void setObtained(boolean obtained) {
        this.obtained = obtained;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Argument get(int i) {
        return line.get(i);
    }

    public void add(Argument p) {
        line.add(p);
    }

//    public void add(TVariable t, Term d, Solve s) {
//        Pair p = new Pair(d, t, s);
//        for (Pair q : stack) {
//            if (p.equals(q)) {
//                return;
//            }
//        }
//        stack.add(p);
////        subst.put(t, d);
//    }

    public List<Argument> getL() {
        return line;
    }

    public Map<Right, List<Solve>> getCauses() {
        return causes;
    }

    public boolean cVarsExists() {
        for (Argument d : line) {
            if (d != null && d.getValue().isCVar()) {
                return true;
            }
        }
        return false;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        //TODO: Запись causes
//        dos.writeLong(right.getRd());
        dos.writeInt(line.size());
        for (Argument d : line) {
            d.writeCompiledData(dos);
        }

//        dos.writeInt(stack.size());
//        for (Pair p : stack) {
//            dos.writeLong(p.getT().getId());
//            dos.writeLong(p.getC().getId());
//        }

        int flags = (antc ? 0x0002 : 0);
        dos.writeInt(flags);
    }

    public String toString() {

        Operation op = Parser.getOp(predicate.getName());
//        String str = String.format("%c", s.isAntc() ? Enums.ANT : Enums.SUC);
        int index = 0;
        boolean aqn = false;
        boolean xantc = antc;
        Map<Term, String> pqn = new HashMap<>();
        List<String> params = new ArrayList<>();
        for (Argument d : line) {
            if (d == null) {
                aqn = true;
                params.add("z");
            } else if (d.getValue().isCVar()) {
                if (!pqn.containsKey(d)) {
                    pqn.put(d.getValue(), "x" + (++index));
                }
                params.add(pqn.get(d));
            } else {
                params.add(d.toString());
            }
        }

        String str = "";
        if (op == null) {
            if (!antc) {
                str += String.format("%c", Enums.NOT);
                xantc = true;
            }
            str += predicate.getName() + "(";
            int i = 0;
            for (String p : params) {
                str += p;
                if (i + 1 < predicate.getRange()) {
                    str += String.format("%c", Enums.COMMA);
                }
                ++i;
            }
            str += ")";
        } else {
            if (!antc) {
                str += String.format("%c(", Enums.NOT);
            }
            if (op.getRange() == 1) {
                if (op.isPost()) {
                    str += params.get(0) + op.getName();
                } else {
                    str += op.getName() + params.get(0);
                }
            } else {
                for (int i = 0; i < op.getRange(); ++i) {
                    str += params.get(i);
                    if (i + 1 < op.getRange()) {
                        str += " " + op.getName() + " ";
                    }
                }
            }
            if (!antc) {
                str += ")";
            }
        }

//        if(s.isAntc()) {
        String prefix = ""; //String.format("%c", Enums.ANT);;
        if (aqn) {
            prefix += String.format("%cz ", xantc ? Enums.AQN : Enums.PQN);
        }
        for (String p : pqn.values()) {
            prefix += String.format("%c%s ", xantc ? Enums.PQN : Enums.AQN, p);
        }
//        } else {
//        }
        return prefix + str + ";";
    }


    public boolean equals(Object t) {
        if (t == null) {
            return false;
        } else if (t instanceof Solve && ((Solve) t).line.size() == line.size()) {
            for (int i = 0; i < line.size(); ++i) {
                if (line.get(i) != null && ((Solve) t).line.get(i) != null && !((Solve) t).line.get(i).equals(line.get(i))) {
                    return false;
                } else if (line.get(i) != null && ((Solve) t).line.get(i) == null) {
                    return false;
                } else if (line.get(i) == null && ((Solve) t).line.get(i) != null) {
                    return false;
                }
            }
            return true;
        } else if (t instanceof List && ((List) t).size() == line.size()) {
            for (int i = 0; i < line.size(); ++i) {
                List<Term> d = null;
                List<Argument> a = null;
                if (((List) t).get(0) instanceof Term) {
                    d = (List<Term>) t;
                } else {
                    a = (List<Argument>) t;
                }
//                (((List) t).get(0) instanceof Term) ? null : (List<Term>) t;
//                (((List) t).get(0) instanceof Argument) ? null : (List<Argument>) t;
                if (d != null) {
                    if (line.get(i) != null && d.get(i) != null && !d.get(i).equals(line.get(i))) {
                        return false;
                    } else if (line.get(i) != null && d.get(i) == null) {
                        return false;
                    } else if (line.get(i) == null && d.get(i) != null) {
                        return false;
                    }
                } else if (a != null) {
                    Term c = a.get(i).getValue();
                    if (line.get(i) != null && a.get(i) != null && !line.get(i).equals(c)) {
                        return false;
                    } else if (line.get(i) != null && a.get(i) == null) {
                        return false;
                    } else if (line.get(i) == null && a.get(i) != null) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private String loadVars() {
        String log = "";
        for (Argument a : line) {
            if (a.isTSet() && !a.isEmpty()) {
                if (!log.isEmpty()) {
                    log += ", ";
                }
                String str = "";
                if (a.getValue().isCVar()) {
                    str = String.format("%s=(some)", a.getT().getName());
                } else {
                    str = String.format("%s=%s", a.getT().getName(), a.getValue().toString());
                }
                log += str;
                //}
            }
        }
        if (!log.isEmpty()) {
            mind.getValues().add(log);
        }
        return log;
    }


    private int loadSolves() {
        int i = 0;
        for (Argument a : line) {
            if (a.isTSet() && !a.isEmpty()) {
                mind.getSolutions().add(a.getValue().toString());
                ++i;
            }
        }
        return i;
    }


}
