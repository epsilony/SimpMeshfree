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
import net.epsilony.math.util.GaussLegendreQuadratureUtils;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition.BoundaryConditionType;
import net.epsilony.simpmeshfree.model.geometry.BoundaryNode;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.geometry.TriangleJni;
import net.epsilony.simpmeshfree.model.mechanics.SupportDomains.SimpleRoundSupportDomain;
import net.epsilony.simpmeshfree.shapefun.ShapeFunction;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.simpmeshfree.utils.ModelPanelManager.ViewMarkerType;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class ElectricModel implements ModelImagePainter {

    public ElectricModel(GeometryModel gm) {
        this.gm = gm;
    }
    Logger log = Logger.getLogger(ElectricModel.class);
    private TriangleJni triJni;
    private int quadN;
    private DenseVector bVector;
    private DenseVector xVector;

    public LinkedList<Node> getNodes() {
        return nodes;
    }
    GeometryModel gm;
    SupportDomain supportDomain;
    ShapeFunction shapeFunction;

    public void setQuadN(int quadN) {
        this.quadN = quadN;
    }

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
//    LayeredDomainTree<Node> nodesDomainTree = null;
    FlexCompRowMatrix kMat = null;

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }

    public RadialBasisFunction getRadialBasisFunction() {
        return radialBasisFunction;
    }

    public ShapeFunction getShapeFunction() {
        return shapeFunction;
    }

    public SupportDomain getSupportDomain() {
        return supportDomain;
    }

    private void initNodesMatrixIndex() {
        int i = 0;
        for (Node node : nodes) {
            node.setMatrixIndex(i);
            i++;
        }
    }

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
//        nodesDomainTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, true);
        triJni = triangleJni;
        log.info(String.format("End of generateNodesByTriangle%n nodes.size()=%d boundaryNodes.size()=%d", nodes.size(), boundaryNodes.size()));
    }

    public void quadrateTriangleDomains(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomainsByGrid(%d)", qn));
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        kMat = new FlexCompRowMatrix(nodes.size(), nodes.size());

        double[] weights = null;
        double[] points = null;

        weights = GaussLegendreQuadratureUtils.getWeights(qn);
        points = GaussLegendreQuadratureUtils.getPositions(qn);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }

        log.info("Triangle Domain Size:"+triangleQuadratureDomains.size());
        int count=0;
        for (double[] triangleDomain : triangleQuadratureDomains) {
            count++;
            if(count%10==0){
                log.info("Triangle Quadrated:"+count);
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
                            int nIndex = supportNodes.get(n).getMatrixIndex();
                            int mIndex = supportNodes.get(m).getMatrixIndex();
                            if (mIndex > nIndex) {
                                continue;
                            }
                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);
                            double td, w;
                            td = mx * nx + my * ny;
                            w = weights[k] * weights[l] * J;
                            kMat.add(mIndex, nIndex, w * td);
                        }
                    }
                }
            }
        }
        log.info("End of quadrateTriangleDomainsByGrid");
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

    public void applyEssentialBoundaryConditions() {
        log.info("Start applyAccurateEssentialBoundaryConditions");
        Segment segment;
        double segmentParm;

        LinkedList<BoundaryCondition> tempBCs;
        int rowcol;
        nodes.size();
        double[] txy = new double[2];
        double ux;
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

                rowcol = bNode.getMatrixIndex();

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
            }
        }
    }

    public void solve() throws ArgumentOutsideDomainException {
        log.info("Start solve()");
        initNodesMatrixIndex();
        quadrateTriangleDomains(quadN);
        bVector = new DenseVector(nodes.size());

//        natureBoundaryQuadrate(quadN);

        applyEssentialBoundaryConditions();

        RCMJni rcmJni = new RCMJni();
        Object[] results = rcmJni.compile(kMat, bVector);
        log.info("solve the Ax=b now");
        xVector = new DenseVector(bVector.size());
        ((UpperSymmBandMatrix) results[0]).solve((DenseVector) results[1], xVector);


        log.info("Finished: solve the Ax=b");
        int index;
        log.info("edit the nodes ux uy data");
        int[] PInv = rcmJni.PInv;
        for (Node node : nodes) {
            index = PInv[node.getMatrixIndex()] - 1;
            node.setUx(xVector.get(index));
        }
        log.info("End of solve()");
    }
    boolean showNodes = true;
    double nodesScreenSize = 4;
    ViewMarkerType nodesScreenType = ViewMarkerType.Round;
    ViewMarkerType boundaryNodesScreenType = ViewMarkerType.X;
    double boundaryNodesScreenSize = 3;
    Color nodesColor = Color.RED;
    boolean showGrand = true;
    Color grandColor = Color.lightGray;
    double displayFactor = 5;
    boolean showTriangleDomain = true;
    Color triangleDomainColor = Color.lightGray;
    List<Point> grandSamplePoints = null;

    public List<Point> getGrandSamplePoints() {
        return grandSamplePoints;
    }

    public void setGrandSamplePoints(List<Point> grandSamplePoints) {
        this.grandSamplePoints = grandSamplePoints;
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
        if (showGrand&&grandSamplePoints!=null) {
            path.append(manager.viewMarker(grandSamplePoints, nodesScreenSize, nodesScreenType), false);
            g2.setColor(grandColor);
            g2.draw(path.createTransformedShape(null));
            path.reset();
            ArrayList<Node> supportNodes = new ArrayList<Node>(100);
            Vector[] partialValues = null;
            SupportDomain postSupportDomain=new SimpleRoundSupportDomain(7, 10, 3, 6, gm, nodes);
            for (Point p : grandSamplePoints) {
                double x = p.getX();
                double y = p.getY();
                double nodesAverDistance = postSupportDomain.supportNodes(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                double px=0,py=0;
                for(int i=0;i<supportNodes.size();i++){
                    double u=supportNodes.get(i).getUx();
                    px+=u*partialValues[0].get(i);
                    py+=u*partialValues[1].get(i);
                }
                path.moveTo(x, y);
                path.lineTo(x+px*displayFactor, y+py*displayFactor);
            }
            g2.setColor(Color.BLUE);
            g2.draw(path.createTransformedShape(tx));
        }

    }

    public boolean isShowGrand() {
        return showGrand;
    }

    public void setShowGrand(boolean showGrand) {
        this.showGrand = showGrand;
    }

    public boolean isShowNodes() {
        return showNodes;
    }

    public void setShowNodes(boolean showNodes) {
        this.showNodes = showNodes;
    }

    public boolean isShowTriangleDomain() {
        return showTriangleDomain;
    }

    public void setShowTriangleDomain(boolean showTriangleDomain) {
        this.showTriangleDomain = showTriangleDomain;
    }

    public double getDisplayFactor() {
        return displayFactor;
    }

    public void setDisplayFactor(double displayFactor) {
        this.displayFactor = displayFactor;
    }
}
