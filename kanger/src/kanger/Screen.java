package kanger;

import kanger.compiler.Parser;
import kanger.compiler.SysOp;
import kanger.enums.Enums;
import kanger.enums.LibMode;
import kanger.enums.LogMode;
import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;
import kanger.primitives.*;
import jline.ConsoleReader;

//import java.awt.*;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by murray on 28.05.15.
 * $Author: murray $
 */
public class Screen {

    public static boolean LINE_EDITOR_ENABLE = false;

    //       System.getProperties().getProperty("kanger.disable.line.editor") == null
    //               || System.getProperties().getProperty("kanger.disable.line.editor").equals("false");

    public static void session(Mind mind) {
        boolean stop = false;

        ConsoleReader reader = null;
        if (LINE_EDITOR_ENABLE) {
            try {
                reader = new ConsoleReader();
            } catch (IOException e) {
                LINE_EDITOR_ENABLE = false;
            }
        }

        showCopyrigt(mind);

        while (!stop) {
            String line = "";
            try {
                if (LINE_EDITOR_ENABLE && reader != null) {
                    try {
                        Character c = 0;
                        System.out.printf("\n: ");
                        line = reader.readLine(c);
                    } catch (IOException e) {
                        LINE_EDITOR_ENABLE = false;
                    }
                }
                if (!LINE_EDITOR_ENABLE || reader == null) {
                    System.out.printf("\n: ");
                    line = new Scanner(System.in).nextLine();
                }
                if (line == null) line = "";
                if (line.length() > 0) {
                    switch (line.toUpperCase().charAt(0)) {
                        case 'Q':
//                            if (checkChg(mind)) {
                            stop = true;
//                            }
                            break;
                        case 'H':
                            showHelp();
                            break;
                        case 'R': {
                            Mind m = mind;
//                            int pos = 0;
//                            while (line.substring(pos).contains("..")) {
//                                int ps = line.indexOf("..");
//                                line = line.substring(0, ps) + line.substring(ps + 2);
//                                if (m.getParent() != null) {
//                                    m = m.getParent();
//                                }
//                            }
//                            line.replace("/", "");
                            showRights(m, line.charAt(0) != 'r');
                        }
                        break;
                        case 'B': {
                            Mind m = mind;
//                            int pos = 0;
//                            while (line.substring(pos).contains("..")) {
//                                int ps = line.indexOf("..");
//                                line = line.substring(0, ps) + line.substring(ps + 2);
//                                if (m.getParent() != null) {
//                                    m = m.getParent();
//                                }
//                            }
//                            line.replace("/", "");
                            showBase(m, line.charAt(0) != 'b', line.trim().contains(" ") ? line.split(" ")[1] : null);
                        }
                        break;
                        case 'F':
                            showFunctions(mind, line.charAt(0) != 'f');
                            break;
                        case 'L':
                            showHypo(mind);
                            break;
                        case 'V':
                            showLog(mind, LogMode.VALUES);
                            break;
                        case 'S':
                            showLog(mind, LogMode.SOLVES);
                            break;
                        case 'X':
                            showLog(mind, LogMode.ALL);
                            break;
//                    case 'A':
//                        lastQuery = savedQuery;
//                        break;
                        case 'K':
                            killRight(mind);
                            break;
//                        case 'T':
//                            showText(mind);
//                            break;
                        case 'I':
                            String h = makeHypo(mind);
                            if (h != null) {
//                                System.out.println(": " + h);
                                System.out.println();
                                Boolean res = mind.getAnalyser().query(h, false);
                                if (res != null) {
                                    showLog(mind, LogMode.SOLVES);
                                    showLog(mind, LogMode.VALUES);
                                }
                                System.out.println(mind.getLog().getCurrent().getRecord());
//                            lastQuery = savedQuery;
                            }
                            break;
                        case 'C': {
                            System.out.printf("Are you sure to clear workspace? [y/N]? ");
                            String s = new Scanner(System.in).nextLine().toUpperCase();
                            if (!s.isEmpty() && s.charAt(0) == 'Y') {
                                mind.clear();
//                                mind.rollback();
                            }
                        }
                        break;
//                        case 'E': {
//                            System.out.printf("Are you sure to clear working memory? [y/N]? ");
//                            String s = new Scanner(System.in).nextLine().toUpperCase();
//                            if (!s.isEmpty() && s.charAt(0) == 'Y') {
//                                mind.getText().delete(0, mind.getText().length());
//                                mind.clear();
//                            }
//                        }
//                        break;
//                        case 'P':
//                            saveSource(mind);
//                            break;
                        case 'G':
                            loadSource(mind);
                            break;
                        case 'Z':
                            saveCompiled(mind);
                            break;
                        case 'U':
                            loadCompiled(mind);
                            break;
                        case Enums.ANT:
                        case Enums.SUC:
//                        savedQuery = line;
//                        lastQuery = "";
                        case Enums.INS:
                        case Enums.DEL:
                        case Enums.WIPE:
//                            if (!Tools.isComplete(line, 0)) {
//                                incomplete = line;
//                                line = "";
//                            } else {
                            if (LINE_EDITOR_ENABLE) {
//                                reader.getHistory().getHistoryList().remove(0);
//                                reader.getHistory().addToHistory(line);
                            } else {
                                // StringSelection selec = new StringSelection(line);
                                // Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                // clipboard.setContents(selec, selec);
                            }

                            Boolean res = mind.getAnalyser().query(line, false);
                            if (res != null) {
                                showLog(mind, LogMode.SOLVES);
                                showLog(mind, LogMode.VALUES);
                            } else if (mind.getAnalyser().isInsertion()) {
                                showLog(mind, LogMode.SAVED);
                            }
                            System.out.println((mind.getLog().getCurrent()).getRecord());
                            if (res == null && !mind.getAnalyser().isInsertion()) {
                                showHypo(mind);
                            }
//                            }
                            break;
                        case Enums.FOO:
//                            if (!Tools.isComplete(line, 0)) {
//                                incomplete = line;
//                                line = "";
//                            } else {
                            SysOp op = (SysOp) mind.compileLine(line);
                            System.out.printf("SUCCESS: Library updated: =%s;\n", op.toString());
//                            }
                            break;
                        default:
                            System.out.printf("ERROR: Unknown Instruction\n");
                    }

                } else if (line.isEmpty()) {
                    showCopyrigt(mind);
                }
            } catch (ParseErrorException ex) {
                String x = ex.toString();
                int pos = Integer.parseInt(x.split("@")[0]);
                String msg = x.split("@")[1];
                System.out.println("ERROR: " + msg);
                System.out.println(line);
                while (pos-- > 0) System.out.print(" ");
                System.out.println("^");
                line = "";
//                incomplete = "";
            } catch (RuntimeErrorException e) {
                System.out.println(e.toString());
                //e.printStackTrace();
            }

        }
        System.out.println("KANGER III Session closed");

    }


    public static void showLog(Mind mind, LogMode type) {
        if (mind.getLog().size() > 0) {
            for (LogEntry log : mind.getLog().getRoot()) {
                if (type == LogMode.ALL || log.getType() == type) {
                    System.out.println(log.getRecord());
                }

            }
//            System.out.println();
        }
    }

    //    public static void showSolves(Mind context) {
//        if (context.getSolutions().size() > 0) {
//            System.out.println("Solves:");
//            int i = 0;
//            for (String log : (List<String>) context.getSolutions().getRoot()) {
//                System.out.println(String.format("\tSolve %03d: %s", ++i, log));
//            }
//            System.out.println();
//        }
//    }
//
//    public static void showValues(Mind context) {
//        if (context.getValues().size() > 0) {
//            System.out.println("Values:");
//            int i = 0;
//            for (String log : (List<String>) context.getValues().getRoot()) {
//                System.out.println(String.format("\tSolve %03d: %s", ++i, log));
//
//            }
//            System.out.println();
//        }
//    }
    public static void showCopyrigt(Mind mind) {
        System.out.printf("KANGER III, Version %s\n"
                + "Copiryght (C) 1986-%d, Gunn A. Qusnetsov, Dmitry G. Qusnetsov, All rights reserved!\n"
                + "Written by Dmitry G. Qusnetsov. Compiled: %s\n", Version.VERSION_S, Version.YEAR, Version.DATE_S);
//        System.out.printf("Context ID: %s\n", mind.getContextIdString());
    }

    //    public static int fixInsertion(contextAbstract context) {
//        int i = 0;
//        List<String> list = (List<String>) context.getHypotesisStore().getRoot();
//        if (list.size() > 0) {
//            System.out.printf("Predicated added:\n");
//            for (i = 0; i < list.size(); ++i) {
//                context.getText().append(context.getHypotesisStore().get(i));
//                context.getText().append("\r");
//                context.setChanged(true);
//                System.out.printf("  %3d:\t%s\n", i, context.getHypotesisStore().get(i));
//            }
//        }
//        return i;
//    }
    public static int showLine(StringBuffer c, int pos) {
        pos = Parser.skipSpaces(c.toString(), pos);
        while (pos < c.length() && c.charAt(pos) != Enums.EOLN) {
            if (c.charAt(pos) == '\t') {
                System.out.print("    ");
            } else {
                System.out.printf("%c", c.charAt(pos));
            }
            ++pos;
        }
        if (c.charAt(pos) == Enums.EOLN) {
            System.out.print(";");
            ++pos;
        }
        System.out.print("\n");
        return pos;
    }

    public static int skipLine(StringBuffer line, int pos) {
        while (pos < line.length() && line.charAt(pos) != '\n' && line.charAt(pos) != '\r') {
            ++pos;
        }
        return Parser.skipSpaces(line.toString(), pos);
    }

//    public static void showText(Mind context) {
//        StringBuffer c = context.getText();
//        int pos = 0;
//        int i = 0;
//        while (pos < c.length()) {
//            int p = Parser.skipSpaces(c.toString(), pos);
//            if (p < c.length() && c.charAt(p) != Enums.REM) {
//                System.out.printf("%3d: ", ++i);
//                pos = showLine(c, pos);
////                if (i > 0 && i % 19 == 0) {
////                    System.out.printf("\nPress ENTER to continue\n\n");
////                    new Scanner(System.in).next();
////                }
//            } else {
//                pos = skipLine(c, pos);
//            }
//        }
//    }
//
//    public static boolean checkChg(Mind context) {
//        if (context.isChanged()) {
//            System.out.printf("WARNING: Program text was changed. Are you sure [y/N] ? ");
//            String s = new Scanner(System.in).nextLine();
//            return s.toLowerCase().contains("y");
//        } else {
//            return true;
//        }
//    }

    public static void showHelp() {
        System.out.printf(
                "Available KEYWORDS:\n\n"
                        + "   HELP    - Get this message\n"
                        + "\n"
                        + "   ?       - Check for Rights Collisions\n"
                        + "   BASE    - View DataBase contents\n"
                        + "   RIGHTS  - View compiled-structured Rights list\n"
                        + "   FUNCS   - View defined Functions list\n"
                        + "   KILL    - Remove right\n"
                        + "   LIST    - View Hypotheses list after last work\n"
                        + "   INSERT  - Insert Hypotheses as right\n"
                        + "   AGAIN   - Repeat last question\n"
                        + "   XPLAIN  - Show explanation log\n"
                        + "   SOLVES  - Show solves list\n"
                        + "   VALUES  - Show values list\n"
//                        + "   TEXT    - Show source text\n"
                        + "   CLEAR   - Clear workspace\n"
//                        + "   ERASE   - Clear all working memory\n"
                        + "\n"
//                        + "   PUT     - Save Source file\n"
                        + "   GET     - Load Source file from disk\n"
                        + "   ZIP     - Save compiled code\n"
                        + "   UNZIP   - Load compiled code from file\n"
                        + "\n"
                        + "   QUIT    - Quit KANGER\n"
                        + "\n"
                        + "You can use just FIRST letter of keywords.\n"
        );
    }

    //    public static void showFunc(FArg f) {
//        System.out.printf(formatFunc(f));
//    }
    public static void showFunctions(Mind mind, boolean showSys) {

        if (mind.getLibrary().getRoot() != null) {
            System.out.printf("Defined functions:\n");
            int i = 0;
            for (SysOp op : mind.getLibrary().getRoot().values()) {
                if (op.getMode() == LibMode.FUNCTION) {
                    System.out.printf("Function %03d: %s;\n", ++i, op.toString());
                    if (showSys && !op.getScripts().isEmpty()) {
                        for (String s : op.asString().split("\n")) {
                            System.out.printf("\t%s\n", s);
                        }
                        System.out.printf("\n");
                    }
                }
            }
        }
//        if (mind.getFunctions().getRoot() != null) {
//            System.out.printf("Defined functions:\n");
//            for (FunctionDescriptor f = (FunctionDescriptor) mind.getFunctions().getRoot(); f != null; f = f.getNext()) {
//                System.out.printf("\t%s(%d);\n", f.getName(), f.getRange());
//            }
//        }
    }

    //    public static void showDomain(Domain d) {
//        System.out.printf(formatDomain(d));
//    }
    //    puts_subs(DOMAIN*d) {
//        char s[ MAXLINE];
//
//        sputs_subs(d, s);
//        printf("\t%s\n", s);
//    }
//
//    /*
//     * Returns 0 if all parameters defined
//     * or count of undefined
//     */
//    public static String formatPred(Predicate p, Solve s) {
//        String str = String.format("%c%s(", s.isAntc() ? Enums.ANT : Enums.SUC, p.getName());
//        for (int i = 0; i < p.getRange(); ++i) {
//            if (s.get(i) != null && s.get(i).getTerm().getType() == Enums.T_STRING) {
//                str += "\"";
//            }
//            str += String.format("%s", s.get(i) != null ? s.get(i).getTerm().getName() : "_");
//            if (s.get(i) != null && s.get(i).getTerm().getType() == Enums.T_STRING) {
//                str += "\"";
//            }
//
//            if (i + 1 < p.getRange()) {
//                str += String.format("%c", Enums.COMMA);
//            }
//        }
//        str += ");";
//        return str;
//    }
    //
    public static void showCauses(Mind mind, Domain s, int level) {
        String indent = "";
        for (int i = 0; i < level; ++i) {
            indent += "\t";
        }
        for (Right r : s.getCauses().keySet()) {
            if (r != null) {
                System.out.printf("\t\t%sRight: %s\n", indent, r.getOrig());
            }
            for (Domain so : s.getCauses().get(r)) {
                System.out.printf("\t\t%sCause: %s\n", indent, so.toString());
                showCauses(mind, so, level + 1);
            }
        }
    }

    public static void showPred(Mind mind, Predicate p, boolean showCauses) {
        System.out.printf("Predicate %s(%d) :\n", p.getName(), p.getRange());
        if (p.getSolves().isEmpty()) {
            System.out.printf("\tHas not solves\n");
        } else {
            for (Domain s : p.getSolves()) {
                System.out.printf("\t%s\n", s.toString());
                if (showCauses) {
                    showCauses(mind, s, 0);
                }
            }
        }
    }

    //
//
//    puts_loged_preds() {
//        PRED * p;
//        SOLVE * s;
//
//        for (p = Preds; p; p = p -> next)
//            for (s = p -> solve; s; s = s -> next)
//                if (s -> loged)
//                    puts_pred(p, s);
//    }
//
//
//    puts_loged() {
//        printf("Got from predicates:\n");
//        puts_loged_preds();
//        printf("Using right:\n");
//        view_line(Curr_right -> line);
//        printf("--- STEP ----------------------------------\n");
//        if (getch() == 27) {
//            printf("Explanation mode now is %s\n", "OFF");
//            Puts_log = 0;
//        }
//    }
//
//
//    puts_hone(PRED*p, int*cnt) {
//        SOLVE * s;
//        char line[ MAXLINE];
//
//        printf("Predicat %s(%d) :\n", p -> name, p -> range);
//        for (s = p -> hypo; s; s = s -> next) {
//            if (s -> cuted) continue;
//            make_hone(1, p, s, line);
//            printf("  %3d:\t.%s\n", * cnt, line + 1);
//            ++( * cnt);
//        }
//    }
//
//
    public static void showBase(Mind mind, boolean showCauses, String param) {
        for (Predicate p = mind.getPredicates().getRoot(); p != null; p = p.getNext()) {
            if (!p.getSolves().isEmpty() && !mind.getCalculator().exists(p) && (param == null || param.equals(p.getName()))) {
                showPred(mind, p, showCauses);
                System.out.printf("\n");
            }
        }
    }

    //
    public static void showHypo(Mind mind) {
        int i;
        List<Hypotese> list = mind.getHypotesisStore().getRoot();
        if (list != null && list.size() > 0) {
            System.out.printf("Hypotheses list:\n");
            for (i = 0; i < list.size(); ++i) {
                System.out.printf("\t%3d:\t%s\n", i + 1, list.get(i).toString());
            }
            System.out.printf("Use INSERT command for select Hypoteses\n");
        }
    }

    //
//
    public static List<List<String>> formatTree(Right r) {
        List<List<String>> list = new ArrayList<>();
        int depth = 0;
        for (Tree t : r.getTree()) {
            List<String> v = new ArrayList<>();
            v.add(t.isClosed() ? "C" : (t.isUsed() ? "U" : ""));
            list.add(v);
            int len = 0;
            for (Domain d : t.getSequence()) {
                String s = d.toString() + (d.isUsed() ? " *" : "");
                len = Math.max(len, s.length());
                v.add(s);
            }
            depth = Math.max(depth, v.size());
            for (int i = 0; i < v.size(); ++i) {
                String s = v.get(i);
                while (s.length() < len) {
                    s += " ";
                }
                v.set(i, s);
            }
        }
        for (List<String> v : list) {
            int len = v.get(0).length();
            String s = " ";
            while (s.length() < len) {
                s += " ";
            }
            while (v.size() < depth) {
                v.add(s);
            }
        }

        return list;
    }

    public static void showTree(Right r) {
        List<List<String>> net = formatTree(r);
        if (net.size() > 0 && net.get(0).size() > 0) {
            for (int i = 0; i < net.get(0).size(); ++i) {
                for (int k = 0; k < net.size(); ++k) {
                    System.out.print(net.get(k).get(i));
                    if (k + 1 < net.size()) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
        }
    }

    public static void showRights(Mind mind, boolean showTree) {
//        int i = 0;
        for (Right r = mind.getRights().getRoot(); r != null; r = r.getNext()) {
            if (showTree || r.getOrig().isEmpty()) {
                System.out.printf("\n -- Right %03d: %s\n", r.getId(), r.getOrig());
                showTree(r);
            } else {
                System.out.printf("Right %03d: %s\n", r.getId(), r.getOrig());
            }
        }
    }
//
//
//    puts_ptr(pos)
//
//    int pos;
//
//    {
//        while (pos) {
//            putchar(' ');
//            --pos;
//        }
//        printf("^\n");
//    }
//
//
//    puts_err(line)
//
//    char*line;
//
//    {
//        char*s;
//
//        printf("\n");
//        for (s = line;*s &&*s != EOLN;
//        ++s)
//        putchar( * s);
//        if (*s == EOLN)
//        putchar(';');
//        printf("\n");
//        puts_ptr(_err_pos - line);
//        switch (_err_code) {
//            case 0:
//                s = "Success";
//                break;
//            case 1:
//                s = "Right brackets mismatch";
//                break;
//            case 2:
//                s = "Must be ! or ? symbol";
//                break;
//            case 3:
//                s = "Semicolon required";
//                break;
//            case 4:
//                s = "Misplaced ~ symbol";
//                break;
//            case 5:
//                s = "Misplaced left bracket";
//                break;
//            case 6:
//                s = "Misplaced quantor symbol";
//                break;
//            case 7:
//                s = "Misplaced infix symbol";
//                break;
//            case 8:
//                s = "Empty term";
//                break;
//            case 9:
//                s = "Quantor variable mismatch";
//                break;
//            case 10:
//                s = "Symbol inside predicat";
//                break;
//            case 11:
//                s = "Misplaced comma";
//                break;
//            case 12:
//                s = "Misplaced term";
//                break;
//            case 13:
//                s = "Ivalid predicat name";
//                break;
//            default:
//                s = "System error";
//        }
//        printf("Syntax ERROR %d : %s\n", _err_code, s);
//    }

//    public static int skipSpaces(StringBuffer line, int pos) {
//        if (pos < line.length()) {
//            while (pos < line.length() && line.charAt(pos) <= ' ') {
//                ++pos;
//            }
//        }
//        return pos;
//    }

    /* Формирует в line строку гипотезы в качестве правила
     */
    public static String makeHypo(Mind mind) {
//        PRED *p;
//        SOLVE *s;
//        int antc;
//        int i;
//        char temp[MAXLINE];

        System.out.printf("Enter Hypotheses Number: ");
        int i = Integer.parseInt(new Scanner(System.in).nextLine());
        if (--i >= mind.getHypotesisStore().size()) {
            System.out.printf("ERROR: Wrong number\n");
            return null;
        }
        System.out.printf("Enter T(rue) or F(alse) for new right: ");
        String s = new Scanner(System.in).nextLine();
        int antc = (s.charAt(0) == 'T' || s.charAt(0) == 't') ? Enums.ANT : Enums.SUC;

        String temp = mind.getHypotesisStore().get(i).toString();
        if (antc == Enums.ANT) {
            return String.format("!%s;", temp.replace(String.format("%c", Enums.EOLN), ""));
        } else {
            return String.format("!~(%s);", temp.replace(String.format("%c", Enums.EOLN), ""));
        }
    }

    public static void killRight(Mind mind) {
        System.out.printf("Enter Right Number: ");
        int i = Integer.parseInt(new Scanner(System.in).nextLine());
        if (--i >= mind.getRights().size()) {
            System.out.printf("ERROR: Wrong number\n");
            return;
        }
        System.out.printf("Are you sure to remove right " + (i + 1) + " [y/N]? ");
        String s = new Scanner(System.in).nextLine();
        if (s.charAt(0) == 'Y' || s.charAt(0) == 'y') {
            Right q = null;
            Right r = mind.getRights().getRoot();
            for (int k = 0; k < i; ++k) {
                q = r;
                r = r.getNext();
            }

            if (q != null) {
                q.setNext(r.getNext());
                r.setNext(null);
            } else {
                mind.getRights().setRoot(r.getNext());
                r.setNext(null);
            }

//            String text = mind.getText().toString();
//            String orig = r.getOrig();
//
//            int start = text.indexOf(orig);
//            int end = start + orig.length();
//            while (end < text.length() && (text.charAt(end) == '\r' || text.charAt(end) == '\n' || text.charAt(end) == ' ' || text.charAt(end) == '\t')) {
//                ++end;
//            }
//            mind.getText().replace(start, end, "");
//            mind.setChanged(true);

            //TODO: Тут просто закомментировал. С удалением правил надо разобраться
//            for (Predicate p = mind.getPredicates().getRoot(); p != null; p = p.getNext()) {
//                Solve m = p.getSolve();
//                for (Solve o = p.getSolve(); o != null; o = o.getNext()) {
//                    if (o.getCauses().containsKey(r)) {
//                        o.getCauses().remove(r);
//                        if (o.getCauses().size() == 0) {
//                            if (m == o) {
//                                p.setSolve(o.getNext());
//                            } else {
//                                m.setNext(o.getNext());
//                            }
//                        }
//                    }
//                    m = o;
//                }
//            }
//            //mind.clear();
        }
    }

//    public static boolean saveSource(Mind context) {
//        Scanner scanner = new Scanner(System.in);
//        if (context.getSourceFileName().isEmpty()) {
//            context.setSourceFileName("context.k");
//        }
//        StringBuilder tmp = new StringBuilder(context.getText().toString().replace("\r", "\r\n"));
//
//        System.out.printf("Enter file name for save (%s): ", context.getSourceFileName());
//        String line = scanner.nextLine();
//        if (!line.isEmpty()) {
//            context.setSourceFileName(line);
//        } else {
//            line = context.getSourceFileName();
//        }
//
//        if (new File(line).exists()) {
//            System.out.print("WARNING: File already exists. Overwrite [y/N] ? ");
//            String ch = scanner.nextLine();
//            if (!(ch.startsWith("y") || ch.startsWith("Y"))) {
//                return false;
//            }
//        }
//
//        try {
//            BufferedWriter f = new BufferedWriter(new FileWriter(new File(line)));
//            f.write(tmp.toString());
//            f.flush();
//            f.close();
//            context.setChanged(false);
//
//            System.out.printf("File %s saved\n", line);
//            return true;
//        } catch (IOException ex) {
//            System.out.printf("ERROR: %s\n", ex);
//            return false;
//        }
//    }

    public static boolean loadSource(Mind mind) throws ParseErrorException, RuntimeErrorException {
        Scanner scanner = new Scanner(System.in);
//        if (checkChg(mind)) {
        List<String> list = new ArrayList<>();
        File[] dir = new File(".").listFiles();
        if (dir != null) {
            for (File f : dir) {
                if (!f.isDirectory() && f.getName().contains(".k")) {
                    list.add(f.getName());
                }
            }
        }

        if (list.size() > 0) {
            System.out.println("Files available:");
            int i = 0;
            int n = 1;
            int cnt = 4;
            for (String name : list) {
                System.out.printf("\t%d: %s", n, name);
                if (++i >= cnt) {
                    System.out.println();
                    i = 0;
                }
                ++n;
            }
        }

//                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
//                    Runtime.getRuntime().exec("dir /p *.k");
//                } else {
//                    Runtime.getRuntime().exec("ls *.k");
//                }
        System.out.printf("\nEnter file name %s%s: ", list.isEmpty() ? "" : "or file number", mind.getSourceFileName().isEmpty() ? "" : " (" + mind.getSourceFileName() + ")");
        String line = scanner.nextLine();

        try {
            int ps = Integer.parseInt(line);
            ps -= 1;
            if (ps < list.size()) {
                line = list.get(ps);
            }
        } catch (Exception ex) {
        }

        if (line.trim().isEmpty()) {
            line = mind.getSourceFileName();
        }
        return loadSourceFile(mind, line);
//        }
//        return false;
    }

    //TODO: Нужна проверка на наличие правила в базе на уровне дерева
    public static boolean loadSourceFile(Mind mind, String line) throws ParseErrorException, RuntimeErrorException {
        try {
            File f = new File(line);
            if (f.exists()) {
                final int length = (int) f.length();
                if (length != 0) {
                    char[] cbuf = new char[length];
                    InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
                    final int read = isr.read(cbuf);
                    StringBuffer buf = new StringBuffer(new String(cbuf).replace("\r\n", "\r"));
                    isr.close();
//                    mind.setText(buf);
//                    mind.setChanged(false);
                    mind.setSourceFileName(line);
//                    mind.rollback();
                    //TODO: Надо это?
                    mind.compile(buf.toString());
                    //mind.getAnalyser().analiser(true);
                    Boolean res = mind.query("?");
                    if (res != null && res) {
                        System.out.println(mind.getLog().getCurrent().getRecord());
                        System.out.printf("File %s loaded\n", line);
                        return true;
                    } else {
                        System.out.println(mind.getLog().getCurrent().getRecord());
                        System.out.printf("Use XPLAIN command for analisys\n");
                        return false;
                    }

                } else {
                    System.out.printf("WARNING: File %s is empty\n", line);
                }
            } else {
                System.out.printf("WARNING: File %s not found\n", line);
            }
        } catch (IOException ex) {
            System.out.printf("ERROR: %s\n", ex);
        }
        return false;
    }

    //    public static String readLine() {
//        return readLine(null);
//    }
//
//    public static String readLine(String line) {
//        try {
//            ConsoleReader console = new ConsoleReader();
//            console.setHistoryEnabled(true);
//            console.setPrompt("\n:");
//            if (line != null) {
//                console.println(line);
//            }
//            line = console.readLine();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                TerminalFactory.get().restore();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (line != null) {
//            return line;
//        } else {
//            return "";
//        }
//    }
    public static boolean saveCompiled(Mind mind) throws RuntimeErrorException, ParseErrorException {
        if (!mind.query("?")) {
            System.out.println(mind.getLog().getCurrent().getRecord());
            System.out.printf("Use XPLAIN command for analisys\n");
            return false;
        }
        Scanner scanner = new Scanner(System.in);
        if (mind.getCompiledFileName().isEmpty()) {
            mind.setCompiledFileName("mind.e");
        }
//        StringBuilder tmp = new StringBuilder(mind.getText().toString().replace("\r", "\r\n"));

        System.out.printf("Enter file name for save (%s): ", mind.getCompiledFileName());
        String line = scanner.nextLine();
        if (!line.isEmpty()) {
            mind.setCompiledFileName(line);
        } else {
            line = mind.getCompiledFileName();
        }

        if (new File(line).exists()) {
            System.out.print("WARNING: File already exists. Overwrite [y/N] ? ");
            String ch = scanner.nextLine();
            if (!(ch.startsWith("y") || ch.startsWith("Y"))) {
                return false;
            }
        }

        OutputStream dos = null;
        try {
            //BufferedWriter f = new BufferedWriter(new FileWriter(new File(line)));
            dos = new FileOutputStream(new File(line));
            mind.writeCompiledData(dos);
            System.out.printf("File %s saved\n", line);
            return true;
        } catch (IOException ex) {
            System.out.printf("ERROR: %s\n", ex);
            return false;
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ex) {
                    System.out.printf("ERROR: %s\n", ex);
                }
            }
        }
    }

    public static boolean loadCompiled(Mind mind) {
        Scanner scanner = new Scanner(System.in);
//        if (checkChg(mind)) {
        List<String> list = new ArrayList<>();
        File[] dir = new File(".").listFiles();
        if (dir != null) {
            for (File f : dir) {
                if (!f.isDirectory() && f.getName().contains(".e")) {
                    list.add(f.getName());
                }
            }
        }

        if (list.size() > 0) {
            System.out.println("Files available:");
            int i = 0;
            int n = 1;
            int cnt = 4;
            for (String name : list) {
                System.out.printf("\t%d: %s", n, name);
                if (++i >= cnt) {
                    System.out.println();
                    i = 0;
                }
                ++n;
            }
        }

//                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
//                    Runtime.getRuntime().exec("dir /p *.k");
//                } else {
//                    Runtime.getRuntime().exec("ls *.k");
//                }
        System.out.printf("\nEnter file name %s%s: ", list.isEmpty() ? "" : "or file number", mind.getCompiledFileName().isEmpty() ? "" : " (" + mind.getCompiledFileName() + ")");
        String line = scanner.nextLine();

        try {
            int ps = Integer.parseInt(line);
            ps -= 1;
            if (ps < list.size()) {
                line = list.get(ps);
            }
        } catch (Exception ex) {
        }

        if (line.trim().isEmpty()) {
            line = mind.getCompiledFileName();
        }

        return loadCompiledFile(mind, line);
//        }
//        return false;
    }

    public static boolean loadCompiledFile(Mind mind, String line) {
        InputStream dis = null;
        try {
            dis = new FileInputStream(new File(line));
            mind.readCompiledData(dis);
            mind.getAnalyser().analiser(true);
            System.out.printf("File %s loaded\n", line);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.printf("ERROR: %s\n", ex);
            return false;
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException ex) {
                    System.out.printf("ERROR: %s\n", ex);
                }
            }
        }
    }

//    private static void showInsertions(context context) {
//        for (LogEntry e : (List<LogEntry>) context.getLog().get(LogMode.SAVED)) {
//            System.out.println(e.getRecord());
//        }
//    }
}
