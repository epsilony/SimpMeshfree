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
import java.util.Collection;
import java.util.LinkedList;
import net.epsilony.math.analysis.GaussLegendreQuadrature;
import net.epsilony.math.radialbasis.RadialBasisFunction;
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
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
 abstract class AbstractModel implements ModelImagePainter{

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
//            System.out.println("length of Triangle" + triangleQuadratureDomains.size());
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
    }

    static Logger log = Logger.getLogger(AbstractModel.class);
    DenseVector bVector;
    LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();
    double boundaryNodesScreenSize = 3;
    ViewMarkerType boundaryNodesScreenType = ViewMarkerType.X;
    GeometryModel gm;
    FlexCompRowMatrix kMat = null;
    UpperSymmBandMatrix matA;
    LinkedList<Node> nodes = new LinkedList<Node>();
    Color nodesColor = Color.RED;
    double nodesScreenSize = 4;
    ViewMarkerType nodesScreenType = ViewMarkerType.Round;
    int quadratureNum;
    RadialBasisFunction radialBasisFunction;
    ShapeFunction shapeFunction;
    boolean showNodes = true;
    boolean showTriangleDomain = true;
    SupportDomain supportDomain;
    TriangleJni triJni;
    Color triangleDomainColor = Color.lightGray;
    LinkedList<double[]> triangleQuadratureDomains = new LinkedList<double[]>();
    LinkedList<double[]> rectangleQuadratureDomains = new LinkedList<double[]>();

    public LinkedList<double[]> getRectangleQuadratureDomains() {
        return rectangleQuadratureDomains;
    }

    
    DenseVector xVector = new DenseVector(nodes.size() * 2);

    public AbstractModel() {
    }

    public void generateBoundaryNodeByApproximatePoints(double size, double flatness) {
        gm.compile(size, flatness);
        for (Route route : gm.getRoutes()) {
            for (ApproximatePoint ap : route.GetApproximatePoints()) {
                BoundaryNode bn = new BoundaryNode(ap);
                nodes.add(bn);
                boundaryNodes.add(bn);
            }
        }
    }

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

    public LinkedList<BoundaryNode> getBoundaryNodes() {
        return boundaryNodes;
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public int getQuadratureNum() {
        return quadratureNum;
    }

    public SupportDomain getSupportDomain() {
        return supportDomain;
    }

    abstract public void initialKMatrix();

    public void quadrateRectangleDomains(int qn) throws ArgumentOutsideDomainException{
        log.info(String.format("Start quadrateRectangleDomains(%d)", qn));
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        initialKMatrix();

        double[] weights = null;
        double[] points = null;
        weights = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(qn);
        points = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(qn);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }

        for (double[] rects:rectangleQuadratureDomains) {
            double x1 = rects[0];
            double y1 = rects[1];
            double x2 = rects[2];
            double y2 = rects[3];
            double u;
            double v;
            double x;
            double y;
            double J=0.25*Math.abs((x2-x1)*(y2-y1));
            double w;
            for (int k = 0; k < weights.length; k++) {
                for (int l = 0; l < weights.length; l++) {
                    u = (points[k] + 1) / 2;
                    v = (points[l] + 1) / 2;
                    x =x1+(x2-x1)*u;
                    y =y1+(y2-y1)*v;
                    w = weights[k] * weights[l] * J;
                    nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                    radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                    partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                    for (int m = 0; m < supportNodes.size(); m++) {
                        double mx = partialValues[0].get(m);
                        double my = partialValues[1].get(m);
                        for (int n = 0; n < supportNodes.size(); n++) {
                            int nIndex = supportNodes.get(n).getMatrixIndex();
                            int mIndex = supportNodes.get(m).getMatrixIndex();
                            if (mIndex > nIndex) {
                                continue;
                            }
                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);
                            quadrateCore(mIndex, mx, my, nIndex, nx, ny, w);
                        }
                    }
                }
            }
        }
        log.info("End of quadrateTriangleDomainsByGrid");
    }

    public void quadrateTriangleDomainsByGrid(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomainsByGrid(%d)", qn));
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        initialKMatrix();

        double[] weights = null;
        double[] points = null;
        weights = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(qn);
        points = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(qn);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }

        for (double[] triangleDomain : triangleQuadratureDomains) {
            double x1 = triangleDomain[0];
            double y1 = triangleDomain[1];
            double x2 = triangleDomain[2];
            double y2 = triangleDomain[3];
            double x3 = triangleDomain[4];
            double y3 = triangleDomain[5];
            double u;
            double v;
            double x;
            double y;
            double J;
            double w;
            for (int k = 0; k < weights.length; k++) {
                for (int l = 0; l < weights.length; l++) {
                    u = (points[k] + 1) / 2;
                    v = (points[l] + 1) / 2;
                    x = x1 + u * x2 - u * x1 + u * x3 * v - u * x2 * v;
                    y = y1 + u * y2 - u * y1 + v * u * y3 - v * u * y2;
                    J = 0.25 * Math.abs(u * (x2 * y3 - x1 * y3 + x1 * y2 - x3 * y2 + x3 * y1 - x2 * y1));
                    w = weights[k] * weights[l] * J;
                    nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                    radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                    partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                    for (int m = 0; m < supportNodes.size(); m++) {
                        double mx = partialValues[0].get(m);
                        double my = partialValues[1].get(m);
                        for (int n = 0; n < supportNodes.size(); n++) {
                            int nIndex = supportNodes.get(n).getMatrixIndex();
                            int mIndex = supportNodes.get(m).getMatrixIndex();
                            if (mIndex > nIndex) {
                                continue;
                            }
                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);
                            quadrateCore(mIndex, mx, my, nIndex, nx, ny, w);
                        }
                    }
                }
            }
        }
        log.info("End of quadrateTriangleDomainsByGrid");
    }

    abstract public void quadrateCore(int mIndex, double dphim_dx, double dphim_dy, int nIndex, double dphin_dx, double dphin_dy, double coefs);

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

                rowcol = bNode.getMatrixIndex();
                accurateEssentialCore(txy, rowcol, tb);
            }
        }
        log.info("End of applyAccurateEssentialBoundaryConditions");
    }

    abstract public void accurateEssentialCore(double[] values, int index, byte flag);

    public void setQuadratureNum(int quadN) {
        this.quadratureNum = quadN;
    }

    public void setRadialBasisFunction(RadialBasisFunction radialBasisFunction) {
        this.radialBasisFunction = radialBasisFunction;
    }

    public void setShapeFunction(ShapeFunction shapeFunction) {
        this.shapeFunction = shapeFunction;
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
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
        ApproximatePoint aprxStart;
        ApproximatePoint aprx;
        ApproximatePoint aprxFront;
        Segment segment = null;
        Vector shapeVector;
        double t;
        double w;
        double t1;
        double t2;
        double x;
        double y;
        double ds;
        double parmStart;
        double parmEnd;
        BoundaryCondition tempBC;
        double[] txy = new double[2];
        double[] quadratePoints = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(n);
        double[] quadrateCoefs = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(n);
        int i = 0;
        int j;
        int k;
        int row;
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

                            supportDomain.boundarySupportNodes(segment, t, supportNodes);
                            segment.parameterPoint(t, txy);
                            x = txy[0];
                            y = txy[1];
                            shapeVector = shapeFunction.shapeValues(supportNodes, x, y);
                            for (k = 0; k < shapeVector.size(); k++) {
                                row = supportNodes.get(k).getMatrixIndex();
                                natureConBoundaryQuadrateCore(values, row, shapeVector.get(k));
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("conNaturalBCs applied");
                        }
                    }
                    if (log.isDebugEnabled() && !conNaturalBCs.isEmpty()) {
                        log.debug("conNaturalBCs applied");
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
                        double coef = ds * w;
                        for (j = 0; j < naturalBCs.size(); j++) {
                            tempBC = naturalBCs.get(j);
                            if (0 != tempBC.getValues(t, txy)) {
                                for (k = 0; k < supportNodes.size(); k++) {
                                    row = supportNodes.get(k).getMatrixIndex();
                                    natureBoundaryQudarateCore(txy, row, shapeVector.get(k), coef);

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

    abstract public void natureConBoundaryQuadrateCore(double[] values, int index, double phi);

    abstract public void natureBoundaryQudarateCore(double[] values, int index, double phi, double coef);
}