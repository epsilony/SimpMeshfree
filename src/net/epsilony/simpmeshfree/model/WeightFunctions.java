/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import net.epsilony.simpmeshfree.utils.SomeFactory;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeightFunctions {

    public static SomeFactory<WeightFunction> factory(final SomeFactory<WeightFunctionCore> coreFunFactory) {
        return factory(coreFunFactory,2);
        
    }

    public static SomeFactory<WeightFunction> factory(final SomeFactory<WeightFunctionCore> coreFunFactory, final int dim) {
        return new SomeFactory<WeightFunction>() {
            @Override
            public WeightFunction produce() {
                WeightFunctionImp imp = new WeightFunctionImp(coreFunFactory.produce(),dim);
                return imp;
            }
        };
    }
    
    public static WeightFunction weightFunction(WeightFunctionCore coreFun, int dim){
        return new WeightFunctionImp(coreFun, dim);
    }
    
    public static WeightFunction weightFunction(WeightFunctionCore coreFun){
        return new WeightFunctionImp(coreFun);
    }

    static class WeightFunctionImp implements WeightFunction {

        private int diffOrder;
        WeightFunctionCore coreFun;
        private final int dim;
        private double[] coreVals;

        private WeightFunctionImp(WeightFunctionCore coreFun, int dim) {
            this.coreFun = coreFun;
            this.dim = dim;
        }

        private WeightFunctionImp(WeightFunctionCore coreFun) {
            this.coreFun = coreFun;
            this.dim = 2;
        }

        @Override
        public TDoubleArrayList[] values(TDoubleArrayList[] distsSqs, TDoubleArrayList rads, TDoubleArrayList[] results) {
            int size=distsSqs[0].size();
            for (int i = 0; i < size; i++) {
                double radSq = rads.getQuick(i);
                radSq *= radSq;
                double distSq = distsSqs[0].get(i);
                coreFun.valuesByNormalisedDistSq(distSq / radSq, coreVals);
                results[0].set(i, coreVals[0]);

                if (diffOrder >= 1) {
                    double distSq_x = distsSqs[1].get(i);
                    double distSq_y = distsSqs[2].get(i);
                    results[1].set(i, coreVals[1] * distSq_x / radSq);
                    results[2].set(i, coreVals[1] * distSq_y / radSq);
                    if (dim == 3) {
                        double distSq_z = distsSqs[3].get(i);
                        results[3].set(i, coreVals[1] * distSq_z / radSq);
                    }
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
            coreFun.setDiffOrder(order);
            coreVals = new double[diffOrder + 1];
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }
    }
}
