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

    private boolean linkDomains(Domain d1, Domain d2, int level, boolean logging, boolean occurrs) throws RuntimeErrorException {

        if (level >= d1.getPredicate().getRange()) {

            if (d1.recalculate()) {
                occurrs = true;
            }
            if (d2.recalculate()) {
                occurrs = true;
            }

            if (logging && occurrs) {
                logComparsion(d1);
                logComparsion(d2);
                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");
            }

            return true;

        } else {
            //ПОДСТАНОВКИ
            //for (int i = 0; i <= level; ++i) {
//            int level = level;
            if (d1.get(level).isFSet() && !d2.get(level).isEmpty() && !d1.get(level).getF().isCalculable() && !d1.get(level).getF().isCalculated()) {
                d1.get(level).getF().setResult(d2.get(level).getValue());
//				d1.setUsed(true);
                occurrs = true;
            }
            if (d2.get(level).isFSet() && !d1.get(level).isEmpty() && !d2.get(level).getF().isCalculable() && !d2.get(level).getF().isCalculated()) {
                d2.get(level).getF().setResult(d1.get(level).getValue());
//				d2.setUsed(true);
                occurrs = true;
            }
            if (d1.get(level).isTSet() && !d2.get(level).isEmpty() && !d2.isDest() && !d1.get(level).getT().contains(d2.get(level).getValue())) {
                try {
                    //ВАЖНО! Для обработкаи запроса не помечаем уже имеющиеся предикаты
//                        if (!d2.isAcceptor() && !d2.getRight().isQuery()) {
//                            d1.setAcceptor(true);
//                        }
                    TSubst s = d1.get(level).getT().setValue(d2.get(level).getValue());
                    s.setSolves(d1, d2);
                    d1.setUsed(true);
//                        d1.setDestFor(d2);
                    occurrs = true;

//                    d1.calcFunctions();
                    //}
                } catch (TValueOutOfOrver ex) {
                }
            }
            if (d2.get(level).isTSet() && !d1.get(level).isEmpty() && !d1.isDest() && !d2.get(level).getT().contains(d1.get(level).getValue())) {
                try {
                    //ВАЖНО! Для обработки запроса не помечаем уже имеющиеся предикаты
//                        if (!d1.isAcceptor() && !d1.getRight().isQuery()) {
//                            d2.setAcceptor(true);
//                        }
                    TSubst s = d2.get(level).getT().setValue(d1.get(level).getValue());
                    s.setSolves(d2, d1);
                    d2.setUsed(true);
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
//            }
            return linkDomains(d1, d2, level + 1, logging, occurrs);
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

            for (Tree t1 : query == null ? set : query.getTree()) {

                for (Tree t2 : set) {

                    int saved = mind.getSubstituted().size();

//                    boolean allowed = true;
////                    t1.recalculate();
//                    for (Domain d : t1.getSystem()) {
//                        int res = d.execSystem();
//                        if (res == 0 /*|| d.isAntc()*/) {
//                            allowed = false;
//                        }
//                    }
//
////                    t2.recalculate();
//                    for (Domain d : t2.getSystem()) {
//                        int res = d.execSystem();
//                        if (res == 0 /*|| d.isAntc()*/) {
//                            allowed = false;
//                        }
//                    }
//                    if (allowed) {
                        for (Domain d1 : t1.getSequence()) {

//                        d1.calcFunctions();
//                        d1.execSystem();
                            for (Domain d2 : t2.getSequence()) {

//                            d2.execSystem();
                                //TODO: Очень сомнительно
                                if (d1.getId() != d2.getId()) {
                                    if (d1.isAntc() != d2.isAntc() && d1.getPredicate().getId() == d2.getPredicate().getId()) {
                                        linkDomains(d1, d2, 0, logging, false);
                                    }
                                }

//                            d2.calcFunctions();
                            }

//                        d1.calcFunctions();
                        }
//                    }

                    boolean allowed = true;
//allowed = true;
//
                    t1.recalculate();
                    for (Domain d : t1.getSystem()) {
                        int res = d.execSystem();
                        if (res == 0 /*|| d.isAntc()*/) {
                            allowed = false;
                        }
                    }

                    t2.recalculate();
                    for (Domain d : t2.getSystem()) {
                        int res = d.execSystem();
                        if (res == 0 /*|| d.isAntc()*/) {
                            allowed = false;
                        }
                    }

                    if (!allowed) {
                        while (mind.getSubstituted().size() > saved) {
                            TVariable tv = mind.getSubstituted().get(mind.getSubstituted().size() - 1);
                            mind.getSubstituted().remove(mind.getSubstituted().size() - 1);
                            tv.rollback();
                        }
                    }

                }

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
        mind.dropLinks();
        link(null, logging);
    }

    public void link(Right r, boolean logging) throws RuntimeErrorException {
        int pass = 0;
//        if (r == null) {
//            mind.clearQueryStatus();
//            mind.getTValues().clear();
//        }

        mind.getSubstituted().clear();
        mind.getCalculated().clear();

        Set<Tree> set;
        if (r == null) {
            set = mind.getActualTrees();
            //функции!
        } else {
            set = r.getActualTrees();
            mind.clearQueryStatus();
        }

//        Screen.showRights(mind, true);
        mind.getExcludedTrees().clear();
        do {
//            mind.getSubstituted().clear();
//            mind.getCalculated().clear();

            if (logging) {
                mind.getLog().add(LogMode.ANALIZER, String.format("============= LINKER PASS %03x =============", ++pass));
            }

//            for (Tree t: set) {
//                for (Domain d : t.getSequence()) {
//                    d.execSystem();
//                }
//            }
//            for (Tree t: set) {
//                for (Domain d : t.getSequence()) {
//                    d.calcFunctions();
//                }
//            }
            mind.getCalculated().clear();
//            for(Tree t : set) {
//                for(Domain d : t.getSequence()) {
//                    if(d.execSystem() == 0) {
//                        t.setExcluded(true);
//                    }
//                }
//            }

            mind.getSubstituted().clear();

            Set<TVariable> tset = new HashSet<>();
            for (Tree t : set) {
                tset.addAll(t.getTVariables(true));
            }
            
            if (r != null) {
                for (Tree t : r.getTree()) {
                    tset.addAll(t.getTVariables(true));
                }
            }

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
            set = mind.getActualTrees();
        } while (mind.getSubstituted().size() > 0 /*|| mind.getCalculated().size() > 0*/);

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
