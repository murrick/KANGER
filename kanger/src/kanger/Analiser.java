package kanger;

import kanger.compiler.SysOp;
import kanger.enums.Enums;
import kanger.enums.LogMode;
import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;
import kanger.primitives.*;
import kanger.stores.HypotesesStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by murray on 26.05.15.
 */
public class Analiser {

    private static final boolean DEBUG_DISABLE_FALSE_CHECK = false;
    private Mind mind;
    private boolean isInsertion = false;

    public Analiser(Mind mind) {
        this.mind = mind;
    }


    private boolean compareDomains(Domain d1, Domain d2, int level) {
        if (level >= d1.getPredicate().getRange()) {
            d1.setUsed(true);
            d2.setUsed(true);
            mind.getLog().add(LogMode.ANALIZER, "Equals:");
            mind.getLog().add(LogMode.ANALIZER, "\t" + d1.toString());
            mind.getLog().add(LogMode.ANALIZER, "\t" + d2.toString());
            return true;
        } else {
            for (int i = 0; i <= level; ++i) {
                if (d1.get(i).isTSet() && d1.get(i).isEmpty() && !d2.get(i).isEmpty()) {
                    if (!d1.get(i).setValue(d2.get(i).getValue())) {
                        return false;
                    } else {
                        d1.get(i).getT().setSolve(d2);
                        d1.setInitiated(true);
                    }
                } else if (d2.get(i).isTSet() && d2.get(i).isEmpty() && !d1.get(i).isEmpty()) {
                    if (!d2.get(i).setValue(d1.get(i).getValue())) {
                        return false;
                    } else {
                        d2.get(i).getT().setSolve(d1);
                        d2.setInitiated(true);
                    }
                } else if (d1.get(i).isTSet() && d2.get(i).isTSet() && d1.get(i).isEmpty() && d2.get(i).isEmpty()) {
                    //TODO: Спорный момент - генерация временной переменной при сравнении двух пустых t-переменных
                    Term c = mind.getTerms().get();
                    d1.get(i).setValue(c);
                    d2.get(i).setValue(c);
                } else if (d1.get(i).isEmpty() || d2.get(i).isEmpty() || !d1.get(i).getValue().equals(d2.get(i).getValue())) {
                    return false;
                }
            }
            return compareDomains(d1, d2, level + 1);
        }
    }

    private boolean compareRights(Right r1, Right r2) {
        //mind.release();
        // Последовательно сравниваем все ветки двух правил
        boolean closed = false;

//        mind.getInitiatedDomains().clear();
//        int initiatedCount = 0;
//        do {
//            initiatedCount = mind.getInitiatedDomains().size();
        for (Tree t1 : r1.getTree()) {
            for (Tree t2 : r2.getTree()) {
                // Сравнение двух ветвей
                for (Domain d1 : t1.getSequence()) {
                    for (Domain d2 : t2.getSequence()) {
                        if (!d1.isUsed() && !d2.isUsed()
                                && d1.isAntc() != d2.isAntc()
                                && d1.getPredicate().equals(d2.getPredicate())) {

//                            boolean initiatedD1 = d1.isInitiated();
//                            boolean initiatedD2 = d2.isInitiated();

                            compareDomains(d1, d2, 0);

//                                if (compareDomains(d1, d2, 0)) {
//                                    t1.setUsed(true);
//                                    t2.setUsed(true);
//
//                                    Screen.showTree(r1);
//                                    Screen.showTree(r2);
//                                }
                        }
                    }
                }

            }
        }
//        } while(initiatedCount != mind.getInitiatedDomains().size());

        boolean treeClosed = false;
        for (Tree t : r1.getTree()) {
            boolean c = true;
            for (Domain d : t.getSequence()) {
                if (d.isUsed()) {
                    t.setUsed(true);
                } else {
                    c = false;
                }
            }
            t.setClosed(c);
            if (c) {
                treeClosed = true;
            }
        }

        for (Tree t : r2.getTree()) {
            boolean c = true;
            for (Domain d : t.getSequence()) {
                if (d.isUsed()) {
                    t.setUsed(true);
                } else {
                    c = false;
                }
            }
            t.setClosed(c);
            if (c && treeClosed) {
                closed = true;
            }
        }

        return closed;
    }

    public boolean analiser(boolean logging) {
        boolean result = false;

//        mind.release();
        mind.clearQueryStatus();

        for (Right r1 = mind.getRights().getRoot(); r1 != null; r1 = r1.getNext()) {
            for (Right r2 = r1.getNext(); r2 != null; r2 = r2.getNext()) {
                if (compareRights(r1, r2)) {
                    result = true;
                }
            }
        }
        return result;
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
                    analiser(true);

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
                    r.setCurrent(true);

                    if (r != null) {
                        mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                        mind.getLog().add(LogMode.ANALIZER, r);
                        mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");

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

                            r.setCurrent(false);
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

                        if (!DEBUG_DISABLE_FALSE_CHECK) {

                            mind.getLog().reset();
                            mind.getLog().add(LogMode.ANALIZER, "============= FALSE CHECKING ==============");

                            analiser(true);
                            Right r = (Right) mind.compileLine(invert(line));
                            r.setCurrent(true);

                            if (r != null) {
                                mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                                mind.getLog().add(LogMode.ANALIZER, r);
                                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");

                                if (analiser(false)) {
                                    mind.getLog().add(LogMode.ANALIZER, "Result: FALSE");
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
                        }
                    }

                    if (res == null) {
                        mind.getLog().add(LogMode.ANALIZER, "============= TRUE CHECKING ===============");

                        //mind.release();
//                        mind.release();
//                        analiser();
                        mind.getSolutions().reset();
                        mind.getValues().reset();
                        mind.release();
                        //analiser();

                        analiser(true);
                        Right r = (Right) mind.compileLine(line);
                        r.setCurrent(true);

                        if (r != null) {
                            mind.getLog().add(LogMode.ANALIZER, "Compiled: " + r.getOrig());
                            mind.getLog().add(LogMode.ANALIZER, r);
                            mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");


//                            if (!isInsertion) {
//                                isHypotheses = true;
//                            }
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

}
