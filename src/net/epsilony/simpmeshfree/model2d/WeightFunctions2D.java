/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.model.DistanceFunction;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.WeightFunction;
import static net.epsilony.simpmeshfree.utils.CommonUtils.len2DBase;
import net.epsilony.simpmeshfree.utils.PartDiffUnivariateFunction;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeightFunctions2D {

    public static WeightFunction factory(PartDiffUnivariateFunction coreFun, DistanceFunction distFun) {
        WeightFunctionImp imp=new WeightFunctionImp(coreFun,distFun);
        return imp;
    }

    static class WeightFunctionImp implements WeightFunction {

        private int order;
        private int baseLen;
        PartDiffUnivariateFunction coreFun;
        DistanceFunction distFun;

        private WeightFunctionImp(PartDiffUnivariateFunction coreFun, DistanceFunction distFun) {
            this.coreFun=coreFun;
            this.distFun=distFun;
        }

        @Override
        public ArrayList<TDoubleArrayList> values(List<Node> nodes, double supportRad, ArrayList<TDoubleArrayList> results) {
            initResults(results, nodes);
            int index=0;
            TDoubleArrayList coreVals=new TDoubleArrayList(order);
            TDoubleArrayList dists=new TDoubleArrayList(order);
            for (Node node :nodes){
                TDoubleArrayList res=results.get(index++);
                distFun.values(node,dists);
                coreFun.values(dists.get(0)/supportRad, coreVals);
                res.add(coreVals.get(0));
                
                if(order>=1){
                    res.add(coreVals.get(1)*dists.get(1)/supportRad);
                    res.add(coreVals.get(1)*dists.get(2)/supportRad);
                }
            }
            return results;
        }

        private void initResults(ArrayList<TDoubleArrayList> results, List<Node> nodes) {
            for (int i = 0; i < min(nodes.size(), results.size()); i++) {
                TDoubleArrayList res = results.get(i);
                res.resetQuick();
                res.ensureCapacity(baseLen);
            }

            for (int i = 0; i < nodes.size() - results.size(); i++) {
                results.add(new TDoubleArrayList(baseLen));
            }
        }

        @Override
        public void setOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.order = order;
            baseLen = len2DBase(order);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    public static class TriSpline implements PartDiffUnivariateFunction {

        private int order;

        @Override
        public TDoubleArrayList values(double dis, TDoubleArrayList results) {
            results.resetQuick();
            

            if (dis > 1) {
                for (int i = 0; i < order + 1; i++) {
                    results.add(0);
                }
                return results;
            }
            if (dis <= 0.5) {
                results.add(2 / 3.0 + 4 * dis * dis * (-1 + dis));
                //2/3.0-4*r*r+4*r*r*r;
            } else {
                double d = 1 - dis;
                results.add(4 / 3.0 * d * d * d);
                //4/3.0-4*r+4*r*r-4/3.0*r*r*r;
            }

            if (order >= 1) {
                if (dis <= 0.5) {
                    results.add(4 * dis * (-2 + 3 * dis));
                    //return -8*r+12*r*r;
                } else {
                    double d = 1 - dis;
                    results.add(-4 * d * d);
                }
            }
            return results;
        }

        @Override
        public void setOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    public static class SimpPower implements PartDiffUnivariateFunction {

        private int power;
        private int order;

        public SimpPower(int power) {
            this.power = power;
        }

        @Override
        public TDoubleArrayList values(double r, TDoubleArrayList results) {
            results.resetQuick();

            if (r > 1) {
                for (int i = 0; i <= order; i++) {
                    results.add(0);
                }
                return results;
            }

            double t = r * r - 1;
            results.add(pow(t, power));

            if (order >= 1) {
                results.add(pow(t, power - 1) * power * 2 * r);
            }
            return results;
        }

        @Override
        public void setOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
