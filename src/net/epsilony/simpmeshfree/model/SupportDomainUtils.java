/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilon
 */
public class SupportDomainUtils {

    public static class SimpConstantSizer implements SupportDomainSizer {

        double rad;
        List<Node> nodes;

        public SimpConstantSizer(double rad, List<Node> nodes) {
            this.rad = rad;
            this.nodes = nodes;
        }

        @Override
        public double domain(Coordinate center, List<Node> outputs) {

            if (outputs != null) {
                double radSq = rad * rad;
                outputs.clear();

                for (Node nd : nodes) {
                    double disSq = GeometryMath.distanceSquare(center, nd);
                    if (disSq <= radSq) {
                        outputs.add(nd);
                    }
                }
            }
            return rad;
        }
    }

    public static class SimpCriterion implements SupportDomainCritierion {

        DistanceSquareFunctions.Common distanceFun;
        int dim;
        private final SupportDomainSizer domainSizer;

        @Override
        public double getSupports(Coordinate center, Boundary centerBound, List<Node> outputNodes, TDoubleArrayList[] distSqs) {
            
            double rad=domainSizer.domain(center, outputNodes);
            distanceFun.setCenter(center);
            distanceFun.sqValues(outputNodes, distSqs);
            return rad;
        }

        public SimpCriterion(int dim, SupportDomainSizer domainSizer) {
            this.dim = dim;
            distanceFun = new DistanceSquareFunctions.Common(dim);
            this.domainSizer = domainSizer;
        }

        public SimpCriterion(SupportDomainSizer domainSizer) {
            this.dim = 2;
            distanceFun = new DistanceSquareFunctions.Common(dim);
            this.domainSizer = domainSizer;
        }

        @Override
        public void setDiffOrder(int order) {
            distanceFun.setDiffOrder(order);
        }

        @Override
        public int getDiffOrder() {
            return distanceFun.getDiffOrder();
        }
    }

    public static SupportDomainSizer simpConstantSizer(double rad, List<Node> nds) {
        return new SimpConstantSizer(rad, nds);
    }

    public static SupportDomainCritierion simpCriterion(int dim, SupportDomainSizer domainSizer) {
        return new SimpCriterion(dim, domainSizer);
    }

    public static SupportDomainCritierion simpCriterion(SupportDomainSizer domainSizer) {
        return simpCriterion(2, domainSizer);
    }

    public static SupportDomainCritierion simpCriterion(int dim, double rad, List<Node> nds) {
        return simpCriterion(dim, simpConstantSizer(rad, nds));
    }

    public static SupportDomainCritierion simpCriterion(double rad, List<Node> nds) {
        return simpCriterion(2, rad, nds);
    }
}
