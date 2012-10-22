/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.sfun.wcores;

import net.epsilony.simpmeshfree.model.sfun.WeightFunctionCore;

/**
 *
 * @author epsilon
 */
public abstract class WeightFunctionCoreImp implements WeightFunctionCore {
    protected int diffOrder;

    @Override
    public void setDiffOrder(int order) {
        this.diffOrder = order;
    }

    @Override
    public int getDiffOrder() {
        return diffOrder;
    }

    protected double[] initResultOutput(double[] ori) {
        if (null == ori) {
            return new double[diffOrder + 1];
        } else {
            return ori;
        }
    }
    
}
