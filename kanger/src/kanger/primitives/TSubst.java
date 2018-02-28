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
        for(Argument a : srcSolve.getArguments()) {
            if(a.isTSet() && srcSolveValues.containsKey(a.getT())) {
                try {
                    a.getT().setValue(srcSolveValues.get(a.getT()));
                } catch (TValueOutOfOrver ex) {
                    //
                }
            }
        }
        return srcSolve;
    }
    
    public void setSolves(Domain dst, Domain src) {
        if(dst != null) {
            dstSolveValues.clear();
            for (Argument a : dst.getArguments()) {
                if (a.isTSet() && !a.getT().isEmpty()) {
                    dstSolveValues.put(a.getT(), a.getT().getValue());
                }
            }
            this.dstSolve = dst;
        }
        if(src != null) {
            srcSolveValues.clear();
            for (Argument a : src.getArguments()) {
                if (a.isTSet() && !a.getT().isEmpty()) {
                    srcSolveValues.put(a.getT(), a.getT().getValue());
                }
            }
            this.srcSolve = src;
        }
    }

    public Domain getDstSolve() {
        return dstSolve;
    }

    public Domain getDstValue() {
        for(Argument a : dstSolve.getArguments()) {
            if(a.isTSet() && dstSolveValues.containsKey(a.getT())) {
                try {
                    a.getT().setValue(dstSolveValues.get(a.getT()));
                } catch (TValueOutOfOrver ex) {
                    //
                }
            }
        }
        return dstSolve;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
