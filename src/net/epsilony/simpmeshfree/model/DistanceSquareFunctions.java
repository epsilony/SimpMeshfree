/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

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
    
    public static DistanceSquareFunction common(int dim){
        return new Common(dim);
    }
    
    public static DistanceSquareFunction common(){
        return new Common();
    }

    public static class Common implements DistanceSquareFunction {

        private Coordinate center;
        private int order;
        private int partDim;
        private int dim;

        public Common() {
            dim = 2;
        }

        public Common(int dim) {
            if (dim < 2 || dim > 3) {
                throw new UnsupportedOperationException("dimension must be 2 or 3");
            }
            this.dim = dim;
        }

        @Override
        public TDoubleArrayList[] sqValues(List<? extends Coordinate> pts, TDoubleArrayList[] results) {
            results=init(results, pts.size());
            for (Coordinate pt : pts) {
                double distSq=GeometryMath.distanceSquare(center, pt);
                results[0].add(distSq);
                if (order >= 1) {
                    
  
                    results[1].add(2*(center.x - pt.x));
                    results[2].add(2*(center.y - pt.y));
                    if (dim == 3) {
                        results[3].add(2*(center.z - pt.z));
                    }
                }
            }
            return results;
        }

        public TDoubleArrayList[] init(TDoubleArrayList[] results, int numPts) {
            if (null == results) {
                results = new TDoubleArrayList[partDim];
                for (int i = 0; i < results.length; i++) {
                    results[i] = new TDoubleArrayList(numPts);
                }
            } else {
                for (int i = 0; i < partDim; i++) {
                    results[i].resetQuick();
                    results[i].ensureCapacity(numPts);
                }
            }
            return results;
        }

        @Override
        public void setCenter(Coordinate center) {
            this.center = center;
        }

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new IllegalArgumentException();
            }
            this.order = order;
            partDim = CommonUtils.lenBase(dim, order);
        }

        @Override
        public int getDiffOrder() {
            return order;
        }
    }
}
