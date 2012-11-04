/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun.wcores;

import java.util.Arrays;
import net.epsilony.simpmeshfree.sfun.WeightFunctionCore;
import net.epsilony.utils.SomeFactory;

/**
 *
 * @author epsilon
 */
public class TriSpline extends WeightFunctionCoreImp {

    @Override
    public double[] valuesByNormalisedDistSq(double disSq, double[] results) {
        results = initResultOutput(results);
        if (disSq >= 1) {
            Arrays.fill(results, 0);
            return results;
        }
        double dis = Math.sqrt(disSq);
        if (dis <= 0.5) {
            results[0] = 2 / 3.0 + 4 * disSq * (-1 + dis);
            //2/3.0-4*r^2+4*r^3    : dis=r disSq=r^2=q;
            //=2/3.0-4*q+4*q^(3/2)
            if (diffOrder >= 1) {
                results[1] = -4 + 6 * dis;
                //*/dq=-4+6*q^(1/2)=-4+6*r
            }
        } else {
            results[0] = 4 / 3.0 - 4 * dis + 4 * disSq - 4 / 3.0 * disSq * dis;
            //4/3.0-4*r+4*r*r-4/3.0*r*r*r=4/3.0*(1-r)^3
            //d=1-r
            if (diffOrder >= 1) {
                results[1] = -2 / dis + 4 - dis * 2.0;
                //=> d*/dq=d(4/3.0-4*q^0.5+4*q-4/3.0*q^(3/2))/dq
                // = -2*q^(-0.5)+4-2*q^(0.5)
                // = -2/r+4-2*r
            }
        }
        return results;
    }

    public static SomeFactory<WeightFunctionCore> genFactory() {
        return new SomeFactory<WeightFunctionCore>() {
            @Override
            public WeightFunctionCore produce() {
                return new TriSpline();
            }
        };
    }
}
