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

    private boolean linkDomains(Domain master, Domain slave, int level, boolean logging, boolean occurrs) {

        if (level >= master.getPredicate().getRange()) {

            if (logging && occurrs) {
                logComparsion(master);
                logComparsion(slave);
                mind.getLog().add(LogMode.ANALIZER, "-------------------------------------------");
            }
            return true;

        } else {
            //ПОДСТАНОВКИ
            for (int i = 0; i <= level; ++i) {

                if (master.get(level).isFSet() && !slave.get(level).isEmpty() && !master.get(level).getF().isCalculable() && !master.get(level).getF().isCalculated()) {
                    master.get(level).getF().setResult(slave.get(level).getValue());
                    occurrs = true;
                }

                if (slave.get(level).isFSet() && !master.get(level).isEmpty() && !slave.get(level).getF().isCalculable() && !slave.get(level).getF().isCalculated()) {
                    slave.get(level).getF().setResult(master.get(level).getValue());
                    occurrs = true;
                }

                if (master.get(level).isTSet() && !slave.get(level).isEmpty() && (!slave.isDest() || slave.isQuery())
                        && !master.get(level).getT().contains(slave.get(level).getValue())) {
                    try {
                        TValue s = master.get(level).getT().setValue(slave.get(level).getValue());
                        s.setDstSolve(master);
                        s.setSrcSolve(slave);
                        if (slave.isQuery() || master.isQuery()) {
                            master.get(level).getT().setQuery();
                        }
                        occurrs = true;
                    } catch (TValueOutOfOrver ex) {
                    }
                }

                if (slave.get(level).isTSet() && !master.get(level).isEmpty() && (!master.isDest() || master.isQuery())
                        && !slave.get(level).getT().contains(master.get(level).getValue())) {
                    try {
                        TValue s = slave.get(level).getT().setValue(master.get(level).getValue());
                        s.setSrcSolve(master);
                        s.setDstSolve(slave);
                        if (master.isQuery() || slave.isQuery()) {
                            slave.get(level).getT().setQuery();
                        }
                        occurrs = true;
                    } catch (TValueOutOfOrver ex) {
                    }
                }
            }
            return linkDomains(master, slave, level + 1, logging, occurrs);
        }

    }

    public void recurseLink(List<TVariable> tvars, int tIndex, Set<Tree> set, boolean logging) throws RuntimeErrorException {
        if (tIndex >= tvars.size()) {

            Set<Function> fs = new HashSet<>();
            Set<Domain> sd = new HashSet<>();

            for (Tree master : set) { //query == null ? set : query.getTree()) {
                if (master.isExcluded()) {
                    continue;
                }

                for (Tree slave : set) {
                    if (slave.isExcluded() || slave.isExcluded(master)) {
                        continue;
                    }


                    mind.getTValues().mark();

                    for (Domain d1 : master.getSequence()) {
                        fs.addAll(d1.getFunctions());
                        if (d1.isSystem()) {
                            sd.add(d1);
                        }

                        for (Domain d2 : slave.getSequence()) {
                            fs.addAll(d2.getFunctions());
                            if (d2.isSystem()) {
                                sd.add(d2);
                            }

                            if (d1.getId() != d2.getId()) {
                                if (d1.isAntc() != d2.isAntc() && d1.getPredicate().getId() == d2.getPredicate().getId()) {
                                    linkDomains(d1, d2, 0, logging, false);
                                }
                            }
                        }
                    }

                    boolean allowed = true;

                    for (Function f : fs) {
                        mind.getCalculator().calculate(f);
                    }
                    for (Domain d : sd) {
                        int res = d.execSystem();
                        if ((res == 0 && !d.isAntc()) || (res == 1 && d.isAntc())) {
                            allowed = false;
                        }
                    }

                    if (!allowed) {
                        mind.getTValues().release();
//                        master.setExcluded(true);
                    } else {
                        mind.getTValues().commit();
//                        master.recalculate();
                    }
                }
            }

        } else {
            TVariable t = tvars.get(tIndex);
            if (t.rewind()) {
                do {
                    mind.getSubstituted().add(t);
                    recurseLink(tvars, tIndex + 1, set, logging);
                } while (t.next());
                mind.getSubstituted().remove(t);
                recurseLink(tvars, tIndex + 1, set, logging);
            } else {
                recurseLink(tvars, tIndex + 1, set, logging);
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

        mind.getExcludedTrees().clear();
        mind.getSubstituted().clear();
        mind.getCalculated().clear();

        Set<Tree> set;
        if (r == null) {
            set = mind.getActualTrees();
            mind.clearQueryStatus();
//            mind.reset();
            //функции!
        } else {
            set = r.getActualTrees();
//            mind.clearQueryStatus();
        }


//        Screen.showRights(mind, true);
//        mind.getSubstituted().clear();
//        mind.getCalculated().clear();


        set = mind.getActualTrees();

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

//                for(Function f: t.getFunctions()) {~
//                    f.clearResult();
//                }
            }

//            if (r != null) {
//                for (Tree t : r.getTree()) {
//                    tset.addAll(t.getTVariables(true));
//                }
//            }


            recurseLink(new ArrayList<>(tset), 0, set, logging);
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

//            set = mind.getActualTrees();

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
