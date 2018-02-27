/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kanger;

import java.util.Set;

import kanger.enums.LogMode;
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

    private void linkDomains(Domain d1, Domain d2, int level, boolean logging, boolean occurrs) {
        if (level >= d1.getPredicate().getRange()) {
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
                        TVariable t = d1.get(i).getT();
                        //ВАЖНО! Для обработкаи запроса не помечаем уже имеющиеся предикаты
                        if (!d2.isAcceptor() && !d2.getRight().isQuery()) {
                            d1.setAcceptor(true);
                        }
                        TSubst s = t.setValue(d2.get(i).getValue());
                        s.setSrcSolve(d2);
                        s.setDstSolve(d1);
                        occurrs = true;
                        //}
                    } catch (TValueOutOfOrver ex) {
                    }
                }
                if (d2.get(i).isTSet() && !d1.get(i).isEmpty() && !d2.get(i).getT().contains(d1.get(i).getValue())) {
                    try {
                        TVariable t = d2.get(i).getT();
                        //ВАЖНО! Для обработки запроса не помечаем уже имеющиеся предикаты
                        if (!d1.isAcceptor() && !d1.getRight().isQuery()) {
                            d2.setAcceptor(true);
                        }
                        TSubst s = t.setValue(d1.get(i).getValue());
                        s.setSrcSolve(d1);
                        s.setDstSolve(d2);
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
    }

    private boolean logComparsion(Domain d) {
        if (d.isAcceptor()) {
            for (Argument a : d.getArguments()) {
                if (a.isTSet() && a.getT().getDstSolve().getPredicate().getId() == d.getPredicate().getId()) {
                    boolean found = false;
                    for (Domain r : a.getT().getSolves()) {
                        if (d.getId() != r.getId()) {
                            mind.getLog().add(LogMode.ANALIZER, "Result: " + r.toString());
                            found = true;
                        }
                    }
                    if (!found) {
                        mind.getLog().add(LogMode.ANALIZER, "Confirmed: " + a.getT().getSrcSolve());
//                        if (d.getRight().isQuery()) {
//                            a.getT().getDstSolve().setAcceptor(false);
//                        }
                    }
                    mind.getLog().add(LogMode.ANALIZER, "From right  : " + a.getT().getRight().toString());
                    mind.getLog().add(LogMode.ANALIZER, "\tAcceptor: " + d.toString());
                    mind.getLog().add(LogMode.ANALIZER, "\tDonor   : " + a.getT().getSrcSolve());
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
                    if (t1.getId() != t2.getId()) {
                        for (Domain d1 : t1.getSequence()) {
                            for (Domain d2 : t2.getSequence()) {
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
        mind.getAcceptorDomains().clear();
        link(null, logging);
    }

    public void link(Right r, boolean logging) {
        int pass = 0;
//        if (r == null) {
//            mind.clearQueryStatus();
//            mind.getTValues().clear();
//        }

        mind.getSubstituted().clear();
        Set<Tree> set;
        if (r == null) {
            set = mind.getActualTrees();
        } else {
            set = r.getActualTrees();
        }

        do {
            mind.getSubstituted().clear();
            if (logging) {
                mind.getLog().add(LogMode.ANALIZER, String.format("============= LINKER PASS %03x =============", ++pass));
            }
            recurseLink(mind.getTVars().getRoot(), set, logging);
            set = mind.getActualTrees();
        } while (mind.getSubstituted().size() > 0);

    }

}
