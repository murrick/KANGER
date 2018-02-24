package kanger.compiler;

import kanger.Mind;
import kanger.enums.Enums;
import kanger.enums.ParseError;
import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;
import kanger.primitives.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by murray on 25.05.15.
 */
public class Compiler {

    private Mind mind;

    public Compiler(Mind mind) {
        this.mind = mind;
    }

    public Right compileLine(PTree root, boolean antc) throws ParseErrorException, RuntimeErrorException {

        //mind.getReplacements().reset();
        mind.resetDummy();
        Right r = mind.getRights().add();

//        NodeFactory n = new NodeFactory();
//        buildNet(n, root, antc, Node.STILL);
//        t = recurseTree(n.getRoot());
        Tree t = new Tree();
        r.getTree().add(t);
        buildNet(r, t, root, antc, new HashMap<String, Argument>());
        mind.resetDummy();

//        r.setT(t);

//        mind.getCalculator().recalculate(-13);
        /*
         * Если правило содержит одну ветку -
         * пытаемся завалить в б.д. все полные предикаты
         * из нее.
         */
//        if (t.getRight() == null) {
////            for (Tree t = tree; t != null; t = t.getRight()) {
//            for (Tree v = t; v != null; v = v.getDown()) {
//                List<Argument> arg = new ArrayList<>();
//                if (mind.getAnalyser().fillSolve(arg, v.getD(), v.getD().isAntc()) != 0) {
//                    mind.getAnalyser().addBase(arg, v.getD(), false /*v.getD().getP(), v.getD().isAntc()*/);
//                }
//            }
////            }
////            mind.getCalculator().recalculate(-13);
//        }

        return r;
    }

    private List<Tree> buildNet(Right r, Tree t, PTree root, boolean antc, Map<String, Argument> replacements) throws ParseErrorException {
        List<Tree> list = new ArrayList<>();
        switch (root.getName().charAt(0)) {
            case Enums.NOT:
                list.addAll(buildNet(r, t, root.getLeft(), !antc, replacements));
                break;

            case Enums.AQN:
            case Enums.PQN:
                compileQuantor(root, antc, replacements);
                list.addAll(buildNet(r, t, root.getRight(), antc, replacements));
                break;

            case Enums.CON:
            case Enums.COMMA:
                if (antc) {
                    Tree x = r.cloneTree(t);
                    list.addAll(buildNet(r, t, root.getLeft(), antc, replacements));
                    list.addAll(buildNet(r, x, root.getRight(), antc, replacements));
                    list.add(x);
                } else {
                    list.addAll(buildNet(r, t, root.getLeft(), antc, replacements));
                    List<Tree> tmp = new ArrayList<>();
                    for(Tree x : list) {
                        tmp.addAll(buildNet(r, x, root.getRight(), antc, replacements));
                    }
                    list.addAll(tmp);
                    list.addAll(buildNet(r, t, root.getRight(), antc, replacements));
                }
                break;

            case Enums.DIS:
                Tree x = r.cloneTree(t);
                list.addAll(buildNet(r, t, root.getLeft(), antc, replacements));
                list.addAll(buildNet(r, x, root.getRight(), antc, replacements));
                list.add(x);
                break;

            case Enums.IMP:
                if (antc) {
                    list.addAll(buildNet(r, t, root.getLeft(), !antc, replacements));
                    List<Tree> tmp = new ArrayList<>();
                    for(Tree z : list) {
                        tmp.addAll(buildNet(r, z, root.getRight(), antc, replacements));
                    }
                    list.addAll(tmp);
                    list.addAll(buildNet(r, t, root.getRight(), antc, replacements));
                } else {
                    x = r.cloneTree(t);
                    list.addAll(buildNet(r, t, root.getLeft(), !antc, replacements));
                    list.addAll(buildNet(r, x, root.getRight(), antc, replacements));
                    list.add(x);
                }
                break;

            case Enums.LB:
                if (root.getLeft() == null) {
                    list.addAll(buildNet(r, t, root.getRight(), antc, replacements));
                } else {
                    compilePredicate(t, root, antc, replacements);
                }
                break;

            default: {
                compilePredicate(t, root, antc, replacements);
            }
        }
        return list;
    }

    private void compileQuantor(PTree root, boolean antc, Map<String, Argument> replacements) throws ParseErrorException {
        if (replacements.containsKey(root.getLeft().getName())) {
            throw new ParseErrorException(root.getPos(), ParseError.AVAR);
        }

        Argument p = null;
        if ((root.getName().charAt(0) == Enums.AQN && antc) || (root.getName().charAt(0) == Enums.PQN && !antc)) {
            p = new Argument(mind.getTVars().add());
            p.getT().setName(root.getLeft().getName());
        } else if ((root.getName().charAt(0) == Enums.AQN && !antc) || (root.getName().charAt(0) == Enums.PQN && antc)) {
            p = new Argument(mind.getTerms().get());
        }
        replacements.put(root.getLeft().getName(), p);
    }

    private Tree compilePredicate(Tree t, PTree root, boolean antc, Map<String, Argument> replacements) {
        List<Argument> arg = new ArrayList<>();
        Predicate pred = null;
        if (root.isSystem()) {
            // системный предикат
            // ПРОВЕРКА НЛ LB НЕ НУЖНА! Т.К. ОНА ОБРАБАТЫВАЕТСЯ
            parseArgs(arg, root, 0, replacements);
            pred = mind.getPredicates().add(root.getName(), arg.size());
        } else if (root.getLeft() == null) {
            pred = mind.getPredicates().add(root.getName(), 0);
        } else {
            parseArgs(arg, root.getRight(), 1, replacements);
            pred = mind.getPredicates().add(root.getLeft().getName(), arg.size());
        }
        t.getSequence().add(mind.getDomains().add(pred, antc, arg, mind.getRights().getRoot()));
        return t;
    }

    private void parseArgs(List<Argument> arg, PTree root, int level, Map<String, Argument> replacements) {
        int s;

        if (root == null) {
        } else if (root.isSystem()) {
            if (level == 0) {
                parseArgs(arg, root.getLeft(), level + 1, replacements);
                parseArgs(arg, root.getRight(), level + 1, replacements);
            } else {
                // системная функция

                Function f = new Function();
                parseArgs(f.getA(), root.getLeft(), level + 1, replacements);
                parseArgs(f.getA(), root.getRight(), level + 1, replacements);
                f.setName(mind.getTerms().add(root.getName()));
                f.setRange(f.getA().size());
                Argument t = new Argument(f);
                arg.add(t);
            }
        } else if (root.getName().charAt(0) == Enums.COMMA) {
            parseArgs(arg, root.getLeft(), level + 1, replacements);
            parseArgs(arg, root.getRight(), level + 1, replacements);
        } else if (root.getName().charAt(0) == Enums.LB) {
            // вложенная функция
            Function f = new Function();
            parseArgs(f.getA(), root.getRight(), level + 1, replacements);
            f.setName(mind.getTerms().add(root.getLeft().getName()));
            f.setRange(f.getA().size());
            Argument t = new Argument(f);
            arg.add(t);
        } else {
            Argument t;
            if ((t = replacements.get(root.getName())) == null) {
                t = new Argument(mind.getTerms().add(root.getName()));
            }
            arg.add(t);
        }
    }
}
