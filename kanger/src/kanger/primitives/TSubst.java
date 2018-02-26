/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kanger.primitives;

/**
 *
 * @author murray
 */
public class TSubst {
    private Term value = null;
    private Domain srcSolve = null;
    private Domain dstSolve = null;
    private boolean success = false;

    public Term getValue() {
        return value;
    }

    public void setValue(Term value) {
        this.value = value;
    }

    public Domain getSrcSolve() {
        return srcSolve;
    }

    public void setSrcSolve(Domain solve) {
        this.srcSolve = solve;
    }

    public Domain getDstSolve() {
        return dstSolve;
    }

    public void setDstSolve(Domain solve) {
        this.dstSolve = solve;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    
}
