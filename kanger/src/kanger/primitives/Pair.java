package kanger.primitives;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 *
 * Пары имя-значение для подкванторных переменных
 */
public class Pair {

    private TVariable t = null;         // t-переменная
    private Term c = null;        // ее значение
    private Solve s = null;
    //private Pair next = null;

    public Pair(Term c, TVariable t, Solve s) {
        this.c = c;
        this.t = t;
        this.s = s;
    }

    public TVariable getT() {
        return t;
    }

    public void setT(TVariable t) {
        this.t = t;
    }

    public Term getC() {
        return c;
    }

    public void setC(Term c) {
        this.c = c;
    }

    public boolean isCSet() {
        return c != null;
    }

    public boolean isTSet() {
        return t != null;
    }

    public Solve getS() {
        return s;
    }

    public void setS(Solve s) {
        this.s = s;
    }

    @Override
    public boolean equals(Object pair) {
        if (pair == null || !(pair instanceof Pair)) {
            return false;
        } else {
            return ((t == null && !((Pair) pair).isTSet()) || (t != null && t.equals(((Pair) pair).getT())))
                    && ((c == null && !((Pair) pair).isCSet()) || (c != null && c.equals(((Pair) pair).getC())));
        }

    }
//    public Pair getNext() {
//        return next;
//    }
//
//    public void setNext(Pair next) {
//        this.next = next;
//    }
}
