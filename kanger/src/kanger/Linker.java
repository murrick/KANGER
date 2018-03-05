/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kanger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kanger.enums.LogMode;
import kanger.exception.RuntimeErrorException;
import kanger.exception.TValueOutOfOrver;
import kanger.primitives.*;

/**
 * @author murray
 */
public class Linker {

    private final Mind mind;

    public Linker(Mind mind) {
        this.mind = mind;
    }

    private boolean linkDomains(Domain master, Domain slave, int level, boolean logging, boolean occurrs) throws RuntimeErrorException {

        if (level >= master.getPredicate().getRange()) {

//            if (d1.recalculate()) {
//                occurrs = true;
//            }
//            if (d2.recalculate()) {
//                occurrs = true;
//            }
            if (logging && occurrs) {
                logComparsion(master);
//                logComparsion(slave);
                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");
            }


            return true;

        } else {
            //ПОДСТАНОВКИ
            for (int i = 0; i <= level; ++i) {
//            int level = level;

                if (master.get(level).isFSet() && !slave.get(level).isEmpty() && !master.get(level).getF().isCalculable() && !master.get(level).getF().isCalculated()) {
                    master.get(level).getF().setResult(slave.get(level).getValue());
//				d1.setUsed(true);
                    occurrs = true;
                }
                if (slave.get(level).isFSet() && !master.get(level).isEmpty() && !slave.get(level).getF().isCalculable() && !slave.get(level).getF().isCalculated()) {
                    slave.get(level).getF().setResult(master.get(level).getValue());
//				d2.setUsed(true);
                    occurrs = true;
                }

                if (master.get(level).isTSet() && !slave.get(level).isEmpty() && !slave.isDest() && !master.get(level).getT().contains(slave.get(level).getValue())) {
                    try {
                        //ВАЖНО! Для обработкаи запроса не помечаем уже имеющиеся предикаты
//                        if (!d2.isAcceptor() && !d2.getRight().isQuery()) {
//                            d1.setAcceptor(true);
//                        }
                        TValue s = master.get(level).getT().setValue(slave.get(level).getValue());
                        s.setDstSolve(master);
                        s.setSrcSolve(slave);
                        master.setUsed(true);
//                        d1.setDestFor(d2);
                        occurrs = true;

//                    d1.calcFunctions();
                        //}
                    } catch (TValueOutOfOrver ex) {
                    }
                }
                if (slave.get(level).isTSet() && !master.get(level).isEmpty() && !master.isDest() && !slave.get(level).getT().contains(master.get(level).getValue())) {
                    try {
                        //ВАЖНО! Для обработки запроса не помечаем уже имеющиеся предикаты
//                        if (!d1.isAcceptor() && !d1.getRight().isQuery()) {
//                            d2.setAcceptor(true);
//                        }
                        TValue s = slave.get(level).getT().setValue(master.get(level).getValue());
                        s.setSrcSolve(master);
                        s.setDstSolve(slave);
                        slave.setUsed(true);
//                        d2.setDestFor(d1);
                        occurrs = true;

//                    d2.calcFunctions();
                        //}
                    } catch (TValueOutOfOrver ex) {
                    }
                }

////                } else if (d1.get(i).isTSet() && d2.get(i).isTSet() && d1.get(i).isEmpty() && d2.get(i).isEmpty()) {
////                    //TODO: Спорный момент - генерация временной переменной при сравнении двух пустых t-переменных
////                    Term c = mind.getTerms().get();
////                    try {
////                        TSubst s = d1.get(i).getT().setValue(c);
////                        s.setSrcSolve(d2);
////                        s.setDstSolve(d1);
////                        d1.setAcceptor(true);
////                    } catch (TValueOutOfOrver ex) {
////                        return false;
////                    }
////                    try {
////                        TSubst s = d2.get(i).getT().setValue(c);
////                        s.setSrcSolve(d1);
////                        s.setDstSolve(d2);
////                        d2.setAcceptor(true);
////                    } catch (TValueOutOfOrver ex) {
////                        return false;
////                    }
//            }
            }
            return linkDomains(master, slave, level + 1, logging, occurrs);
        }

    }

//    private boolean execSystem(Domain d) {
//        if (d.isSystem()) {
//            try {
//                int result = mind.getCalculator().execute(d);
//                return true;
//            } catch (RuntimeErrorException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }
//
//    private void calcFunctions(Set<Tree> set) {
//        for (Tree t : set) {
//            for (Domain d : t.getSequence()) {
//                execSystem(d);
//            }
//            for (Domain d : t.getSequence()) {
//                for (Argument a : d.getArguments()) {
//                    if (a.isFSet()) {
//                        try {
//                            mind.getCalculator().calculate(a.getF());
//                        } catch (RuntimeErrorException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//    }


    public void recurseLink(List<TVariable> tvars, int tIndex, Right query, Set<Tree> set, boolean logging) throws RuntimeErrorException {
        if (tIndex >= tvars.size()) {

            for (Tree master : set) { //query == null ? set : query.getTree()) {

                if (master.isExcluded()) {
                    continue;
                }

//                    t1.recalculate();
//                for (Domain d : master.getSystem()) {
//                    int res = d.execSystem();
//                    if ((res == 0 && !d.isAntc()) || (res == 1 && d.isAntc())) {
//                        allowed = false;
//                    }
//                }
//
//                if (!allowed) {
//                    master.setExcluded(true);
//                    continue;
//                }
//

//                for (TVariable t : tvars) {
//                    t.mark();
//                }

//                boolean occurrs = false;
                for (Tree slave : set) {

                    if (slave.isExcluded() /*|| (slave.getTVariables(true).isEmpty() && master.getTVariables(true).isEmpty())*/) {
                        continue;
                    }

//                    int saved = mind.getSubstituted().size();

//                    allowed = true;
////
//////                    t2.recalculate();
//                    for (Domain d : slave.getSystem()) {
//                        int res = d.execSystem();
//                        if ((res == 0 && !d.isAntc()) || (res == 1 && d.isAntc())) {
//                            allowed = false;
//                        }
//                    }
//
//                    if(!allowed) {
//                        break;
//                    }
//                    if (allowed) {
                    for (Domain d1 : master.getSequence()) {

//                        d1.calcFunctions();
//                        d1.execSystem();
                        for (Domain d2 : slave.getSequence()) {

//                            d2.execSystem();
                            //TODO: Очень сомнительно
                            if (d1.getId() != d2.getId()) {
                                if (d1.isAntc() != d2.isAntc() && d1.getPredicate().getId() == d2.getPredicate().getId()) {
                                    linkDomains(d1, d2, 0, logging, false);
//                                    occurrs = true;
                                }
                            }

//                            d2.calcFunctions();
                        }

//                        d1.calcFunctions();
                    }

//                        master.recalculate();

//                    }

//                    boolean allowed = true;
//allowed = true;
//
//                    for (Domain d : master.getSystem()) {
//                        int res = d.execSystem();
//                        if (res == 0) {
//                            allowed = false;
//                        }
//                    }
//
//                    slave.recalculate();
//                    for (Domain d : slave.getSystem()) {
//                        int res = d.execSystem();
//                        if (res == 0) {
//                            allowed = false;
//                        }
//                    }
//
//                    if (!allowed) {
//                        while (mind.getSubstituted().size() > saved) {
//                            TVariable tv = mind.getSubstituted().get(mind.getSubstituted().size() - 1);
//                            mind.getSubstituted().remove(mind.getSubstituted().size() - 1);
//                            tv.rollback();
//                        }
//                    }

                }

//                if(occurrs) {
                boolean allowed = true;
                for (Domain d : master.getSystem()) {
                    int res = d.execSystem();
                    if ((res == 0 && !d.isAntc()) || (res == 1 && d.isAntc())) {
                        allowed = false;
                    }
                }

                if (!allowed) {
//                    for (TVariable t : tvars) {
//                        t.release();
//                    }
                    master.setExcluded(true);
                } else {
//                    for (TVariable t : tvars) {
//                        t.commit();
//                    }
                    master.recalculate();
                }
//                }

            }

        } else {
            TVariable t = tvars.get(tIndex);
            if (t.rewind()) {
                do {
                    if (logging) {
                        mind.getLog().add(LogMode.ANALIZER, "LINK Value selected: " + t.getVarName() + "=" + t.getValue());
                    }
                    recurseLink(tvars, tIndex + 1, query, set, logging);
                } while (t.next());
            } else {
                recurseLink(tvars, tIndex + 1, query, set, logging);
            }
        }
    }

    public void link(boolean logging) throws RuntimeErrorException {
        mind.clearLinks();
        link(null, logging);
    }

    public void link(Right r, boolean logging) throws RuntimeErrorException {
        int pass = 0;
//        if (r == null) {
//            mind.clearQueryStatus();
//            mind.getTValues().clear();
//        }

        Set<Tree> set;
        if (r == null) {
            set = mind.getActualTrees();
//            mind.reset();
            //функции!
        } else {
            set = r.getActualTrees();
//            mind.clearQueryStatus();
        }


//        Screen.showRights(mind, true);
//        mind.getSubstituted().clear();
//        mind.getCalculated().clear();

        mind.clearQueryStatus();
        mind.getExcludedTrees().clear();

//        for (Tree t : set) {
//            for (Function f : t.getFunctions()) {
//                if (f.isCalculated()) {
//                    f.clearResult();
//                }
//            }
//
////            for (TVariable tv : t.getTVariables(true)) {
////                tv.clear();
////            }
//        }

//        if (r != null) {
//            for (Tree t : r.getTree()) {
//                for (TVariable tv : t.getTVariables(true)) {
//                    tv.clear();
//                }
//            }
//        }


        do {
            mind.getSubstituted().clear();
            mind.getCalculated().clear();

            if (logging) {
                mind.getLog().add(LogMode.ANALIZER, String.format("============= LINKER PASS %03x =============", ++pass));
            }

            Set<TVariable> tset = new HashSet<>();
            for (Tree t : set) {
                tset.addAll(t.getTVariables(true));

//                for(Function f: t.getFunctions()) {
//                    f.clearResult();
//                }
            }

//            if (r != null) {
//                for (Tree t : r.getTree()) {
//                    tset.addAll(t.getTVariables(true));
//                }
//            }


            recurseLink(new ArrayList<>(tset), 0, r, set, logging);
//            for (Tree t: set) {
//                for (Domain d : t.getSequence()) {
//                    d.execSystem();
//                }
//            }
//            for (Tree t : set) {
//                for (Function f : t.getFunctions()) {
//                    if (f.isSubstituted()) {
//                        f.clearResult();
//                        mind.getCalculator().calculate(f);
//                    }
//                }
//            }


//            for (Tree t : set) {
//                for (Function f : t.getFunctions()) {
//                    if (f.isCalculated()) {
//                        f.clearResult();
//                        mind.getCalculator().calculate(f);
//                    }
//                }
//            }

            set = mind.getActualTrees();

        } while (mind.getSubstituted().size() > 0 || mind.getCalculated().size() > 0);

    }

    private boolean logComparsion(Domain d) {
        if (d.isDest()) {
            for (TVariable t : d.getTVariables(true)) {
                if (t.getDstSolve().getPredicate().getId() == d.getPredicate().getId()) {
                    boolean found = false;
                    for (Domain r : t.getUsage()) {
                        if (d.getId() != r.getId()) {
                            mind.getLog().add(LogMode.ANALIZER, "Result: " + r.toString());
                            found = true;
                        }
                    }
                    if (!found) {
                        mind.getLog().add(LogMode.ANALIZER, "Confirmed: " + t.getSrcSolve());
//                        if (d.getRight().isQuery()) {
//                            a.getT().getDstSolve().setAcceptor(false);
//                        }
                    }
                    mind.getLog().add(LogMode.ANALIZER, "From right  : " + t.getRight().toString());
                    mind.getLog().add(LogMode.ANALIZER, "\tAcceptor: " + d.toString());
                    mind.getLog().add(LogMode.ANALIZER, "\tDonor   : " + t.getSrcSolve());
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
