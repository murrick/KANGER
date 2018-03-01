/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kanger.primitives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kanger.exception.TValueOutOfOrver;

/**
 *
 * @author murray
 */
public class TSubst {
    private Term value = null;
    private boolean success = false;
    
    private Domain srcSolve = null;
    private Map<TVariable, Term> srcSolveValues = new HashMap<>();
    private Domain dstSolve = null;
    private Map<TVariable, Term> dstSolveValues = new HashMap<>();

    public Term getValue() {
        return value;
    }

    public void setValue(Term value) {
        this.value = value;
    }

    public Domain getSrcSolve() {
        return srcSolve;
    }

    public Domain getSrcValue() {
//        for(TVariable t : srcSolve.getTVariables(true)) {
//            if(srcSolveValues.containsKey(t)) {
//                try {
//                    t.setValue(srcSolveValues.get(t));
//                } catch (TValueOutOfOrver ex) {

//                }
//            }
//        }
        return srcSolve;
    }
    
    public void setSolves(Domain dst, Domain src) {
        if(dst != null) {
//            dstSolveValues.clear();
//            for (TVariable t : dst.getTVariables(true)) {
//                if (!t.isEmpty()) {
//                    dstSolveValues.put(t, t.getValue());
//                }
//            }
            this.dstSolve = dst;
        }
        if(src != null) {
//            srcSolveValues.clear();
//            for (TVariable t : src.getTVariables(true)) {
//                if (!t.isEmpty()) {
//                    srcSolveValues.put(t, t.getValue());
//                }
//            }
            this.srcSolve = src;
        }
    }

    public Domain getDstSolve() {
        return dstSolve;
    }

    public Domain getDstValue() {
//        for(TVariable t : dstSolve.getTVariables(true)) {
//            if(dstSolveValues.containsKey(t)) {
//                try {
//                    t.setValue(dstSolveValues.get(t));
//                } catch (TValueOutOfOrver ex) {

//                }
//            }
//        }
        return dstSolve;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
