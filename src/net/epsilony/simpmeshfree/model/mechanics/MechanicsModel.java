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

import net.epsilony.math.analysis.GaussLegendreQuadrature;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.math.util.EYMath;
import net.epsilony.math.util.AreaCoordTriangleQuadrature;

import net.epsilony.simpmeshfree.model.ModelTestFrame;
import net.epsilony.simpmeshfree.model.geometry.ApproximatePoint;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition.BoundaryConditionType;
import net.epsilony.simpmeshfree.model.geometry.BoundaryNode;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Route;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.geometry.TriangleJni;
import net.epsilony.simpmeshfree.shapefun.ShapeFunction;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.simpmeshfree.utils.ModelPanelManager.ViewMarkerType;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Logger;

/**
 * <br>RPIM法无网格法</br>
 * <br>正确运行本类的一个最简单代法见{@link ModelTestFrame}的构造部分</br>
 * <br>其中至少需要调用以下函数以完成一个次最简单的计算</br>
 * <br>{@link MechanicsModel#setConstitutiveLaw(no.uib.cipr.matrix.Matrix) }，</br>
 * <br>{@link MechanicsModel#setRadialBasisFunction(net.epsilony.math.radialbasis.RadialBasisFunction) }</br>
 * <br>{@link MechanicsModel#setShapeFunction(net.epsilony.simpmeshfree.shapefun.ShapeFunction) }</br>
 * <br>{@link MechanicsModel#setSupportDomain(net.epsilony.simpmeshfree.model.mechanics.SupportDomain) }</br>
 * @author epsilon
 */
public class MechanicsModel implements ModelImagePainter {

    public LinkedList<Node> getNodes() {
        return nodes;
    }
    GeometryModel gm;
    SupportDomain supportDomain;
    ShapeFunction shapeFunction;

    /**
     * 设置形函数
     * @param shapeFunction
     */
    public void setShapeFunction(ShapeFunction shapeFunction) {
        this.shapeFunction = shapeFunction;
    }
    RadialBasisFunction radialBasisFunction;

    /**
     * 设置形函数所用的径向基函数
     * @param radialBasisFunction
     */
    public void setRadialBasisFunction(RadialBasisFunction radialBasisFunction) {
        this.radialBasisFunction = radialBasisFunction;
    }
    LinkedList<Node> nodes = new LinkedList<Node>();
    LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();
    LinkedList<double[]> triangleQuadratureDomains = new LinkedList<double[]>();
    FlexCompRowMatrix kMat = null;
    DenseMatrix constitutiveLaw = null;

    public Matrix getConstitutiveLaw() {
        return constitutiveLaw;
    }

    /**
     * 设置本构矩阵
     * @param constitutiveLaw
     */
    public void setConstitutiveLaw(Matrix constitutiveLaw) {
        this.constitutiveLaw = new DenseMatrix(constitutiveLaw);
    }
    DenseVector bVector;
    static Logger log = Logger.getLogger(MechanicsModel.class);
    static Logger logDeep = Logger.getLogger(MechanicsModel.class.getName() + ".deep1");
    int logi;

    public MechanicsModel(GeometryModel gm) {
        this.gm = gm;
    }
    TriangleJni triJni;

    /**
     * 用过将几何模形多边形化 调用triangle作Delaunay划分 构造结构内的结点
     * @param size 多边形代中边长的最大长度
     * @param flatness 多边形化中多边形离原曲线的最远距离
     * @param s triangle函数的switchs
     * @param needNeighbors 是否填写结点的邻居信息
     * @param resetNodesIndex 是否重置结点的编号（从1开始）
     */
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
        quadrateTriangleDomainsByGrid(qn);
    }

    public void quadrateTriangleDomainsByGrid(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomainsByGrid(%d)", qn));
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        kMat = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);

        double[] weights = null;
        double[] points = null;

        weights = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(qn);
        points = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(qn);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }
        double d00 = constitutiveLaw.get(0, 0);
        double d01 = constitutiveLaw.get(0, 1);
        double d10 = constitutiveLaw.get(1, 0);
        double d11 = constitutiveLaw.get(1, 1);
        double d22 = constitutiveLaw.get(2, 2);
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
            double u, v, x, y, J;


            for (int k = 0; k < weights.length; k++) {
                for (int l = 0; l < weights.length; l++) {
                    u = (points[k] + 1) / 2;
                    v = (points[l] + 1) / 2;
                    x = x1 + u * x2 - u * x1 + u * x3 * v - u * x2 * v;
                    y = y1 + u * y2 - u * y1 + v * u * y3 - v * u * y2;
                    J = 0.25 * Math.abs(u * (x2 * y3 - x1 * y3 + x1 * y2 - x3 * y2 + x3 * y1 - x2 * y1));
                    nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                    radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                    partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                    for (int m = 0; m < supportNodes.size(); m++) {
                        double mx = partialValues[0].get(m);
                        double my = partialValues[1].get(m);
                        for (int n = 0; n < supportNodes.size(); n++) {
                            int nIndex = supportNodes.get(n).getMatrixIndex() * 2;
                            int mIndex = supportNodes.get(m).getMatrixIndex() * 2;
                            if (mIndex > nIndex) {
                                continue;
                            }


                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);

                            double td, w;

                            td = mx * d00 * nx + my * d22 * ny;
                            w = weights[k] * weights[l] * J;
                            kMat.add(mIndex, nIndex, w * td);
                            td = mx * d01 * ny + my * d22 * nx;
                            kMat.add(mIndex, nIndex + 1, w * td);
                            td = my * d11 * ny + mx * d22 * nx;
                            kMat.add(mIndex + 1, nIndex + 1, w * td);
                            if (mIndex != nIndex) {
                                td = my * d10 * nx + mx * d22 * ny;
                                kMat.add(mIndex + 1, nIndex, w * td);
                            }
                        }
                    }
                }
            }
        }
        log.info("End of quadrateTriangleDomainsByGrid");
    }

    public void quadrateTriangleDomainsByAreaCoord(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomainsByAreaCoord(%d)", qn));
        double x, y, w, area;
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        kMat = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);
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
        int tsum = 0;
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
                        logDeep.debug(String.format("%d:point(%.2f,%.2f)is outof %s", tsum, x, y, Arrays.toString(triangleDomain)));
                    }
                }
                w = weights[i];
                nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                for (k = 0; k < supportNodes.size(); k++) {
                    int kIndex = supportNodes.get(k).getMatrixIndex() * 2;

                    for (l = 0; l < supportNodes.size(); l++) {
                        int lIndex = supportNodes.get(l).getMatrixIndex() * 2;
                        if (k < l) {
                            continue;
                        }
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

                        kMat.add(kIndex, lIndex, kkl.get(0, 0) * w * area);
                        kMat.add(kIndex, lIndex + 1, kkl.get(0, 1) * w * area);
                        kMat.add(kIndex + 1, lIndex + 1, kkl.get(1, 1) * w * area);
                        kMat.add(kIndex + 1, lIndex, kkl.get(1, 0) * w * area);
                    }
                }
            }
        }


        log.info("End of quadrateTriangleDomainsByAreaCoord");
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
//        applyEssentialBoundaryConditionsByPenalty();
        applyAccurateEssentialBoundaryConditions();
    }

    public void applyEssentialBoundaryConditionsByPenalty() {
        log.info("Start applyEssentialBoundaryConditionsByPenalty");
        Segment segment;
        double segmentParm;

        LinkedList<BoundaryCondition> tempBCs;
        int rowcol;
        nodes.size();
        double[] txy = new double[2];
        double ux, uy;
        byte tb;
        double kMax = kMat.get(0, 0);
        for (int i = 0; i < kMat.numRows(); i++) {
            if (kMax < kMat.get(i, i)) {
                kMax = kMat.get(i, i);
            }
        }
        double alpha = 1e8 * kMax;
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
                    double kii = kMat.get(rowcol, rowcol);
                    kMat.set(rowcol, rowcol, alpha * kii);
                    bVector.set(rowcol, alpha * kii * ux);
                }

                if ((BoundaryCondition.Y & tb) == BoundaryCondition.Y) {
                    uy = txy[1];
                    double kii = kMat.get(rowcol + 1, rowcol + 1);
                    kMat.set(rowcol + 1, rowcol + 1, alpha * kii);
                    bVector.set(rowcol + 1, alpha * kii * uy);
                }
            }
        }
        log.info("End of applyEssentialBoundaryConditionsByPenalty");
    }

    public void applyAccurateEssentialBoundaryConditions() {
        log.info("Start applyAccurateEssentialBoundaryConditions");
        Segment segment;
        double segmentParm;

        LinkedList<BoundaryCondition> tempBCs;
        int rowcol;
        nodes.size();
        double[] txy = new double[2];
        double ux, uy;
        int i;
        byte tb;
//        SparseVector rowVector;
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
                    double kk = 0;

                    for (i = 0; i < rowcol; i++) {
                        kk = kMat.get(i, rowcol);
                        if (0 != kk) {
                            bVector.add(i, -kk * ux);
                            kMat.set(i, rowcol, 0);
                        }
                    }
                    for (VectorEntry vn : kMat.getRow(rowcol)) {
                        bVector.add(vn.index(), -vn.get() * ux);
                    }
                    kMat.setRow(rowcol, new SparseVector(kMat.numColumns()));
                    kMat.set(rowcol, rowcol, 1);
                    bVector.set(rowcol, ux);
                }

                if ((BoundaryCondition.Y & tb) == BoundaryCondition.Y) {
                    uy = txy[1];
                    double kk = 0;
                    for (i = 0; i < rowcol + 1; i++) {
                        kk = kMat.get(rowcol + 1, i);
                        if (kk != 0) {
                            bVector.add(i, -kk * uy);
                            kMat.set(i, rowcol + 1, 0);
                        }
                    }
                    for (VectorEntry vn : kMat.getRow(rowcol)) {
                        bVector.add(vn.index(), -vn.get() * uy);
                    }
                    kMat.setRow(rowcol + 1, new SparseVector(kMat.numColumns()));
                    kMat.set(rowcol + 1, rowcol + 1, 1);
                    bVector.set(rowcol + 1, uy);
                }

            }
        }


        log.info("End of applyAccurateEssentialBoundaryConditions");
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

        natureBoundaryQuadrate(quadN);

        applyEssentialBoundaryConditions();

        RCMJni rcmJni = new RCMJni();
        Object[] results = rcmJni.compile(kMat, bVector);
        log.info("solve the Ax=b now");
        xVector = new DenseVector(bVector.size());
        ((UpperSymmBandMatrix) results[0]).solve((DenseVector) results[1], xVector);


        log.info("Finished: solve the Ax=b");
        rcmJni.fillDisplacement(xVector, nodes);
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

   

    public static void main(String[] args) {
        UpperSymmPackMatrix mat = new UpperSymmPackMatrix(3);
        mat.set(3, 1, 1);
        System.out.println(mat);
    }
}
