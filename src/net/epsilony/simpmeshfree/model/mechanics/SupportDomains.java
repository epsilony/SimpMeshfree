/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.geometry.ApproximatePoint;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.GeometryUtils;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.util.collection.LayeredDomainTree;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class SupportDomains {

    public static class SimpleRoundSupportDomain implements SupportDomain {

        Logger log = Logger.getLogger(SimpleRoundSupportDomain.class);
        double rMin;
        Node nodeFrom = Node.tempNode(0, 0);
        Node nodeTo = Node.tempNode(0, 0);
        private double rMax;
        private double maxStep;
        private int minNode;
        LayeredDomainTree<Node> nodesDomainTree = null;
        private GeometryModel gm;

        public SimpleRoundSupportDomain(double rMin, double rMax, double maxStep, int minNode, GeometryModel gm, List<Node> nodes) {
            this.rMin = rMin;
            this.rMax = rMax;
            this.maxStep = maxStep;
            this.minNode = minNode;
            this.gm = gm;
            nodesDomainTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, true);
        }

        @Override
        public double supportNodes(double x, double y, List<Node> output) {
            output.clear();
            LinkedList<Node> nodes = new LinkedList<Node>();
            double r = rMin;
            double nodesAverageDistance;
            do {
                nodeFrom.setXY(x - r, y - r);
                nodeTo.setXY(x + r, y + r);
                nodesDomainTree.domainSearch(nodes, nodeFrom, nodeTo);
                nodesAverageDistance = r / (Math.sqrt(nodes.size()) - 1);
                if (null != gm) {
                    LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                    gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);
                    GeometryUtils.insightNodes(x, y, r, null, 0, aps, nodes);
                }
                r += (rMax - rMin) / maxStep;
            } while (nodes.size() < minNode && r < rMax + (rMax - rMin) / maxStep);
            if (nodes.size() < minNode) {
                log.warn(String.format("SimpleRoundSupportDomain.supportNodes(%5.2f,%5.2f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", x, y, r - (rMax - rMin) / maxStep, nodes.size(), minNode));
            }

            output.addAll(nodes);
            if (log.isDebugEnabled()) {
                log.debug(String.format("SimpleRoundSupportDomain.supportNodes(%5.2f,%5.2f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", x, y, nodesAverageDistance, r - (rMax - rMin) / maxStep, nodes.size()));
            }
            return nodesAverageDistance;
        }

        @Override
        public double boundarySupportNodes(Segment bSegment, double parm, List<Node> output) {
            output.clear();
            LinkedList<Node> nodes = new LinkedList<Node>();
            double[] pt = new double[2];
            bSegment.parameterPoint(parm, pt);
            double x = pt[0];
            double y = pt[1];
            double nodesAverageDistance;
            double r = rMin;
            do {
                nodeFrom.setXY(x - r, y - r);
                nodeTo.setXY(x + r, y + r);
                nodesDomainTree.domainSearch(nodes, nodeFrom, nodeTo);
                nodesAverageDistance = r / (Math.sqrt(nodes.size()) - 1);

                LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);
                GeometryUtils.insightNodes(x, y, r, bSegment, parm, aps, nodes);
                r += (rMax - rMin) / maxStep;
            } while (nodes.size() < minNode && r < rMax + (rMax - rMin) / maxStep);
            output.addAll(nodes);
            if (nodes.size() < minNode) {
                log.warn(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", bSegment, parm, r - (rMax - rMin) / maxStep, nodes.size(), minNode));
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", bSegment, parm, nodesAverageDistance, r - (rMax - rMin) / maxStep, output.size()));
            }

            return nodesAverageDistance;
        }
    }
}
