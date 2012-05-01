/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilon
 */
public class DistanceFunctions {

    public static class Common implements DistanceFunction{
        private Coordinate center;
        private int order;
        private int baseLen;

        @Override
        public TDoubleArrayList values(Coordinate pt, TDoubleArrayList results) {
            results.reset();
            results.ensureCapacity(baseLen);
            results.add(GeometryMath.distance(center, pt));
            if(order>=1){
                double dist=results.getQuick(0);
                results.add((center.x-pt.x)/dist);
                results.add((center.y-pt.y)/dist);
                results.add((center.z-pt.z)/dist);
            }
            return results;
        }

        @Override
        public void setCenter(Coordinate center) {
            this.center=center;
        }

        @Override
        public void setOrder(int order) {
            if(order<0||order>=2){
                throw new IllegalArgumentException();
            }
            this.order=order;
            baseLen=CommonUtils.len3DBase(order);
        }

        @Override
        public int getOrder() {
            return order;
        }

    }
    
        public static class Common2D implements DistanceFunction{
        private Coordinate center;
        private int order;
        private int baseLen;

        @Override
        public TDoubleArrayList values(Coordinate node, TDoubleArrayList results) {
            results.reset();
            results.ensureCapacity(baseLen);
            results.add(GeometryMath.distance(center, node));
            if(order>=1){
                double dist=results.getQuick(0);
                results.add((center.x-node.x)/dist);
                results.add((center.y-node.y)/dist);
            }
            return results;
        }

        @Override
        public void setCenter(Coordinate center) {
            this.center=center;
        }

        @Override
        public void setOrder(int order) {
            if(order<0||order>=2){
                throw new IllegalArgumentException();
            }
            this.order=order;
            baseLen=CommonUtils.len2DBase(order);
        }

        @Override
        public int getOrder() {
            return order;
        }
        
    }
}
