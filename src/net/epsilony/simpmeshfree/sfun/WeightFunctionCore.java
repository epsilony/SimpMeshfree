/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import net.epsilony.simpmeshfree.utils.PartDiffOrdered;

/**
 *
 * @author epsilon
 */
public interface WeightFunctionCore extends PartDiffOrdered{
    double[] valuesByNormalisedDistSq(double distSq,double[] results);
}
