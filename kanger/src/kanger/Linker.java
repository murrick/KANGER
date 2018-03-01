/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kanger;

import java.util.Set;

import kanger.enums.LogMode;
import kanger.exception.RuntimeErrorException;
import kanger.exception.TValueOutOfOrver;
import kanger.primitives.Argument;
import kanger.primitives.Domain;
import kanger.primitives.Right;
import kanger.primitives.TSubst;
import kanger.primitives.TVariable;
import kanger.primitives.Tree;

/**
 * @author murray
 */
public class Linker {

    private final Mind mind;

    public Linker(Mind mind) {
        this.mind = mind;
    }

    private boolean linkDomains(Domain d1, Domain d2, int level, boolean logging, boolean occurrs) {
        if (level >= d1.getPredicate().getRange()) {

            execSystem(d1);
            execSystem(d2);

            if (logging && occurrs) {
                logComparsion(d1);
                logComparsion(d2);
                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");
            }

        } else {
            //ПОДСТАНОВКИ
            for (int i = 0; i <= level; ++i) {

                if (d1.get(i).isTSet() && !d2.get(i).isEmpty() && !d1.get(i).getT().contains(d2.get(i).getValue())) {
                    try {
                        //ВАЖНО! Для обработкаи запроса не помечаем уже имеющиеся предикаты
//                        if (!d2.isAcceptor() && !d2.getRight().isQuery()) {
//                            d1.setAcceptor(true);
//                        }
                        TSubst s = d1.get(i).getT().setValue(d2.get(i).getValue());
                        s.setSolves(d1, d2);
//                        d1.setDestFor(d2);
                        occurrs = true;
                        //}
                    } catch (TValueOutOfOrver ex) {
                    }
                }
                if (d2.get(i).isTSet() && !d1.get(i).isEmpty() && !d2.get(i).getT().contains(d1.get(i).getValue())) {
                    try {
                        //ВАЖНО! Для обработки запроса не помечаем уже имеющиеся предикаты
//                        if (!d1.isAcceptor() && !d1.getRight().isQuery()) {
//                            d2.setAcceptor(true);
//                        }
                        TSubst s = d2.get(i).getT().setValue(d1.get(i).getValue());
                        s.setSolves(d2, d1);
//                        d2.setDestFor(d1);
                        occurrs = true;
                        //}
                    } catch (TValueOutOfOrver ex) {
                    }
//                } else if (d1.get(i).isTSet() && d2.get(i).isTSet() && d1.get(i).isEmpty() && d2.get(i).isEmpty()) {
//                    //TODO: Спорный момент - генерация временной переменной при сравнении двух пустых t-переменных
//                    Term c = mind.getTerms().get();
//                    try {
//                        TSubst s = d1.get(i).getT().setValue(c);
//                        s.setSrcSolve(d2);
//                        s.setDstSolve(d1);
//                        d1.setAcceptor(true);
//                    } catch (TValueOutOfOrver ex) {
//                        return false;
//                    }
//                    try {
//                        TSubst s = d2.get(i).getT().setValue(c);
//                        s.setSrcSolve(d1);
//                        s.setDstSolve(d2);
//                        d2.setAcceptor(true);
//                    } catch (TValueOutOfOrver ex) {
//                        return false;
//                    }
                }
            }
            linkDomains(d1, d2, level + 1, logging, occurrs);
        }

        return occurrs;
    }

    private boolean execSystem(Domain d) {
        if (mind.getCalculator().exists(d.getPredicate())) {
            try {
                int result = mind.getCalculator().execute(d);
                return true;
            } catch (RuntimeErrorException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void calcFunctions(Set<Tree> set) {
        for (Tree t : set) {
            for (Domain d : t.getSequence()) {
                execSystem(d);
            }
            for (Domain d : t.getSequence()) {
                for (Argument a : d.getArguments()) {
                    if (a.isFSet()) {
                        try {
                            mind.getCalculator().calculate(a.getF());
                        } catch (RuntimeErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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

    public void recurseLink(TVariable t, Set<Tree> set, boolean logging) {
        if (t == null) {

            for (Tree t1 : set) {
                for (Tree t2 : set) {
                    for (Domain d1 : t1.getSequence()) {
                        for (Domain d2 : t2.getSequence()) {
                            if (t1.getId() != t2.getId()) {
                                if (d1.isAntc() != d2.isAntc() && d1.getPredicate().getId() == d2.getPredicate().getId()) {
                                    linkDomains(d1, d2, 0, logging, false);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            t.rewind();
            do {
                recurseLink(t.getNext(), set, logging);
            } while (t.next());
        }
    }

    public void link(boolean logging) {
        mind.dropLinks();
        link(null, logging);
    }

    public void link(Right r, boolean logging) {
        int pass = 0;
//        if (r == null) {
//            mind.clearQueryStatus();
//            mind.getTValues().clear();
//        }

        mind.getSubstituted().clear();
        mind.getCalculated().clear();

        Set<Tree> set;
//        if (r == null) {
            set = mind.getActualTrees();
//        } else {
//            set = r.getActualTrees();
//        }

        do {
            mind.getSubstituted().clear();
            mind.getCalculated().clear();
            if (logging) {
                mind.getLog().add(LogMode.ANALIZER, String.format("============= LINKER PASS %03x =============", ++pass));
            }
            recurseLink(mind.getTVars().getRoot(), set, logging);
            calcFunctions(set);
//            set = mind.getActualTrees();
        } while (mind.getSubstituted().size() > 0 || mind.getCalculated().size() > 0);

    }


}
