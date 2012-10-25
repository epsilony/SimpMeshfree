/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun.wcores;

import net.epsilony.simpmeshfree.sfun.WeightFunctionCore;
import net.epsilony.simpmeshfree.utils.SomeFactory;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

/**
 *
 * @author epsilon
 */
public class Wu extends WeightFunctionCoreImp {

    public static final int C2 = 2, C4 = 4, C6 = 6;
    private int type = C2;
    static final PolynomialFunction c2_by_r = new PolynomialFunction(new double[]{8, 0, -72, 105, 0, -63, 0, 27, 0, -5});
    static final PolynomialFunction c2P_1_by_r = new PolynomialFunction(new double[]{-72, 315 / 2.0, 0, -315 / 2.0, 0, 189 / 2.0, 0, -45 / 2.0});
    static final PolynomialFunction c2P_2_by_r_not_complete = new PolynomialFunction(new double[]{0, -945 / 4.0, 0, 945 / 4.0, 0, -315 / 4.0});

    public static void c2(double squareR, int diffOrder, double[] output) {
        //c2=(1-r)**5*(8+40*r+48*r**2+25*r**3+5*r**4)
        //which is c2 continue;
        if (diffOrder > 2 || diffOrder < 0) {
            throw new UnsupportedOperationException("Too larger or too small differentiation order (" + diffOrder + ") of Wendland c2");
        }

        if (squareR >= 1) {
            for (int i = 0; i <= diffOrder; i++) {
                output[i] = 0;
            }
            return;
        }

        double r = Math.sqrt(squareR);


        output[0] = c2_by_r.value(r);
        if (diffOrder < 1) {
            return;
        }
        output[1] = c2P_1_by_r.value(r);
        if (diffOrder < 2) {
            return;
        }
        output[2] = c2P_2_by_r_not_complete.value(r) + 315 / (4 * r);
    }
    static final PolynomialFunction c4_by_r = new PolynomialFunction(new double[]{6, 0, -44, 0, 198, -231, 0, 99, 0, -33, 0, 5});
    static final PolynomialFunction c4P_1_by_r = new PolynomialFunction(new double[]{-44, 0, 396, 1155.0 / 2, 0, 693.0 / 2, 0, -297.0 / 2, 0, 55.0 / 2});
    static final PolynomialFunction c4P_2_by_r = new PolynomialFunction(new double[]{396, -3465.0 / 4, 0, 3465.0 / 4, 0, -2079.0 / 4, 0, 495.0 / 4});

    public static void c4(double squareR, int diffOrder, double[] output) {
        //c4=(1-r)**6*(6+36*r+82*r**2+72*r**3+30*r**4+5*r**5)
        //which is c4 continue;
        if (diffOrder > 2 || diffOrder < 0) {
            throw new UnsupportedOperationException("Too larger or too small differentiation order (" + diffOrder + ") of Wendland c2");
        }

        if (squareR >= 1) {
            for (int i = 0; i <= diffOrder; i++) {
                output[i] = 0;
            }
            return;
        }

        double r = Math.sqrt(squareR);
        for (int i = 0; i < diffOrder+1; i++) {
            switch (i) {
                case 0:
                    output[0] = c4_by_r.value(r);
                    break;
                case 1:
                    output[1] = c4P_1_by_r.value(r);
                    break;
                case 2:
                    output[2] = c4P_2_by_r.value(r);
                    break;
            }

        }
    }
    static final PolynomialFunction c6_by_r = new PolynomialFunction(new double[]{5, 0, -39, 0, 143, 0, -429, 429, 0, -143, 0, 39, 0, -5});
    static final PolynomialFunction c6P_1_by_r = new PolynomialFunction(new double[]{-39, 0, 286, 0, -1287, 3003.0 / 2, 0, -1287.0 / 2, 0, 429.0 / 2, 0, -65.0 / 2});
    static final PolynomialFunction c6P_2_by_r = new PolynomialFunction(new double[]{286, 0, -2574, 15015.0 / 4, 0, -9009.0 / 4, 0, 3861.0 / 4, 0, -715.0 / 4});

    public static void c6(double squareR, int diffOrder, double[] output) {
        //c6=(1-r)**7*(5+35*r+101*r**2+147*r**3+101*r**4+35*r**5+5*r**6)
        //which is c6 continue
        if (diffOrder > 2 || diffOrder < 0) {
            throw new UnsupportedOperationException("Too larger or too small differentiation order (" + diffOrder + ") of Wendland c2");
        }

        if (squareR >= 1) {
            for (int i = 0; i <= diffOrder; i++) {
                output[i] = 0;
            }
            return;
        }

        double r = Math.sqrt(squareR);
        for (int i = 0; i < diffOrder; i++) {
            switch (i) {
                case 0:
                    output[0] = c6_by_r.value(r);
                    break;
                case 1:
                    output[1] = c6P_1_by_r.value(r);
                    break;
                case 2:
                    output[2] = c6P_2_by_r.value(r);
                    break;
            }
        }
    }

    public Wu(int type) {
        switch (type) {
            case C2:
            case C4:
            case C6:
                this.type = type;
                break;
            default:
                throw new UnsupportedOperationException("Wrong type!");
        }
    }

    @Override
    public double[] valuesByNormalisedDistSq(double distSq, double[] results) {
        results = initResultOutput(results);
        switch (type) {
            case C2:
                c2(distSq, diffOrder, results);
                break;
            case C4:
                c4(distSq, diffOrder, results);
                break;
            case C6:
                c6(distSq, diffOrder, results);
        }
        return results;
    }

    SomeFactory<WeightFunctionCore> genFactory(final int type) {
        return new SomeFactory<WeightFunctionCore>() {
            @Override
            public WeightFunctionCore produce() {
                return new Wu(type);
            }
        };
    }
}
