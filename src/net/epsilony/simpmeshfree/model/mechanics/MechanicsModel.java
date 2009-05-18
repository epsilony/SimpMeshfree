/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.epsilony.math.analysis.GaussLegendreQuadrature;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.math.util.EYMath;
import net.epsilony.math.util.AreaCoordTriangleQuadrature;

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
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.simpmeshfree.utils.ModelPanelManager.ViewMarkerType;
import net.epsilony.util.collection.LayeredDomainTree;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class MechanicsModel implements ModelImagePainter {

    public LinkedList<Node> getNodes() {
        return nodes;
    }
    GeometryModel gm;
    SupportDomain supportDomain = null;
    ShapeFunction shapeFunction;

    public void setShapeFunction(ShapeFunction shapeFunction) {
        this.shapeFunction = shapeFunction;
    }
    RadialBasisFunction radialBasisFunction;

    public void setRadialBasisFunction(RadialBasisFunction radialBasisFunction) {
        this.radialBasisFunction = radialBasisFunction;
    }
    LinkedList<Node> nodes = new LinkedList<Node>();
    LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();
    LinkedList<double[]> triangleQuadratureDomains = new LinkedList<double[]>();
    LayeredDomainTree<Node> nodesDomainTree = null;
    FlexCompRowMatrix compRowMatrix = null;
    Matrix constitutiveLaw = null;

    public Matrix getConstitutiveLaw() {
        return constitutiveLaw;
    }

    public void setConstitutiveLaw(Matrix constitutiveLaw) {
        this.constitutiveLaw = constitutiveLaw;
    }
    Vector bVector;
    static Logger log = Logger.getLogger(MechanicsModel.class);
    static Logger logDeep = Logger.getLogger(MechanicsModel.class.getName() + ".deep1");
    int logi;

    public MechanicsModel(GeometryModel gm) {
        this.gm = gm;
    }
    TriangleJni triJni;

    public void generateNodesByTriangle(double size, double flatness, String s, boolean needNeighbors, boolean resetNodesIndex) {
        log.info(String.format("Start generateNodesByTiangle%nsize=%6.3e flatness=%6.3e s=%s needNeighbors=%b resetNodesIndex %b", size, flatness, s, needNeighbors, resetNodesIndex));
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
        triJni = triangleJni;
        log.info(String.format("End of generateNodesByTriangle%n nodes.size()=%d boundaryNodes.size()=%d", nodes.size(), boundaryNodes.size()));
    }

    public void generateQuadratureDomainsByTriangle() {
        log.info("Start generateQuadratureDomainsByTriangle()");
        triangleQuadratureDomains = triJni.getTriangleXYsList();
        log.info("End of generateQuadratureDomainsByTriangle()");
    }

    public void generateQuadratureDomainsByTriangle(double size, double flatness, String s) {
        log.info(String.format("Start generateQuadratureDomainsByTriangle(%6.3e, %6.3e, %s", size, flatness, s));
        TriangleJni triangleJni = new TriangleJni();
        gm.compile(size, flatness);
        triangleJni.complie(gm, s);
        triangleQuadratureDomains = triangleJni.getTriangleXYsList();
        log.info("End of generateQuadratureDomainsByTriangle()");
    }

    public SupportDomain getSupportDomain() {
        return supportDomain;
    }

    public void quadrateTriangleDomains(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomains(%d)", qn));
        double x, y, w, area;
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        compRowMatrix = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);
        DenseMatrix bk = new DenseMatrix(3, 2);
        DenseMatrix bl = new DenseMatrix(3, 2);
        DenseMatrix kkl = new DenseMatrix(2, 2);
        DenseMatrix tempMat = new DenseMatrix(2, 3);
        double[] weights = AreaCoordTriangleQuadrature.getWeights(qn);
        double[] areaCoords = AreaCoordTriangleQuadrature.getAreaCoordinates(qn);
        int i = 0, k, l;

        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(areaCoords));
        }
        logi = 0;
           int tsum=0;
        for (double[] triangleDomain : triangleQuadratureDomains) {


            if (log.isDebugEnabled()) {
                log.debug(String.format("%d: triangleDomain %s", logi, Arrays.toString(triangleDomain)));
                logi++;
            }
            double x1 = triangleDomain[0];
            double y1 = triangleDomain[1];
            double x2 = triangleDomain[2];
            double y2 = triangleDomain[3];
            double x3 = triangleDomain[4];
            double y3 = triangleDomain[5];
            area = Math.abs((x1 - x2) * (y3 - y2) - (x3 - x2) * (y1 - y2)) / 2;
 
            for (i = 0; i < weights.length; i++) {
                x = x1 * areaCoords[i * 3] + x2 * areaCoords[i * 3 + 1] + x3 * areaCoords[i * 3 + 2];
                y = y1 * areaCoords[i * 3] + y2 * areaCoords[i * 3 + 1] + y3 * areaCoords[i * 3 + 2];
                if (logDeep.isDebugEnabled()) {
                    double vec1 = EYMath.vectorProduct(x - x1, y - y1, x2 - x1, y2 - x1);
                    double vec2 = EYMath.vectorProduct(x - x1, y - y1, x3 - x1, y3 - x1);
                    boolean out = false;
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    vec1 = EYMath.vectorProduct(x - x2, y - y2, x1 - x2, y1 - y2);
                    vec2 = EYMath.vectorProduct(x - x2, y - y2, x3 - x2, y3 - y2);
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    vec1 = EYMath.vectorProduct(x - x3, y - y3, x1 - x3, y1 - y3);
                    vec2 = EYMath.vectorProduct(x - x3, y - y3, x2 - x3, y2 - y3);
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    if (out) {
                        tsum++;
                        logDeep.debug(String.format("%d:point(%.2f,%.2f)is outof %s", tsum,x, y, Arrays.toString(triangleDomain)));
                    }
                }
                w = weights[i];
                nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                for (k = 0; k < supportNodes.size(); k++) {
                    for (l = 0; l < supportNodes.size(); l++) {
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
//                        if(logDeep.isDebugEnabled()){
//                            logDeep.debug("bkbl");
//                            logDeep.debug(bk);
//                            logDeep.debug(bl);
//                            logDeep.debug("kkl:");
//                            logDeep.debug(kkl);
//                            logDeep.debug(constitutiveLaw);
//                        }
                        int kIndex = supportNodes.get(k).getMatrixIndex() * 2;
                        int lIndex = supportNodes.get(l).getMatrixIndex() * 2;
                        compRowMatrix.add(kIndex, lIndex, kkl.get(0, 0) * w * area);
                        compRowMatrix.add(kIndex, lIndex + 1, kkl.get(0, 1) * w * area);
                        compRowMatrix.add(kIndex + 1, lIndex + 1, kkl.get(1, 1) * w * area);
                        compRowMatrix.add(kIndex + 1, lIndex, kkl.get(1, 0) * w * area);
                    }
                }
            }
        }


        log.info("End of quadrateTriangleDomains");
    }

    public void natureBoundaryQuadrate(int n) throws ArgumentOutsideDomainException {
        log.info(String.format("Start natureBoundaryQuadrate(%d", n));
        LinkedList<Route> routes = gm.getRoutes();
        LinkedList<ApproximatePoint> aprxPts;
        ArrayList<Node> supportNodes = new ArrayList<Node>();
        ArrayList<BoundaryCondition> naturalBCs = new ArrayList<BoundaryCondition>(10);
        LinkedList<BoundaryCondition> tempBCs = null;
        LinkedList<BoundaryCondition> conNaturalBCs = new LinkedList<BoundaryCondition>();
        LinkedList<double[]> conNaturalValues = new LinkedList<double[]>();
        ApproximatePoint aprxStart,
                aprx, aprxFront;
        Segment segment = null;
        Vector shapeVector;

        double t, w, t1, t2, x, y, traX, traY, ds;
        double parmStart, parmEnd;
        BoundaryCondition tempBC;

        double[] txy = new double[2];
        double[] quadratePoints = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(n);
        double[] quadrateCoefs = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(n);
        int i = 0, j, k, row;


        for (Route route : routes) {
            aprxPts = route.GetApproximatePoints();
            aprxStart = aprxPts.getFirst();
            aprx = aprxStart;
            if (log.isDebugEnabled()) {
                log.debug(route);
                log.debug("route Approximate Point size=" + aprxPts.size());
            }

            do {
                if (aprx.getSegmentParm() == 0) {
                    segment = aprx.getSegment();
                    if (log.isDebugEnabled()) {
                        log.debug(segment);
                    }

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
                    if (log.isDebugEnabled()) {
                        log.debug("conNaturalBCs.size() =" + conNaturalBCs.size());
                        log.debug("naturalBCs.size()=" + naturalBCs.size());
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
                                row = supportNodes.get(k).getMatrixIndex() * 2;
                                bVector.add(row, shapeVector.get(k) * traX);
                                bVector.add(row + 1, shapeVector.get(k) * traY);
                            }

                        }
                        if (log.isDebugEnabled()) {
                            log.debug("conNaturalBCs applied");
                        }
                    }
                    if (log.isDebugEnabled() && !conNaturalBCs.isEmpty()) {
                        log.debug("conNaturalBCs applied");
                    }

                    if (naturalBCs.size() > 0) {
                        if (logDeep.isDebugEnabled()) {
                            logDeep.debug(segment);
                        }
                    }

                }

                if (naturalBCs.size() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("applying natural bc");
                    }
                    parmStart = aprx.getSegmentParm();
                    aprxFront = aprx.getFront();
                    if (aprxFront.getSegmentParm() == 0) {
                        parmEnd = 1;
                    } else {
                        parmEnd = aprxFront.getSegmentParm();
                    }
                    t1 = -(parmStart - parmEnd) * 0.5;
                    t2 = (parmStart + parmEnd) * 0.5;
                    if (logDeep.isDebugEnabled()) {
                        logDeep.debug(parmStart);
                        logDeep.debug(parmEnd);
                    }
                    for (i = 0; i < n; i++) {
                        t = t2 + t1 * quadratePoints[i];
                        w = quadrateCoefs[i] * t1;

                        segment.parameterPoint(t, txy);
                        x = txy[0];
                        y = txy[1];
                        segment.parameterDifference(t, txy);
                        ds = Math.sqrt(txy[0] * txy[0] + txy[1] * txy[1]);
                        double nodesAverDistance = supportDomain.boundarySupportNodes(segment, t, supportNodes);
                        radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                        shapeVector = shapeFunction.shapeValues(supportNodes, x, y);

                        for (j = 0; j < naturalBCs.size(); j++) {
                            tempBC = naturalBCs.get(j);
                            if (0x00 != tempBC.getValues(t, txy)) {
                                traX = txy[0];
                                traY = txy[1];
                                for (k = 0; k < supportNodes.size(); k++) {
                                    row = supportNodes.get(k).getMatrixIndex() * 2;
                                    bVector.add(row, shapeVector.get(k) * traX * w * ds);
                                    bVector.add(row + 1, shapeVector.get(k) * traY * w * ds);
                                }
                            }
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("end of applying natural bc");
                    }
                }

                aprx = aprx.getFront();
            } while (aprx != aprxStart);
        }

        log.info("End of natureBoundaryQuadrate");
    }

    public LinkedList<BoundaryNode> getBoundaryNodes() {
        return boundaryNodes;
    }

    public void applyEssentialBoundaryConditions() {
        log.info("Start applyEssentialBoundaryConditions");
        Segment segment;
        double segmentParm;

        LinkedList<BoundaryCondition> tempBCs;
        int rowcol;
        nodes.size();
        double[] txy = new double[2];
        double ux, uy;
        int i;
        byte tb;
        for (BoundaryNode bNode : boundaryNodes) {
            segment = bNode.getSegment();
            segmentParm = bNode.getSegmentParm();
            if (0 == segmentParm) {
                LinkedList<BoundaryCondition> bc1, bc2;
                bc1 = segment.getBoundaryConditions();
                bc2 = segment.getBack().getBoundaryConditions();
                if (bc1 != null || bc2 != null) {
                    tempBCs = new LinkedList<BoundaryCondition>();
                    if (bc1 != null) {
                        tempBCs.addAll(segment.getBoundaryConditions());
                    }
                    if (bc2 != null) {
                        tempBCs.addAll(segment.getBack().getBoundaryConditions());
                    }
                } else {
                    tempBCs = null;
                }
            } else {
                tempBCs = segment.getBoundaryConditions();
            }
            if (null == tempBCs) {
                continue;
            }

            for (BoundaryCondition bc : tempBCs) {
                if (bc.getType() != BoundaryConditionType.Essential) {
                    continue;
                }

                tb = bc.getValues(bNode.getSegmentParm(), txy);
                if (BoundaryCondition.NOT == tb) {
                    continue;
                }

                rowcol = bNode.getMatrixIndex() * 2;
                if ((BoundaryCondition.X & tb) == BoundaryCondition.X) {
                    ux = txy[0];
                    for (i = 0; i < compRowMatrix.numRows(); i++) {
                        bVector.add(i, -compRowMatrix.get(i, rowcol) * ux);
                        compRowMatrix.set(i, rowcol, 0);
                    }
                    for (i = 0; i < compRowMatrix.numColumns(); i++) {
                        compRowMatrix.set(rowcol, i, 0);
                    }
                    bVector.set(rowcol, ux);
                    compRowMatrix.set(rowcol, rowcol, 1);
//                    for (i = 0; i < nodes.size(); i++) {
//                        bVector.add(i * 2, -compRowMatrix.get(i * 2, rowcol) * ux);
//                        bVector.add(i * 2 + 1, -compRowMatrix.get(i * 2 + 1, rowcol) * ux);
//                        compRowMatrix.set(i * 2, rowcol, 0);
//                        compRowMatrix.set(i * 2 + 1, rowcol, 0);
//                        compRowMatrix.set(rowcol, i * 2, 0);
//                        compRowMatrix.set(rowcol, i * 2 + 1, 0);
//                    }
//
//                    compRowMatrix.set(rowcol, rowcol, 1);
//                    bVector.set(rowcol, ux);
                }

                if ((BoundaryCondition.Y & tb) == BoundaryCondition.Y) {
                    uy = txy[1];
                    for (i = 0; i < compRowMatrix.numRows(); i++) {
                        bVector.add(i, -compRowMatrix.get(i, rowcol + 1) * uy);
                        compRowMatrix.set(i, rowcol + 1, 0);
                    }
                    for (i = 0; i < compRowMatrix.numColumns(); i++) {
                        compRowMatrix.set(rowcol + 1, i, 0);
                    }

                    compRowMatrix.set(rowcol + 1, rowcol + 1, 1);
                    bVector.set(rowcol + 1, uy);
                }

            }
        }
//        if (logDeep.isDebugEnabled()) {
//            logDeep.debug((compRowMatrix));
//        }
        if (logDeep.isDebugEnabled()) {
            for (BoundaryNode node : boundaryNodes) {
                if (node.getX() != 48) {
                    continue;
                }
                logDeep.debug(node.getX() + ", " + node.getY() + " ");
                logDeep.debug(compRowMatrix.getRow(node.getMatrixIndex() * 2));
                logDeep.debug("-----------------");
                logDeep.debug(compRowMatrix.getRow(node.getMatrixIndex() * 2 + 1));

            }
        }
        log.info("End of setEssentialBoundaryConditions");
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }
    UpperSymmBandMatrix matA;
    DenseVector xVector = new DenseVector(nodes.size() * 2);
    int quadN;

    public int getQuadN() {
        return quadN;
    }

    public void setQuadN(int quadN) {
        this.quadN = quadN;
    }

    public void solve() throws ArgumentOutsideDomainException {
        log.info("Start solve()");
        initNodesMatrixIndex();
        quadrateTriangleDomains(quadN);
        bVector = new DenseVector(nodes.size() * 2);

//        natureBoundaryQuadrate(quadN);

        applyEssentialBoundaryConditions();

//        AmdJni amdJni = new AmdJni();
//
//        matA = amdJni.complile(compRowMatrix, bVector);
        matA = new UpperSymmBandMatrix(compRowMatrix, compRowMatrix.numRows() / 2 + 1);
//        for (int i = 0; i < compRowMatrix.numRows(); i++) {
//            for (int j = i; j < compRowMatrix.numRows(); j++) {
//                if (compRowMatrix.get(i, j) - compRowMatrix.get(j, i) > 0.001) {
//                    System.out.println("fail!!!!!!!!!!!!!!!!!!!");
//                    System.out.println("i = " + i);
//                    System.out.println("j = " + j);
//                    System.out.println("compRowMatrix.get(i,j) = " + compRowMatrix.get(i, j));
//                    System.out.println("compRowMatrix.get(j,i) = " + compRowMatrix.get(j, i));
//                    return;
//                }
//            }
//        }

        log.info("solve the Ax=b now");
        xVector = new DenseVector(bVector.size());
//        for(int i=0;i<bVector.size();i++){
//            bVector.set(i,1);
//        }
        matA.solve(bVector, xVector);

        log.info("Finished: solve the Ax=b");
        int index;
        log.info("edit the nodes ux uy data");
        for (Node node : nodes) {
            //index = amdJni.P[node.getMatrixIndex()] * 2;
            index = node.getMatrixIndex() * 2;
            node.setUx(xVector.get(index));
            node.setUy(xVector.get(index + 1));
        }
        if (logDeep.isDebugEnabled()) {
            for (Node node : nodes) {
                if (node.getX() == 48) {
                    logDeep.debug(String.format("node y=%5.3f,fvector=%e %e", node.getY(), bVector.get(node.getMatrixIndex() * 2), bVector.get(node.getMatrixIndex() * 2 + 1)));
                }
            }
            for (Node node : nodes) {
                if (node.getX() == 0) {
                    logDeep.debug(String.format("es node y=%5.3f,fvector=%e %e", node.getY(), bVector.get(node.getMatrixIndex() * 2), bVector.get(node.getMatrixIndex() * 2 + 1)));
                }
            }
            for (Node node : nodes) {
                if (node.getX() != 0 && node.getX() != 48 && (bVector.get(node.getMatrixIndex() * 2) != 0 || bVector.get(node.getMatrixIndex() * 2 + 1) != 0)) {
                    logDeep.debug(String.format("es node x=%5.3f y=%5.3f,fvector=%e %e", node.getX(), node.getY(), bVector.get(node.getMatrixIndex() * 2), bVector.get(node.getMatrixIndex() * 2 + 1)));
                }
            }

        }

        DenseVector testVector = new DenseVector(bVector.size());
        matA.mult(xVector, testVector);
        for (int i = 0; i < xVector.size(); i++) {
            if (bVector.get(i) - testVector.get(i) > 1) {
                System.out.println(i);
                System.out.println(bVector.get(i));
                System.out.println(testVector.get(i));
            }
        }

        log.info("End of solve()");
    }
    boolean showNodes = true;
    double nodesScreenSize = 4;
    ViewMarkerType nodesScreenType = ViewMarkerType.Round;
    ViewMarkerType boundaryNodesScreenType = ViewMarkerType.X;
    double boundaryNodesScreenSize = 3;
    Color nodesColor = Color.RED;
    boolean showDisplacedNodes = true;
    Color nodesDisplacedColor = Color.lightGray;
    double displaceFactor = 500;
    boolean showTriangleDomain = true;
    Color triangleDomainColor = Color.lightGray;

    public double getDisplaceFactor() {
        return displaceFactor;
    }

    public void setDisplaceFactor(double displaceFactor) {
        this.displaceFactor = displaceFactor;
    }

    @Override
    public void paintModel(BufferedImage modelImage, ModelPanelManager manager) {
        Graphics2D g2 = modelImage.createGraphics();


        g2.setComposite(AlphaComposite.Clear);
        //g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());

        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();

        if (showNodes) {
            path.append(manager.viewMarker(nodes, nodesScreenSize, nodesScreenType), false);
            path.append(manager.viewMarker(boundaryNodes, boundaryNodesScreenSize * 2, boundaryNodesScreenType), false);
            g2.setColor(nodesColor);
            g2.draw(path.createTransformedShape(null));
        }

        path.reset();

        if (showTriangleDomain) {
            System.out.println("length of Triangle" + triangleQuadratureDomains.size());
            g2.setColor(triangleDomainColor);
            for (double[] tri : triangleQuadratureDomains) {
                path.moveTo(tri[0], tri[1]);
                path.lineTo(tri[2], tri[3]);
                path.lineTo(tri[4], tri[5]);
                path.lineTo(tri[0], tri[1]);
                path.closePath();
            }

            g2.draw(path.createTransformedShape(tx));


        }

        path.reset();
        if (showDisplacedNodes) {
            for (Node node : nodes) {
                manager.viewMarker(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy(), nodesScreenSize, nodesScreenType, path);

            }

            for (Node node : boundaryNodes) {
                manager.viewMarker(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy(), boundaryNodesScreenSize, boundaryNodesScreenType, path);
            }


            g2.setColor(nodesDisplacedColor);
            g2.draw(path.createTransformedShape(null));
            path.reset();
            for (Node node : nodes) {
                path.moveTo(node.getX(), node.getY());
                path.lineTo(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy());
            }
            g2.setColor(Color.BLUE);
            g2.draw(path.createTransformedShape(tx));
        }





    }

    private void initNodesMatrixIndex() {
        int i = 0;
        for (Node node : nodes) {
            node.setMatrixIndex(i);
            i++;
        }
    }
//

    public class SimpleRoundSupportDomain implements SupportDomain {

        double rMin;
        Node nodeFrom = Node.tempNode(0, 0);
        Node nodeTo = Node.tempNode(0, 0);
        private double rMax;
        private double maxStep;
        private int min;

        public SimpleRoundSupportDomain(double rMin, double rMax, double maxStep, int min) {
            this.rMin = rMin;
            this.rMax = rMax;
            this.maxStep = maxStep;
            this.min = min;
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
                LinkedList<ApproximatePoint> aps = new LinkedList<ApproximatePoint>();
                gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r + gm.getSegmentApproximateSize(), aps);

                GeometryUtils.insightNodes(x, y, r, null, 0, aps, nodes);
                r += (rMax - rMin) / maxStep;
            } while (nodes.size() < min && r < rMax + (rMax - rMin) / maxStep);
            if (nodes.size() < 0) {
                log.warn(String.format("SimpleRoundSupportDomain.supportNodes(%5.2f,%5.2f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", x, y, r - (rMax - rMin) / maxStep, nodes.size(), min));
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
            } while (nodes.size() < min && r < rMax + (rMax - rMin) / maxStep);
            output.addAll(nodes);
            if (nodes.size() < min) {
                log.warn(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output) (r=%5.3f) get less nodes than expected (%d<%d) ", bSegment, parm, r - (rMax - rMin) / maxStep, nodes.size(), min));
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("SimpleRoundSupportDomain.boundarySupportNodes(%s,%5.4f,output)%nnodesAverageDistance=%5.2f (r=%5.3f) output.size()=%d", bSegment, parm, nodesAverageDistance, r - (rMax - rMin) / maxStep, output.size()));
            }

            return nodesAverageDistance;
        }
    }

    public static void main(String[] args) {
        FlexCompRowMatrix matrix = new FlexCompRowMatrix(10, 10);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrix.set(i, j, 1);
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
        System.out.println(matrix);
    }
}
