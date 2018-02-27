package kanger;

import kanger.compiler.SysOp;
import kanger.enums.Enums;
import kanger.enums.LogMode;
import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;
import kanger.primitives.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// !@x a(x) -> b(x), @y b(y) -> c(y), @z c(z) -> d(z);
/**
 * Created by murray on 26.05.15.
 */
public class Analiser {

    private static final boolean DEBUG_DISABLE_FALSE_CHECK = false;

    private final Mind mind;
    private boolean isInsertion = false;

    public Analiser(Mind mind) {
        this.mind = mind;
    }

    private boolean recurseTree(List<Domain> sequence, List<TVariable> vars, int index, boolean logging) {
        boolean result = false;
        if (index >= vars.size()) {
            for (int k = 0; k < sequence.size(); ++k) {
                Domain a = sequence.get(k);
                for (int j = k + 1; j < sequence.size(); ++j) {
                    Domain b = sequence.get(j);
                    if (a.getPredicate().getId() == b.getPredicate().getId() && a.isAntc() != b.isAntc()) {
                        boolean equals = true;
                        if ((a.getRight().isQuery() && b.isAcceptor()) || (b.getRight().isQuery() && a.isAcceptor())) {
                            equals = false;
                        } else {
                            for (int i = 0; i < a.getPredicate().getRange(); ++i) {
                                if (!a.getArguments().get(i).isEmpty()
                                        && !b.getArguments().get(i).isEmpty()
                                        && a.getArguments().get(i).getValue().equals(b.getArguments().get(i).getValue())) {
                                } else {
                                    equals = false;
                                    break;
                                }
                            }
                        }
                        if (equals) {
                            result = true;

                            if (a.getRight().isQuery()) {
                                mind.getSolutions().add(b.toString());
                            } else if ((b.getRight().isQuery())) {
                                mind.getSolutions().add(a.toString());
                            } else {
                                mind.getSolutions().add(a.toString());
                                mind.getSolutions().add(b.toString());
                            }

                            if (logging) {
                                mind.getLog().add(LogMode.ANALIZER, "Sequence resolved : ");
                                for (Domain x : sequence) {
                                    mind.getLog().add(LogMode.ANALIZER, "\t" + x.toString());
                                }
                                mind.getLog().add(LogMode.ANALIZER, "Сoincidence : ");
                                mind.getLog().add(LogMode.ANALIZER, "\t" + a.toString());
                                mind.getLog().add(LogMode.ANALIZER, "\t" + b.toString());
                            }

                            List<TVariable> list;
                            if (a.getRight().isQuery()) {
                                list = getTVariables(Arrays.asList(a));
                            } else if (b.getRight().isQuery()) {
                                list = getTVariables(Arrays.asList(b));
                            } else {
                                list = getTVariables(Arrays.asList(a, b));
                            }

                            if (!list.isEmpty()) {
                                if (logging) {
                                    mind.getLog().add(LogMode.ANALIZER, "Values : ");
                                }
                                for (TVariable t : list) {
                                    mind.getValues().add(t.getName() + "=" + t.getValue());
                                    if (logging) {
                                        mind.getLog().add(LogMode.ANALIZER, "\t" + t.getName() + "=" + t.getValue());
                                    }
                                }
                            }

                            if (logging) {
                                mind.getLog().add(LogMode.ANALIZER, "===========================================");
                            }

                        }
                    }
                }
            }
        } else {
            vars.get(index).rewind();
            do {
                if (recurseTree(sequence, vars, index + 1, logging)) {
                    result = true;
                }
            } while (vars.get(index).next());
        }
        return result;
    }

    public boolean analiseTree(List<Domain> sequence, boolean logging) {
        List<TVariable> vars = getTVariables(sequence);
        return recurseTree(sequence, vars, 0, logging);
    }

    public boolean analiser(boolean logging) {
        boolean result = false;
        int counter = 0;

        if (logging) {
            mind.getLog().add(LogMode.ANALIZER, "============= ANALISER ====================");
        }

        Set<Tree> query = new HashSet<>();
        for (Right r = mind.getRights().getRoot(); r != null; r = r.getNext()) {
            if (r.isQuery()) {
                query.addAll(r.getTree());
            }
        }

        for (Tree t = mind.getTrees().getRoot(); t != null; t = t.getNext()) {
            if (analiseTree(t.getSequence(), logging)) {
                result = true;
            }
            for (Tree x : query) {
                List<Domain> actual = new ArrayList<>();
                actual.addAll(t.getSequence());
                actual.addAll(x.getSequence());
                if (analiseTree(actual, logging)) {
                    result = true;
                }
            }
        }

        return result;
    }

    private List<TVariable> getTVariables(List<Domain> sequence) {
        List<TVariable> list = new ArrayList<>();
        for (Domain d : sequence) {
            for (Argument a : d.getArguments()) {
                if (a.isTSet() && !list.contains(a)) {
                    list.add(a.getT());
                }
            }
        }
        return list;
    }

    //    ///////////////////////////////
    void storeHypo() {
        for (Right r1 = mind.getRights().getRoot(); r1 != null; r1 = r1.getNext()) {
            for (Tree t : r1.getTree()) {
                if (!t.isClosed() && t.isUsed()) {
                    for (Domain d : t.getSequence()) {
                        if (!d.isUsed()) {
                            mind.getHypotesisStore().add(d.getPredicate(), d.getArguments());
                        }
                    }
                }
            }
        }
    }

    //    ///////////////////////////////
    private List<Right> killInsertion(Right target, boolean withRelatedRights) {
        int flag = 0;
        mind.release();
        List<Right> rr = new ArrayList<>();

        if (mind.getHypotesisStore().size() > 0) {
            for (Hypotese h : (List<Hypotese>) mind.getHypotesisStore().getRoot()) {
//                h.getPredicate().deleteSolve(h.getSolve());
                if (withRelatedRights) {

                    for (Right r : h.getRights()) {
                        rr.add(r);
                        mind.removeInsertionRight(r);
                    }
                }
            }
        }
//        else if (target.getWidth() == 1 && target.getHeight() == 1) {
//            Solve s = target.getT().getD().getPredicate().deleteSolve(target.getT().getD().getArguments());
//            if (withRelatedRights && s != null) {
//                if (s.getRight() != null) {
//                    rr.add(s.getRight());
//                    mind.removeInsertionRight(s.getRight());
//                }
//            }
//        }
        mind.mark();
        return rr;

//        List<Right> todo = new ArrayList<>();
//        for (Right r = mind.getRights().getRoot(); r != null; r = r.getNext()) {
//            if (r.equals(target)) {
//                todo.add(r);
//            }
//        }
//        for (Right r : todo) {
//            mind.removeInsertionRight(r);
//        }
    }

    /////////////////////////////////////
    private String invert(String line) {
        if (line.charAt(0) == Enums.ANT) {
            return String.format("%c%s", Enums.SUC, line.substring(1));
        } else {
            return String.format("%c%s", Enums.ANT, line.substring(1));
        }
    }

    private String resign(int sign, String line) {
        return String.format("%c%s", sign, line.substring(1));
    }

    public boolean isInsertion() {
        return isInsertion;
    }

    public Boolean query(String line, boolean testMode) throws ParseErrorException, RuntimeErrorException {
        Boolean res = null;

        boolean storeH = mind.getHypotesisStore().isEnabled();
        boolean storeV = mind.getValues().isEnabled();
        boolean storeS = mind.getSolutions().isEnabled();
        boolean storeL = mind.getLog().isEnabled();

        mind.getHypotesisStore().enable(!testMode);
        mind.getValues().enable(!testMode);
        mind.getSolutions().enable(!testMode);
        mind.getLog().enable(!testMode);

        mind.getLog().reset();
        mind.getSolutions().reset();
        mind.getValues().reset();
        mind.getHypotesisStore().reset();

        mind.release();
        mind.mark();

        //mind.clear();
        //mind.release();
//        isHypotheses = false;
        isInsertion = false;

        mind.getLog().add(LogMode.ANALIZER, "============= CHECKING ===================");
        mind.getLinker().link(true);
        if (analiser(true)) {
            mind.getLog().add(LogMode.ANALIZER, "ERROR: Collisions in Program");
            res = null;
        } else {
//            mind.mark();
            int key = line.charAt(0);
            switch (key) {

                case Enums.INS:
                    isInsertion = true;
                    line = resign(Enums.ANT, line);

                case Enums.ANT: {
//                    isHypotheses = true;

//                    mind.getSolutions().reset();
//                    mind.getValues().reset();
//                    mind.getSubstitutions().reset();
                    mind.getLog().reset();
                    mind.getLog().add(LogMode.ANALIZER, "============= ACCEPTING ===================");

//                    mind.release();
//                    analiser(false);
//                    Right r = (Right) mind.compileLine(invert(line));
//                    if (r != null) {
//                        mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
//                        mind.getLog().add(LogMode.ANALIZER, r.getT());
//                        mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");
//
//                        if (analiser(false)) {
//                            mind.getLog().add(LogMode.ANALIZER, "ERROR: Conflict in new Right");
//                            res = null;
//                        } else {
//                            res = false;
//                        }
//                    }
//
//                    if (res != null) {
//                        mind.release();
//                        analiser();
                    Right r = (Right) mind.compileLine(line);

                    if (r != null) {
                        mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                        mind.getLog().add(LogMode.ANALIZER, r);
                        mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");

                        mind.getLinker().link(r, true);
                        if (analiser(true)) {
                            mind.getLog().add(LogMode.ANALIZER, "ERROR: Conflict in new Right");
                            res = null;
                        } else {
                            res = true;
                            if (!isInsertion) {
                                //mind.release();
//                                mind.getText().append(line);
//                                mind.getText().append("\r");
                                //mind.compileLine(line);
                                mind.getLog().add(LogMode.SOLVES, String.format("\tSolve 000:\t%s", line));
//                                res = true;
                                mind.getLog().add(LogMode.ANALIZER, "SUCCESS: New Right Accepted");
                            } else {
                                mind.removeInsertionRight(r);
                                if (mind.getHypotesisStore().size() != 0) {
                                    mind.getLog().add(LogMode.SAVED, "Predicates added:");
                                    int i = 0;
                                    for (Hypotese s : (List<Hypotese>) mind.getHypotesisStore().getRoot()) {
//                                        mind.getText().append(String.format("%c%s", Enums.ANT, s.toString()) + "\r");
                                        mind.getSolutions().add(String.format("%c%s", Enums.ANT, s.toString()));
                                        mind.getLog().add(LogMode.SAVED, String.format("\tSolve %03d: \t%s", ++i, String.format("%c%s", Enums.ANT, s.toString())));
                                    }
                                }
                                mind.getLog().add(LogMode.ANALIZER, "SUCCESS: New solves: " + mind.getHypotesisStore().size());
                            }

                            mind.setChanged(true);
                            mind.mark();
                        }
                    }
//                    }

//                    mind.release();
//                    mind.clearQueryStatus();
                }
                break;

                case Enums.DEL:
                case Enums.WIPE:
                    SysOp op = mind.getCalculator().find(line);
                    if (op != null) {
                        if (mind.getCalculator().unregister(op.toString())) {
                            mind.getLog().add(LogMode.ANALIZER, "SUCCESS: Function removed: " + op.toString());
                        } else {
                            mind.getLog().add(LogMode.ANALIZER, "WARNING: Unable to remove function: " + op.toString());
                        }
                        break;
                    } else {
                        isInsertion = true;
                        line = resign(Enums.SUC, line);
                    }

                case Enums.SUC: {
//                    mind.release();

//                    mind.getSolutions().reset();
//                    mind.getValues().reset();
//                    mind.getSubstitutions().reset();
                    //mind.getPredicates().mark();
                    if (line.length() == 1) {
                        mind.getLog().add(LogMode.ANALIZER, "SUCCESS: No Collisions in Program");
                        res = true;
                    } else if (!isInsertion) {

//                        mind.release();
//                        mind.mark();
//                        isHypotheses = true;
                        mind.getLog().reset();
                        if (!DEBUG_DISABLE_FALSE_CHECK) {

                            mind.getLog().add(LogMode.ANALIZER, "============= FALSE CHECKING ==============");

//
//                            analiser(false);
                            Right r = (Right) mind.compileLine(invert(line));

                            if (r != null) {
                                r.setQuery(true);

                                mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                                mind.getLog().add(LogMode.ANALIZER, r);
                                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");

                                mind.getSolutions().reset();
                                mind.getValues().reset();
//                                mind.getRights().release();
//                                mind.getTrees().release();
//                                mind.getDomains().release();
                                mind.getLinker().link(r, true);
                                if (analiser(true)) {
                                    mind.getLog().add(LogMode.ANALIZER, "Result: FALSE");
                                    logResult();
                                    res = false;
                                } else if (!isInsertion) {
                                    storeHypo();
//                                isHypotheses = false;

                                    //mind.release();
//                            mind.getSolutions().reset();
//                            mind.getValues().reset();
//                            mind.getSubstitutions().reset();
//                            mind.getSolutions().reset();
//                            mind.getValues().reset();
//                            mind.getSubstitutions().reset();
//                            analiser();
                                    //mind.clear();
                                }
                            }

                            if (res == null) {
                                mind.release();
                                mind.getLinker().link(false);
                                mind.clearQueryStatus();
                            }

                        } else {
                        }
                    }

                    if (res == null) {
                        mind.getLog().add(LogMode.ANALIZER, "============= TRUE CHECKING ===============");

                        //mind.release();
//                        mind.release();
//                        analiser();
                        //analiser();
                        Right r = (Right) mind.compileLine(line);

                                System.out.println("----------- AFTER COMPILE LINE");
                                Screen.showBase(mind, false, null);
                        
                        if (r != null) {
                            r.setQuery(true);
                            mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                            mind.getLog().add(LogMode.ANALIZER, r);
                            mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");

                            mind.getSolutions().reset();
                            mind.getValues().reset();
//                            mind.getRights().release();
//                            mind.getTrees().release();
//                            mind.getDomains().release();

//                            if (!isInsertion) {
//                                isHypotheses = true;
//                            }
                            mind.getLinker().link(r, true);
                            
                            if (analiser(true)) {
                                if (isInsertion) {
                                    mind.removeInsertionRight(r);
                                    List<Right> killedRights = killInsertion(r, key == Enums.WIPE);
                                    if (mind.getHypotesisStore().size() != 0) {
                                        mind.getLog().add(LogMode.SAVED, "Predicates deleted:");
                                        int i = 0;
                                        for (Hypotese s : (List<Hypotese>) mind.getHypotesisStore().getRoot()) {
//                                            mind.getText().append(String.format("%c%s", Enums.ANT, s.toString()) + "\r");
                                            mind.getSolutions().add(String.format("%c%s", Enums.ANT, s.toString()));
                                            mind.getLog().add(LogMode.SAVED, String.format("\tSolve %03d: \t%s", ++i, String.format("%c%s", Enums.ANT, s.toString())));
                                        }
                                    }
                                    if (killedRights.size() != 0) {
                                        mind.getLog().add(LogMode.SAVED, "Rights deleted:");
                                        for (Right rr : killedRights) {
                                            mind.getLog().add(LogMode.SAVED, String.format("\tRight %03d: \t%s", rr.getId(), rr.getOrig()));
                                        }
                                    }
                                    mind.getLog().add(LogMode.ANALIZER, "SUCCESS: Deleted solves: " + mind.getHypotesisStore().size());

                                } else {
                                    mind.getLog().add(LogMode.ANALIZER, "Result: TRUE");
                                    logResult();
                                    res = true;
                                }
                            } else if (isInsertion) {
                                mind.getLog().add(LogMode.ANALIZER, "Result: No predicates was deleted");
                            } else {
                                if (!isInsertion) {
                                    storeHypo();
                                }
//                                mind.release();
                                // Удаляем гипотезы,которые приведут к сходимости в случае анализа правил.
                                // Гипотезв приводящие к сходимости на уровне базы данных отсеиваются в фазе добавления
                                if (!testMode) {
                                    for (int i = 0; i < mind.getHypotesisStore().size(); ++i) {
                                        String h = String.format("%c%s", Enums.SUC, mind.getHypotesisStore().get(i));
                                        Boolean result = query(h, true);
                                        if (result != null) {
                                            mind.getHypotesisStore().get(i).delete();
                                        }
                                    }
                                    mind.getHypotesisStore().pack();
                                }

                                if (mind.getHypotesisStore().getRoot() != null && mind.getHypotesisStore().size() > 0) {
                                    mind.getLog().add(LogMode.ANALIZER, String.format("Result: WHO KNOWS? %d Hypotheses", mind.getHypotesisStore().size()));
                                } else {
                                    mind.getLog().add(LogMode.ANALIZER, "Result: WHO KNOWS? No Hypotheses.");
                                }
                            }
                        }

                    }

//                    mind.release();
                    break;
                }
            }
        }

        mind.getHypotesisStore().enable(storeH);
        mind.getValues().enable(storeV);
        mind.getSolutions().enable(storeS);
        mind.getLog().enable(storeL);

        return res;
    }

    private void logResult() {
        if (mind.getSolutions().size() > 0) {
            mind.getLog().add(LogMode.SOLVES, "Solves:");
            int i = 0;
            for (String log : (List<String>) mind.getSolutions().getRoot()) {
                mind.getLog().add(LogMode.SOLVES, String.format("\tSolve %03d: %s", ++i, log));
            }
        }
        if (mind.getValues().size() > 0) {
            mind.getLog().add(LogMode.VALUES, "Values:");
            int i = 0;
            for (String log : (List<String>) mind.getValues().getRoot()) {
                mind.getLog().add(LogMode.VALUES, String.format("\tSolve %03d: %s", ++i, log));
            }
        }

    }

}
