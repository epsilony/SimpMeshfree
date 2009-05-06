/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.math.util.TriangleQuadrature;
import net.epsilony.simpmeshfree.model.geometry.ApproximatePoint;
import net.epsilony.simpmeshfree.model.geometry.BoundaryNode;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.geometry.TriangleJni;
import net.epsilony.simpmeshfree.shapefun.ShapeFunction;
import net.epsilony.util.collection.LayeredDomainTree;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public class MechanicsModel {

    GeometryModel gm;
    SupportDomain supportDomain = null;
    ShapeFunction shapeFunction;
    RadialBasisFunction radialBasisFunction;
    LinkedList<Node> nodes = new LinkedList<Node>();
    LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();
    LinkedList<double[]> triangleQuadratureDomains = new LinkedList<double[]>();
    LayeredDomainTree<Node> nodesDomainTree = null;

    public MechanicsModel(GeometryModel gm) {
        this.gm = gm;
    }

    public void generateNodesByTriangle(double size, double flatness, String s, boolean needNeighbors, boolean resetNodesIndex) {
        if (resetNodesIndex) {
            Node n = Node.tempNode(0, 0);
            n.getIndexManager().reset();
        }
        nodes.clear();
        boundaryNodes.clear();

        TriangleJni triangleJni = new TriangleJni();
        gm.compile(size, flatness);
        triangleJni.complie(gm, s);
        nodes.addAll(triangleJni.getNodes(needNeighbors));
        for (Node n : nodes) {
            if (n.type() == ModelElementType.BoundaryNode) {
                boundaryNodes.add((BoundaryNode) n);
            }
        }
        nodesDomainTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, true);
    }

    public void generateQuadratureDomainsByTriangle(double size, double flatness, String s) {
        TriangleJni triangleJni = new TriangleJni();
        gm.compile(size, flatness);
        triangleJni.complie(gm, s);
        triangleQuadratureDomains = triangleJni.getTriangleXYsList();
    }

    public void quadrateTriangleDomains(int n) {
        double[] xys = new double[n * n * 2];
        double[] weights = new double[n * n];
        for (double[] triangleDomain : triangleQuadratureDomains) {
            TriangleQuadrature.triangleQuadratePointWeight(xys, weights, n, triangleDomain[0], triangleDomain[1], triangleDomain[2], triangleDomain[3], triangleDomain[4], triangleDomain[5]);
        }
        int i, j;
        double x, y, w;
        double nodesAverDistance;
        LinkedList<Node> supportNodes=new LinkedList<Node>();
        Vector[] partialValues;
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x = xys[(i * n + j) * 2];
                y = xys[(i * n + j) * 2 + 1];
                w = xys[i * n + j];
                nodesAverDistance=supportDomain.supportNode(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues=shapeFunction.shapePartialValues(supportNodes, x, y);
            }
        }
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }
//

    public class RoundSupportDomain implements SupportDomain {

        double r;
        Node nodeFrom=Node.tempNode(0, 0);
        Node nodeTo=Node.tempNode(0, 0);

        @Override
        public double supportNode(double x, double y, List<Node> list) {
            LinkedList<Node> nodes = new LinkedList<Node>();
            nodeFrom.setXY(x-r,y-r);
            nodeTo.setXY(x+r, y+r);
            nodesDomainTree.domainSearch(nodes,nodeFrom,nodeTo);
            double nodesAverageDistance=r/(Math.sqrt(nodes.size())-1);
            LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();

            gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);
            for (Node node : list) {
                if (!node.isInDistance(x, y, r)) {
                    continue;
                }
                if (GeometryModel.canSeeEach(node.getX(),node.getY(),x,y, aps)) {
                    list.add(node);
                }
            }
            return nodesAverageDistance;
        }
    }
//
//    public class LayerSupportDomain implements SupportDomain{
//        int layer;
//        double searchRadiu;
//        LinkedList<ApproximatePoint> aps=new LinkedList<ApproximatePoint>();
//        ArrayList<Node> nodes=new ArrayList<Node>(100);
//        private ApproximatePoint tempApproximatePoint1;
//        private ApproximatePoint tempApproximatePoint2;
//        {
//            tempApproximatePoint1 = ApproximatePoint.tempApproximatePoint(0, 0);
//            tempApproximatePoint2 = ApproximatePoint.tempApproximatePoint(0, 0);
//        }
//        @Override
//        public List<Node> supportNode(Node n, List<Node> list) {
//            list.clear();
//            n.bfsTraverse(layer, nodes);
//            double minX,maxX,minY,maxY,x,y;
//            minX=maxX=n.getX();
//            minY=maxY=n.getY();
//            for(Node node:nodes){
//                x=node.getX();
//                y=node.getY();
//                if(x>maxX){
//                    maxX=x;
//                }else{
//                    if(x<minX){
//                        minX=x;
//                    }
//                }
//                if(y>maxY){
//                    maxY=y;
//                }else{
//                    if(y<minY){
//                        minY=y;
//                    }
//                }
//            }
//            tempApproximatePoint1.setXY(minX,minY);
//            tempApproximatePoint2.setXY(maxX,maxY);
//            gm.pointDomainSearch(tempApproximatePoint1, tempApproximatePoint2,aps);
//            for(Node node:nodes){
//                if(GeometryModel.canSeeEach(n, node, aps)){
//                    list.add(node);
//                }
//            }
//            return list;
//        }
//
//        @Override
//        public List<Node> supportNode(double x, double y, List<Node> list) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }
}
