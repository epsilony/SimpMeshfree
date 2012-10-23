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
public class Wendland extends WeightFunctionCoreImp{

    static final PolynomialFunction c2_by_r = new PolynomialFunction(new double[]{1, 0, -10, 20, -15, 4});
    static final PolynomialFunction c2P_1_by_r = new PolynomialFunction(new double[]{-10, 30, -30, 10});

    public static void c2(double squareR, int diffOrder, double[] output) {
        //c2=(1-r)**4*(4*r+1)
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
        output[2] = 15 * r - 30 + 15 / r;
    }
    static final PolynomialFunction c4_by_r = new PolynomialFunction(new double[]{3, 0, -28, 0, 210, -448, 420, -192, 35});
    static final PolynomialFunction c4P_1_by_r = new PolynomialFunction(new double[]{-28, 0, 420, -1120, 1260, -672, 140});
    static final PolynomialFunction c4P_2_by_r = new PolynomialFunction(new double[]{420, -1680, 2520, -1680, 420});

    public static void c4(double squareR, int diffOrder, double[] output) {
        //c4=(1-r)**6*(35*r**2+18*r+3)
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
        for (int i = 0; i < diffOrder; i++) {
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
    
    static final PolynomialFunction c6_by_r=new PolynomialFunction(new double[]{1,0,-11,0,66,0,-462,1056,-1155,704,-231,32});
    static final PolynomialFunction c6P_1_by_r=new PolynomialFunction(new double[]{-11,0,132,0,-1386,3696,-4620,3168,-1155,176});
    static final PolynomialFunction c6P_2_by_r=new PolynomialFunction(new double[]{132,0,-2772,9240,-13860,11088,-4620,792});
    
    public static void c6(double squareR, int diffOrder, double[] output) {
        //c6=(1-r)**8*(32*r**3+25*r**2+8*r+1)
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

    public static final int C2=2,C4=4,C6=6;
    
    private int type=C2;

    public Wendland(int type) {
        switch(type){
            case C2:
            case C4:
            case C6:
                this.type=type;
            default:
                throw new UnsupportedOperationException("Wrong type!");
        }
    }
    
    @Override
    public double[] valuesByNormalisedDistSq(double distSq, double[] results) {
        results=initResultOutput(results);
        switch(type){
            case C2:
                c2(distSq, diffOrder, results);
                break;
            case C4:
                c4(distSq,diffOrder,results);
                break;
            case C6:
                c6(distSq,diffOrder,results);
        }
        return results;
    }
    
    SomeFactory<WeightFunctionCore> genFactory(final int type){
        return new SomeFactory<WeightFunctionCore>() {

            @Override
            public WeightFunctionCore produce() {
                return new Wendland(type);
            }
        };
    }
}
