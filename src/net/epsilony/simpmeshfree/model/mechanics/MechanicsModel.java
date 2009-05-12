/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.epsilony.math.analysis.GaussLegendreQuadrature;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.math.util.TriangleQuadrature;

import net.epsilony.simpmeshfree.model.geometry.ApproximatePoint;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition.BoundaryConditionType;
import net.epsilony.simpmeshfree.model.geometry.BoundaryNode;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.GeometryUtils;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.geometry.Route;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.geometry.TriangleJni;
import net.epsilony.simpmeshfree.shapefun.ShapeFunction;
import net.epsilony.util.collection.LayeredDomainTree;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.math.ArgumentOutsideDomainException;

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
    FlexCompRowMatrix compRowMatrix = null;
    Matrix constitutiveLaw = null;
    Vector bVector;

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

    public void quadrateTriangleDomains(int n) throws ArgumentOutsideDomainException {
        double[] xys = new double[n * n * 2];
        double[] weights = new double[n * n];
        for (double[] triangleDomain : triangleQuadratureDomains) {
            TriangleQuadrature.triangleQuadratePointWeight(xys, weights, n, triangleDomain[0], triangleDomain[1], triangleDomain[2], triangleDomain[3], triangleDomain[4], triangleDomain[5]);
        }
        int i, j, k, l;
        double x, y, w;
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        compRowMatrix = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);
        DenseMatrix bk = new DenseMatrix(3, 2);
        DenseMatrix bl = new DenseMatrix(3, 2);
        DenseMatrix kkl = new DenseMatrix(2, 2);
        DenseMatrix tempMat = new DenseMatrix(2, 3);
        i = 0;
        for (Node node : nodes) {
            node.setMatrixIndex(i);
            i++;
        }
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                x = xys[(i * n + j) * 2];
                y = xys[(i * n + j) * 2 + 1];
                w = xys[i * n + j];
                nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                for (k = 0; k < supportNodes.size(); k++) {
                    for (l = k; l < supportNodes.size(); l++) {
                        bk.set(0, 0, partialValues[0].get(k));
                        bk.set(1, 1, partialValues[1].get(k));
                        bk.set(2, 0, partialValues[1].get(k));
                        bk.set(2, 1, partialValues[0].get(k));
                        bl.set(0, 0, partialValues[0].get(l));
                        bl.set(1, 1, partialValues[1].get(l));
                        bl.set(2, 0, partialValues[1].get(l));
                        bl.set(2, 1, partialValues[0].get(l));
                        bk.transAmult(constitutiveLaw, tempMat);
                        tempMat.mult(bl, kkl);
                        final int kIndex = supportNodes.get(k).getMatrixIndex() * 2;
                        final int lIndex = supportNodes.get(l).getMatrixIndex() * 2;
                        compRowMatrix.add(kIndex, lIndex, kkl.get(0, 0) * w);
                        compRowMatrix.add(kIndex, lIndex + 1, kkl.get(0, 1) * w);
                        compRowMatrix.add(kIndex + 1, lIndex + 1, kkl.get(1, 1) * w);
                        compRowMatrix.add(kIndex + 1, lIndex, kkl.get(1, 0) * w);
                    }
                }
            }
        }
    }

    public void natureBoundaryQuadrate(int n) throws ArgumentOutsideDomainException {
        LinkedList<Route> routes = gm.getRoutes();
        LinkedList<ApproximatePoint> aprxPts;
        ArrayList<Node> supportNodes = new ArrayList<Node>();
        ArrayList<BoundaryCondition> naturalBCs = new ArrayList<BoundaryCondition>(10);
        LinkedList<BoundaryCondition> tempBCs = null;
        LinkedList<BoundaryCondition> conNaturalBCs = new LinkedList<BoundaryCondition>();
        LinkedList<double[]> conNaturalValues = new LinkedList<double[]>();
        ApproximatePoint aprxStart, aprx, aprxFront;
        Segment segment = null;
        Vector shapeVector;
        double t, w, t1, t2, x, y, traX, traY, ds;
        double parmStart, parmEnd;
        BoundaryCondition tempBC;
        double[] txy = new double[2];
        double[] quadratePoints = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(n);
        double[] quadrateCoefs = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(n);
        int i = 0, j, k, index;
        bVector = new DenseVector(nodes.size() * 2);
        for (Node node : nodes) {
            node.setMatrixIndex(i);
            i++;
        }
        for (Route route : routes) {
            aprxPts = route.GetApproximatePoints();
            aprxStart = aprxPts.getFirst();
            aprx = aprxStart;

            do {
                if (aprx.getSegmentParm() == 0) {
                    segment = aprx.getSegment();
                    tempBCs = segment.getBoundaryConditions();
                    naturalBCs.clear();
                    conNaturalBCs.clear();
                    if (null != tempBCs) {
                        for (BoundaryCondition bc : tempBCs) {
                            if (bc.getType() == BoundaryConditionType.Natural) {
                                naturalBCs.add(bc);
                            } else if (bc.getType() == BoundaryConditionType.ConNatural) {
                                conNaturalBCs.add(bc);
                            }
                        }
                    }
                    for (BoundaryCondition bc : conNaturalBCs) {
                        bc.getConNaturalValues(conNaturalValues);
                        for (double[] values : conNaturalValues) {
                            t = values[0];
                            traX = values[1];
                            traY = values[2];
                            supportDomain.boundarySupportNodes(segment, t, supportNodes);
                            segment.parameterPoint(t, txy);
                            x = txy[0];
                            y = txy[1];
                            shapeVector = shapeFunction.shapeValues(supportNodes, x, y);
                            for (k = 0; k < shapeVector.size(); k++) {
                                index = supportNodes.get(k).getMatrixIndex() * 2;
                                bVector.add(index, shapeVector.get(k) * traX);
                                bVector.add(index + 1, shapeVector.get(k) * traY);
                            }
                        }
                    }
                }
                if (naturalBCs.size() > 0) {
                    parmStart = aprx.getSegmentParm();
                    aprxFront = aprx.getFront();
                    if (aprxFront.getSegmentParm() == 0) {
                        parmEnd = 1;
                    } else {
                        parmEnd = aprxFront.getSegmentParm();
                    }

                    for (i = 0; i < n; i++) {
                        t1 = (parmStart - parmEnd) * 0.5;
                        t2 = (parmStart + parmEnd) * 0.5;
                        t = t2 + t1 * quadratePoints[i];
                        w = quadrateCoefs[i] * t1;
                        supportDomain.boundarySupportNodes(segment, t, supportNodes);
                        segment.parameterPoint(t, txy);
                        x = txy[0];
                        y = txy[1];
                        segment.parameterDifference(t, txy);
                        ds = Math.sqrt(txy[0] * txy[0] + txy[1] * txy[1]);

                        shapeVector = shapeFunction.shapeValues(supportNodes, x, y);
                        for (j = 0; j < naturalBCs.size(); j++) {
                            tempBC = naturalBCs.get(j);
                            if (0x00 != tempBC.getValues(t, txy)) {
                                traX = txy[0];
                                traY = txy[1];
                                for (k = 0; i < shapeVector.size(); k++) {
                                    index = supportNodes.get(k).getMatrixIndex() * 2;
                                    bVector.add(index, shapeVector.get(k) * traX * w * ds);
                                    bVector.add(index + 1, shapeVector.get(k) * traY * w * ds);
                                }
                            }
                        }
                    }
                }

                aprx = aprx.getFront();
            } while (aprx != aprxStart);
        }
    }

    public void setEssentialBoundaryConditions() {
        Segment segment;
        LinkedList<BoundaryCondition> tempBCs;
        int col, rowcol, size = nodes.size();
        nodes.size();
        double[] txy = new double[2];
        double ux, uy;
        int i;
        byte tb;
        for (BoundaryNode bNode : boundaryNodes) {
            segment = bNode.getSegment();
            tempBCs = segment.getBoundaryConditions();
            if (null == tempBCs) {
                continue;
            }
            for (BoundaryCondition bc : tempBCs) {
                if (bc.getType() != BoundaryConditionType.Essential) {
                    continue;
                }
                tb = bc.getValues(bNode.getSegmentParm(), txy);
                if (0x00 == tb) {
                    continue;
                }
                rowcol = bNode.getMatrixIndex() * 2;
                if ((BoundaryCondition.X & tb) != 0) {
                    ux = txy[0];
                    for (i = 0; i <size; i++) {
                        bVector.add(i*2, -compRowMatrix.get(i*2, rowcol) * ux);
                        compRowMatrix.set(i*2,rowcol,0);
                        compRowMatrix.set(rowcol,i*2,0);
                    }
                    compRowMatrix.set(rowcol, rowcol, ux);
                    bVector.set(rowcol, ux);
                }

                if ((BoundaryCondition.Y & tb) != 0) {
                    uy = txy[0];
                    for (i = 0; i <size; i++) {
                        bVector.add(i, -compRowMatrix.get(i*2+1, rowcol+1) * uy);
                        compRowMatrix.set(i*2+1,rowcol+1,0);
                        compRowMatrix.set(rowcol+1,i*2+1,0);
                    }
                    
                    compRowMatrix.set(rowcol+1, rowcol+1, uy);
                    bVector.set(rowcol+1, uy);
                }
            }
        }
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }
//

    public class SimpleRoundSupportDomain implements SupportDomain {

        double r;
        Node nodeFrom = Node.tempNode(0, 0);
        Node nodeTo = Node.tempNode(0, 0);

        public SimpleRoundSupportDomain(double r) {
            this.r = r;
        }

        @Override
        public double supportNodes(double x, double y, List<Node> output) {
            output.clear();
            LinkedList<Node> nodes = new LinkedList<Node>();
            nodeFrom.setXY(x - r, y - r);
            nodeTo.setXY(x + r, y + r);
            nodesDomainTree.domainSearch(nodes, nodeFrom, nodeTo);
            double nodesAverageDistance = r / (Math.sqrt(nodes.size()) - 1);
            LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
            gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);

            GeometryUtils.insightNodes(x, y, r, null, 0, aps, nodes);
            output.addAll(nodes);
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
            nodeFrom.setXY(x - r, y - r);
            nodeTo.setXY(x + r, y + r);
            nodesDomainTree.domainSearch(nodes, nodeFrom, nodeTo);
            double nodesAverageDistance = r / (Math.sqrt(nodes.size()) - 1);
            LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();

            gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);
            GeometryUtils.insightNodes(x, y, r, bSegment, parm, aps, nodes);
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
//        public List<Node> supportNodes(Node n, List<Node> list) {
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
//        public List<Node> supportNodes(double x, double y, List<Node> list) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }

    public static void main(String[] args) {
        FlexCompRowMatrix matrix = new FlexCompRowMatrix(10, 10);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrix.set(i, j, 0);
            }
        }
        SparseVector v = matrix.getRow(0);
        int t = v.getUsed();
        System.out.println("t = " + t);
        v.zero();
        System.out.println("v.getUsed() = " + v.getUsed());
        matrix.compact();
        v = matrix.getRow(0);
        System.out.println("v.getUsed() = " + v.getUsed());
    }
}
