/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import static java.lang.Math.pow;
import java.util.Arrays;
import java.util.List;
import static net.epsilony.simpmeshfree.utils.CommonUtils.lenBase;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeightFunctions {

    public static WeightFunction factory(WeightFunctionCore coreFun, DistanceSquareFunction distFun) {
        WeightFunctionImp imp = new WeightFunctionImp(coreFun, distFun);
        return imp;
    }

    public static WeightFunction factory(WeightFunctionCore coreFun, DistanceSquareFunction distFun, int dim) {
        WeightFunctionImp imp = new WeightFunctionImp(coreFun, distFun, dim);
        return imp;
    }

    static class WeightFunctionImp implements WeightFunction {

        private int diffOrder;
        private int diffDim;
        WeightFunctionCore coreFun;
        DistanceSquareFunction distFun;
        private final int dim;
        private double[] coreVals;

        private WeightFunctionImp(WeightFunctionCore coreFun, DistanceSquareFunction distFun, int dim) {
            this.coreFun = coreFun;
            this.distFun = distFun;
            this.dim = dim;
        }

        private WeightFunctionImp(WeightFunctionCore coreFun, DistanceSquareFunction distFun) {
            this.coreFun = coreFun;
            this.distFun = distFun;
            this.dim = 2;
        }

        @Override
        public TDoubleArrayList[] values(List<Node> nodes, double supportRad, TDoubleArrayList[] results) {

            results = distFun.sqValues(nodes, results);
            double radSq = supportRad * supportRad;
            for (int i=0;i<nodes.size();i++) {
                double distSq = results[0].get(i);
                coreFun.valuesByNormalisedDistSq(distSq / radSq, coreVals);
                results[0].set(i, coreVals[0]);

                if (diffOrder >= 1) {
                    double distSq_x=results[1].get(i);
                    double distSq_y=results[2].get(i);
                    results[1].set(i,coreVals[1] * distSq_x / radSq);
                    results[2].set(i,coreVals[1] * distSq_y / radSq);
                    if(dim==3){
                        double distSq_z=results[3].get(i);
                        results[3].set(i,coreVals[1]*distSq_z/radSq);
                    }
                }
            }
            return results;
        }

//        private TDoubleArrayList[] initResultsTale(TDoubleArrayList[] results, int NdsNum) {
//            if (results.length < diffDim) {
//                results = Arrays.copyOf(results, diffDim);
//                for (int i = diffOrder; i < diffDim; i++) {
//                    results[i] = new TDoubleArrayList(NdsNum);
//                    results[i].fill(0, NdsNum, 0);
//                }
//            } else {
//                for (int i = diffOrder; i < diffDim; i++) {
//                    TDoubleArrayList result = results[i];
//                    result.resetQuick();
//                    result.fill(0, NdsNum, 0);
//                }
//            }
//            return results;
//        }

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = order;
            distFun.setDiffOrder(order);
            coreFun.setDiffOrder(order);
            coreVals = new double[diffOrder+1];
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        @Override
        public DistanceSquareFunction getDistFun() {
            return distFun;
        }
    }
}
