/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.processor2D;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.model2D.ApproximatePoint;
import net.epsilony.simpmeshfree.model2D.Model;
import net.epsilony.simpmeshfree.model2D.GeometryUtils;
import net.epsilony.simpmeshfree.model2D.Node;
import net.epsilony.simpmeshfree.model2D.Point;
import net.epsilony.simpmeshfree.model2D.Segment;
import net.epsilony.util.collection.LayeredDomainTree;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class SupportDomains {

    public static class SimpleRoundSupportDomain implements SupportDomain {

        transient static Logger log = Logger.getLogger(SimpleRoundSupportDomain.class);
        double rMin;
        private double rMax;
        private double maxStep;
        private int minNode;
        LayeredDomainTree<Node> nodesDomainTree = null;
        private Model gm;
//        Collection<Node> nodes;

        public SimpleRoundSupportDomain(double rMin, double rMax, double maxStep, int minNode, Model gm, Collection<Node> nodes) {
            this.rMin = rMin;
            this.rMax = rMax;
            this.maxStep = maxStep;
            this.minNode = minNode;
            this.gm = gm;
//            this.nodes=nodes;
            nodesDomainTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, true);
        }

        @Override
        public double supportNodes(double x, double y, List<Node> output) {
            if (null == log) {
                log = Logger.getLogger(SimpleRoundSupportDomain.class);
            }
            Node nodeFrom = Node.tempNode(0, 0);
            Node nodeTo = Node.tempNode(0, 0);
            output.clear();
            LinkedList<Node> tnodes = new LinkedList<Node>();
            double r = rMin;
            double nodesAverageDistance;
            do {
                nodeFrom.setXY(x - r, y - r);
                nodeTo.setXY(x + r, y + r);
                nodesDomainTree.domainSearch(tnodes, nodeFrom, nodeTo);
                nodesAverageDistance = r / (Math.sqrt(tnodes.size()) - 1);
                if (null != gm) {
                    LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                    gm.approximatePointSearch(aps, x - r - gm.getSegmentApproximateSize(), y - r - gm.getSegmentApproximateSize(), x + r + gm.getSegmentApproximateSize(), y + r + gm.getSegmentApproximateSize());
                    GeometryUtils.insightNodes(x, y, r, null, 0, aps, tnodes);
                }
                r += (rMax - rMin) / maxStep;
            } while (tnodes.size() < minNode && r < rMax + (rMax - rMin) / maxStep);
            if (tnodes.size() < minNode) {
                log.warn(String.format("SimpleRoundSupportDomain.supportNodes(%5.2f,%5.2f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", x, y, r - (rMax - rMin) / maxStep, tnodes.size(), minNode));
            }

            output.addAll(tnodes);
            if (null != log && log.isDebugEnabled()) {
                log.debug(String.format("SimpleRoundSupportDomain.supportNodes(%5.2f,%5.2f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", x, y, nodesAverageDistance, r - (rMax - rMin) / maxStep, tnodes.size()));
            }
            return nodesAverageDistance;
        }

        @Override
        public double boundarySupportNodes(Segment bSegment, double parm, List<Node> output) {
            if (null == log) {
                log = Logger.getLogger(SimpleRoundSupportDomain.class);
            }
            Node nodeFrom = Node.tempNode(0, 0);
            Node nodeTo = Node.tempNode(0, 0);
            output.clear();
            LinkedList<Node> tnodes = new LinkedList<Node>();
            double[] pt = new double[2];
            bSegment.parameterPoint(parm, pt);
            double x = pt[0];
            double y = pt[1];
            double nodesAverageDistance;
            double r = rMin;
            do {
                nodeFrom.setXY(x - r, y - r);
                nodeTo.setXY(x + r, y + r);
                nodesDomainTree.domainSearch(tnodes, nodeFrom, nodeTo);
                nodesAverageDistance = r / (Math.sqrt(tnodes.size()) - 1);

                LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                gm.approximatePointSearch(aps, x - r - gm.getSegmentApproximateSize(), y - r - gm.getSegmentApproximateSize(), x + r + gm.getSegmentApproximateSize(), y + r + gm.getSegmentApproximateSize());
                GeometryUtils.insightNodes(x, y, r, bSegment, parm, aps, tnodes);
                r += (rMax - rMin) / maxStep;
            } while (tnodes.size() < minNode && r < rMax + (rMax - rMin) / maxStep);
            output.addAll(tnodes);
            if (tnodes.size() < minNode) {
                log.warn(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", bSegment, parm, r - (rMax - rMin) / maxStep, tnodes.size(), minNode));
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", bSegment, parm, nodesAverageDistance, r - (rMax - rMin) / maxStep, output.size()));
            }

            return nodesAverageDistance;
        }

        @Override
        public SupportDomain CopyOf(boolean deep) {
            if (deep) {
                return new SimpleRoundSupportDomain(rMin, rMax, maxStep, minNode, gm, nodesDomainTree.getPoints());
            } else {
                return this;
            }
        }
    }

    public static class RoundSupportDomainForCSRBF implements SupportDomain {

        static Logger log = Logger.getLogger(RoundSupportDomainForCSRBF.class);
        // The fill distance can be interpreted as the radius of the largest ball which is completely contained in and which does not contain any nodes.
        //in a Delaunay division it is the largerest circumradius of all the triangles
        private double fillDistanceRadiu;
        private Model model;
        private final double alpha;

        public RoundSupportDomainForCSRBF(Collection<Node> nodes,double fillDistanceRadiu, Model model, double alpha) {
            this.fillDistanceRadiu = fillDistanceRadiu;
            this.model = model;
            this.alpha = alpha;
        }

        public RoundSupportDomainForCSRBF(Collection<Node> nodes, Model model, Collection<double[]> triangleDomains, double alpha) {
            this.model = model;
            nodesDomainTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, true);
            double rMax = 0;
            for (double[] ds : triangleDomains) {
                double r = EYMath.cirumradiu(ds);
                if (rMax < r) {
                    rMax = r;
                }
            }
            fillDistanceRadiu = rMax;
            this.alpha = alpha;
        }
        LayeredDomainTree<Node> nodesDomainTree = null;

        @Override
        public double supportNodes(double x, double y, List<Node> output) {
            Node nodeFrom = Node.tempNode(0, 0);
            Node nodeTo = Node.tempNode(0, 0);
            output.clear();
            LinkedList<Node> tnodes = new LinkedList<Node>();
            double r = fillDistanceRadiu * alpha;
            double nodesAverageDistance;

            nodeFrom.setXY(x - r, y - r);
            nodeTo.setXY(x + r, y + r);
            nodesDomainTree.domainSearch(tnodes, nodeFrom, nodeTo);
            nodesAverageDistance = r / (Math.sqrt(tnodes.size()) - 1);
            if (null != model) {
                LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                model.approximatePointSearch(aps, x - r - model.getSegmentApproximateSize(), y - r - model.getSegmentApproximateSize(), x + r + model.getSegmentApproximateSize(), y + r + model.getSegmentApproximateSize());
                GeometryUtils.insightNodes(x, y, r, null, 0, aps, tnodes);
            }

            output.addAll(tnodes);
            if (log.isDebugEnabled()) {
                log.debug(String.format("supportNodes(%5.2f,%5.2f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", x, y, nodesAverageDistance, r, tnodes.size()));
            }
            return nodesAverageDistance;
        }

        @Override
        public double boundarySupportNodes(Segment bSegment, double parm, List<Node> output) {
            Node nodeFrom = Node.tempNode(0, 0);
            Node nodeTo = Node.tempNode(0, 0);
            output.clear();
            LinkedList<Node> tnodes = new LinkedList<Node>();
            double[] pt = new double[2];
            bSegment.parameterPoint(parm, pt);
            double x = pt[0];
            double y = pt[1];
            double nodesAverageDistance;
            double r = alpha * fillDistanceRadiu;

            nodeFrom.setXY(x - r, y - r);
            nodeTo.setXY(x + r, y + r);
            nodesDomainTree.domainSearch(tnodes, nodeFrom, nodeTo);
            nodesAverageDistance = r / (Math.sqrt(tnodes.size()) - 1);

            LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
            model.approximatePointSearch(aps, x - r - model.getSegmentApproximateSize(), y - r - model.getSegmentApproximateSize(), x + r + model.getSegmentApproximateSize(), y + r + model.getSegmentApproximateSize());
            GeometryUtils.insightNodes(x, y, r, bSegment, parm, aps, tnodes);

            output.addAll(tnodes);
            if (log.isDebugEnabled()) {
                log.debug(String.format("boundarySupportNodes(%s,%5.4f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", bSegment, parm, nodesAverageDistance, r, output.size()));
            }

            return nodesAverageDistance;
        }

        @Override
        public SupportDomain CopyOf(boolean deep) {
            if(deep){
                return new RoundSupportDomainForCSRBF(nodesDomainTree.getPoints(), fillDistanceRadiu, model, alpha);
            }
            return this;
        }
    }
}