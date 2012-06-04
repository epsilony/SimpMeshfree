/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import static java.lang.Math.pow;
import java.util.Arrays;

/**
 *
 * @author epsilon
 */
public class WeightFunctionCores {
    public static class TriSpline implements WeightFunctionCore {

        private int diffOrder;

        @Override
        public double[] valuesByNormalisedDistSq(double disSq, double[] results) {
            if (null == results) {
                results = new double[diffOrder+1];
            }

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
                results[0] = 4/3.0-4*dis+4*disSq-4/3.0*disSq*dis;
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

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = order;

        }

        @Override
        public int getDiffOrder() {
            return diffOrder;


        }

        @Override
        public WeightFunctionCore avatorInstance() {
            return new TriSpline();
        }
    }

    public static class SimpPower implements WeightFunctionCore {

        private int power;
        private int diffOrder;

        public SimpPower(int power) {
            this.power = power;
        }

        @Override
        public double[] valuesByNormalisedDistSq(double disSq, double[] results) {

            if(null==results){
                results=new double[diffOrder];
            }
            if (disSq >= 1) {
                Arrays.fill(results, 0);
                return results;
            }

            double t = disSq- 1;
            results[0]=pow(t, power);
            //(r*r-1)^p = (q-1)^p

            if (diffOrder >= 1) {
                results[1]=pow(t, power - 1) * power ;
            }
            return results;
        }

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = order;
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        @Override
        public WeightFunctionCore avatorInstance() {
           return new SimpPower(power);
        }
    }
}
