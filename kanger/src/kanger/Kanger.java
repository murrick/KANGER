package kanger;

import java.util.logging.Level;
import java.util.logging.Logger;

import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 */
public class Kanger {

    public static void main(String[] args) {

        Mind mind = new Mind();
//        new LibraryStrings(mind);
//        new LibraryMath(mind);

//        mind.setText(new StringBuffer("!num(0);\r" +
//                        "!@x num(x), x < 20 -> num(++x);\r"));
//        mind.setText(new StringBuffer(
//                "!@x $y father(y,x);\n" +
//                        "!@x ~father(x,x);\n" +
//                        "!@x (male(x) || female(x)) && (~male(x) || ~female(x));\n" +
//                        "!@x ($y daughter(x,y)) -> female(x), issue(x,y);\n" +
//                        "!@x ($y son(x,y)) -> male(x), issue(x,y);\n" +
//                        "!@x @y father(x,y) -> male(x), issue(y,x);\n" +
//                        "!@x @y issue(y,x) -> (male(y) -> son(y,x)), (female(y) -> daughter(y,x));\n" +
//                        "!@x @y issue(x,y) -> (male(y) -> father(y,x)), (female(y) -> mother(y,x));\n" +
//                        "!father(John, Tom);\n" +
//                        "!daughter(Sarah, John);\n" +
//                        "!age(John, 37);\n" +
//                        "!age(Tom, 12);\n" +
//                        "!age(Sarah, 4);" +
//                        "!@x @y @a @b age(x,a), age(y,b), a > b -> older(x,y);\n" +
//                        "!@x @y @a @b age(x,a), age(y,b), a < b -> younger(x,y);\n"
//        ));
//        mind.compile();
//        mind.setText(new StringBuffer(
//                "!@x $y father(y,x); " +
//                "!@x ~father(x,x);"
//        ));
//        mind.compile();
//        mind.setText(new StringBuffer(
//                " * это комментарий\r" +
//                        "!num(0); *** и это тоже ;;;\r" +
//                        "!@x (num(x), x < 3) -> num(++x);\r"
//        ));
//        mind.setText(new StringBuffer(
//                "!@x child(x) -> ((male(x) -> boy(x)), (female(x) -> girl(x)));\n" +
//                "!@x @y fath(x,y) -> (child(y), male(x), native(x,y));\n" +
//                "*!@x @y daug(x,y) -> (fath(y,x), child(x), female(x));\n" +
//                "*!@x @y son(x,y) -> (fath(y,x), child(x), male(x));\n" +
//                "*!@x @y (fath(x,y), male(y)) } son(y,x);\n" +
//                "*!@x @y (fath(x,y), female(y)) -> daug(y,x);\n" +
//                "!@x boy(x) -> (male(x), child(x));\n" +
//                "!@x girl(x) -> (female(x), child(x));\n" +
//                "*!@x ~fath(x,x);\n" +
//                "*!@x ~daug(x,x);\n" +
//                "*!@x ~son(x,x);\n" +
//                "*!@x $y fath(y,x);\n" +
//                "!@x ~(male(x), female(x));\n" +
//                "!@x male(x) || female(x);\n"));

//        mind.setText(new StringBuffer("=add(a,b) {add = a+b;};"));
//        if (args.length > 0 && new File(args[0]).exists()) {
//            if (args[0].endsWith(".e")) {
//                Screen.loadCompiledFile(mind, args[0]);
//            } else {
//                Screen.loadSourceFile(mind, args[0]);
//            }
//        }
//
//        mind.compile();

//        try {
//            mind.compile("!@x num(x), x < 20 -> num(++x);");
//            mind.compile("!num(0);");
////            mind.compile("!num(0);");
////            mind.compile("!@x num(x), x < 10 -> num(++x);");
//        } catch (ParseErrorException e) {
//            e.printStackTrace();
//        } catch (RuntimeErrorException e) {
//            e.printStackTrace();
//        }

//        try {
//            mind.compile("!$x a(x,ttt);");
//            mind.compile("!$x ~a(x,ttt);");
//            mind.compile("!@x b(x,ttt);");
//            mind.compile("!@x ~c(x,ttt);");
//        } catch (ParseErrorException e) {
//            e.printStackTrace();
//        } catch (RuntimeErrorException e) {
//            e.printStackTrace();
//        }

//        try {
//
//            mind.compile("!@x a(x) -> b(x), @y b(y) -> c(y), @z c(z) -> d(z); "
//                    + "!a(mmm); "
//                    + "!a(nnn); "
//                    + "!b(ooo); "
//                    + "!d(v); " +
//
//                    "!@x a(x) -> ~n(x); ");
//
//        } catch (ParseErrorException ex) {
//            Logger.getLogger(Kanger.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (RuntimeErrorException ex) {
//            Logger.getLogger(Kanger.class.getName()).log(Level.SEVERE, null, ex);
//        }

        try {

            mind.compile(
                    "!num(0); "
                    + "!@x num(x) && x < 10 -> num(++x);");

        } catch (ParseErrorException ex) {
            Logger.getLogger(Kanger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeErrorException ex) {
            Logger.getLogger(Kanger.class.getName()).log(Level.SEVERE, null, ex);
        }

        Screen.session(mind);

//        mind.compileLine[jhjij("!@x ~a(x) || b(x);");
//        mind.compileLine("!@x ~a(x) -> b(x);");
//        mind.compileLine("!@x ~a(x), b(x);");
//        mind.compileLine("!@x $y ~a(x,y), f(y, aaa) -> b(x,y);");
//        Screen.showRights(mind);
//        Compiler c = new Compiler(mind);
//        c.compileLine(new StringBuffer("!@(x) a(b);"), 0);
    }
}
