/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilon
 */
public class DistanceSquareFunctionCores {
    public static double[] initResults(int dim,int diffOrder){
        return new double[CommonUtils.lenBase(dim, diffOrder)];
    }
    
    public static DistanceSquareFunctionCore common(int dim){
        return new Common(dim);
    }
    
    public static class Common implements DistanceSquareFunctionCore{
        private int diffOrder;
        private int dim;

        public Common(int dim) {
            this.dim = dim;
        }

        @Override
        public double[] value(Coordinate pt, Coordinate center, double[] results) {
            if(dim!=2&&dim!=3){
                throw new UnsupportedOperationException();
            }
            if(null==results){
                results=initResults(dim, diffOrder);
            }
            results[0]=GeometryMath.distanceSquare(center, pt);
            if(diffOrder>0){
                results[1]=2*(pt.x-center.x);
                results[2]=2*(pt.y-center.y);
                if(dim==3){
                    results[3]=2*(pt.z-center.z);
                }
            }
            return results;
        }

        @Override
        public void setDiffOrder(int order) {
            this.diffOrder=order;
            if(diffOrder>1){
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }
        
    }
}
