/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun.wcores;

import java.util.Arrays;
import net.epsilony.simpmeshfree.sfun.WeightFunctionCore;
import net.epsilony.simpmeshfree.utils.SomeFactory;

/**
 *
 * @author epsilon
 */
public class SimpPower extends WeightFunctionCoreImp {
    int power;

    public SimpPower(int power) {
        this.power = power;
    }

    @Override
    public double[] valuesByNormalisedDistSq(double disSq, double[] results) {
        results = initResultOutput(results);
        if (disSq >= 1) {
            Arrays.fill(results, 0);
            return results;
        }
        double t = disSq - 1;
        results[0] = Math.pow(t, power);
        //(r*r-1)^p = (q-1)^p
        if (diffOrder >= 1) {
            results[1] = Math.pow(t, power - 1) * power;
        }
        return results;
    }


    public static SomeFactory<WeightFunctionCore> genFactory(final int power) {
        return new SomeFactory<WeightFunctionCore>() {
            @Override
            public WeightFunctionCore produce() {
                return new SimpPower(power);
            }
        };
    }
}
