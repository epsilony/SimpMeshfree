/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilon
 */
public class DistanceSquareFunctions {

    public static DistanceSquareFunction common(int dim) {
        return new Common(dim);
    }

    public static DistanceSquareFunction common() {
        return new Common();
    }

    public static class Common implements DistanceSquareFunction {

        private Coordinate pos;
        private int order;
        private int baseLen;
        private int dim;
        DistanceSquareFunctionCore coreFun;

        public Common() {
            dim = 2;
            coreFun=DistanceSquareFunctionCores.common(dim);
        }

        public Common(int dim) {
            if (dim < 2 || dim > 3) {
                throw new UnsupportedOperationException("dimension must be 2 or 3");
            }
            this.dim = dim;
            coreFun=DistanceSquareFunctionCores.common(dim);
        }

        @Override
        public TDoubleArrayList[] sqValues(List<? extends Coordinate> centers, TDoubleArrayList[] results) {
            results = init(results, centers.size());
            double[] distSqs=new double[baseLen];
            for (Coordinate center:  centers) {
                coreFun.value(pos, center, distSqs);
                for(int i=0;i<baseLen;i++){
                    results[i].add(distSqs[i]);
                }
            }
            return results;
        }

        public TDoubleArrayList[] init(TDoubleArrayList[] results, int numPts) {
            if (null == results) {
                results = new TDoubleArrayList[baseLen];
                for (int i = 0; i < results.length; i++) {
                    results[i] = new TDoubleArrayList(numPts);
                }
            } else {
                for (int i = 0; i < baseLen; i++) {
                    results[i].resetQuick();
                    results[i].ensureCapacity(numPts);
                }
            }
            return results;
        }

        @Override
        public void setPosition(Coordinate pos) {
            this.pos = pos;
        }

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new IllegalArgumentException();
            }
            this.order = order;
            baseLen = CommonUtils.lenBase(dim, order);
            coreFun.setDiffOrder(order);
        }

        @Override
        public int getDiffOrder() {
            return order;
        }
    }

    public static TDoubleArrayList[] initDistSqsContainer(int dim, int diffOrder, int capacityGuess) {
        int lenBase = CommonUtils.lenBase(dim, diffOrder);
        TDoubleArrayList[] result = new TDoubleArrayList[lenBase];
        for (int i = 0; i < result.length; i++) {
            if (capacityGuess > 0) {
                result[i] = new TDoubleArrayList(capacityGuess);
            } else {
                result[i] = new TDoubleArrayList();
            }
        }
        return result;
    }
    
    public static TDoubleArrayList[] initDistSqsContainer(int dim,int diffOrder){
        return initDistSqsContainer(dim, diffOrder, -1);
    }
}
