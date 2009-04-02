/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.utils.DomainSelectListener;
import net.epsilony.simpmeshfree.utils.ModelImageWriter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.util.collection.LayeredDomainTree;

/**
 *
 * @author epsilon
 */
public class GeometryModel implements ModelImageWriter, DomainSelectListener {

    public LinkedList<Point> getPoints() {
        return points;
    }

    public void setPoints(LinkedList<Point> points) {
        this.points = points;
    }

    public LinkedList<SegmentRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(LinkedList<SegmentRoute> routes) {
        this.routes = routes;
    }
    LinkedList<SegmentRoute> routes = new LinkedList<SegmentRoute>();
    LinkedList<Point> points = new LinkedList<Point>();
    LinkedList<ApproximatePoint> aprxPts = new LinkedList<ApproximatePoint>();
    LinkedList<Point> holes = new LinkedList<Point>();
    ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    ArrayList<Node> nodes = new ArrayList<Node>();
    final AffineTransform itrx = new AffineTransform();
    private int compileTime = 0;
    LayeredDomainTree<Node> nodesSearchTree;
    LayeredDomainTree<ApproximatePoint> approximatePointsSearchTree;
    boolean wider = true;
    double segmentApproximateSize;
    double segmentFlatness;
    Point leftDown = Point.tempPoint(0, 0);
    Point rightUp = Point.tempPoint(0, 0);

    public Point getLeftDown() {
        return leftDown;
    }

    public Point getRightUp() {
        return rightUp;
    }

    public Point newPoint(double x, double y) {
        Point p = new Point(x, y);
        points.add(p);
        return p;
    }
    final double[] addShapeTds = new double[6];

    public void addHole(double x, double y) {
        holes.add(Point.tempPoint(x, y));
    }

    Shape modelShape;
    public void addShape(Shape shape) {
        this.modelShape=shape;
        PathIterator pi = shape.getPathIterator(itrx);
        boolean hasMoveTo = false;
        boolean hasClose = false;
        Point lastEnd = null;
        Point routeHead = null;
        SegmentRoute rt = null;
        Point tp, tp1, tp2;
        while (!pi.isDone()) {
            switch (pi.currentSegment(addShapeTds)) {
                case PathIterator.SEG_MOVETO:
                    if (hasMoveTo && !hasClose) {
                        rt.add(new LineSegment(lastEnd, routeHead));
//                        rt.compile();
                        routes.add(rt);
                    }
                    lastEnd = new Point(addShapeTds[0], addShapeTds[1]);
                    rt = new SegmentRoute();
                    routeHead = lastEnd;
                    hasMoveTo = true;
                    hasClose = false;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (lastEnd.x != routeHead.x || lastEnd.y != routeHead.y) {
                        rt.add(new LineSegment(lastEnd, routeHead));
//                        System.out.println("close");
                    } else {
                        rt.getLast().setRightVertex(rt.getFirst().getLeftVertex());
//                        System.out.println(rt.getLast().getRightVertex().x+" "+rt.getLast().getRightVertex().getY());
//                        System.out.println("close2");
                    }

                    hasClose = true;
//                    System.out.println(rt);
//                    rt.compile();
                    routes.add(rt);
                    break;
                case PathIterator.SEG_LINETO:
                    tp = new Point(addShapeTds[0], addShapeTds[1]);

                    rt.add(new LineSegment(lastEnd, tp));
                    lastEnd = tp;
//                    System.out.println("line");
                    break;
                case PathIterator.SEG_QUADTO:
                    tp = new Point(addShapeTds[0], addShapeTds[1]);
                    tp1 = new Point(addShapeTds[2], addShapeTds[3]);
//                    System.out.println("quad");
                    rt.add(CubicBezierSegment.fromQuadBezierPoints(lastEnd, tp, tp1));
                    lastEnd = tp1;
                    break;
                case PathIterator.SEG_CUBICTO:
                    tp = new Point(addShapeTds[0], addShapeTds[1]);
                    tp1 = new Point(addShapeTds[2], addShapeTds[3]);
                    tp2 = new Point(addShapeTds[4], addShapeTds[5]);
                    rt.add(new CubicBezierSegment(lastEnd, tp, tp1, tp2));
                    lastEnd = tp2;
                    break;
            }
            pi.next();
        }
//        System.out.println("routes.size() = " + routes.size());

    }
    TriangleJni triangleJni = new TriangleJni();

    public double getSegmentApproximateSize() {
        return segmentApproximateSize;
    }

    public double getSegmentFlatness() {
        return segmentFlatness;
    }

    public void compile(double size, double flatness, String switches) {
        segmentApproximateSize = size;
        segmentFlatness = flatness;
        approximatePoint(size, flatness);
        leftDown.setXY(aprxPts.getFirst());
        rightUp.setXY(aprxPts.getFirst());
        for (ApproximatePoint ap : aprxPts) {
            if (ap.x < leftDown.x) {
                leftDown.x = ap.x;
            } else {
                if (ap.x > rightUp.x) {
                    rightUp.x = ap.x;
                }
            }
            if (ap.y < leftDown.y) {
                leftDown.y = ap.y;
            } else {
                if (ap.y > rightUp.y) {
                    rightUp.y = ap.y;
                }
            }
        }
        triangleJni.setSwitchs(switches);
        triangleJni.setPointsSegments(aprxPts, points);
        double[] tds = new double[holes.size() * 2];
        int i = 0;
        for (Point p : holes) {
            tds[i * 2] = p.x;
            tds[i * 2 + 1] = p.y;
            i++;
        }
        triangleJni.setHoles(tds, holes.size());
        triangleJni.triangleFun();
        triangleJni.getNodesTriangles(nodes, triangles);
        System.out.println("nodes.size() = " + nodes.size());
        nodesSearchTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, wider);
        approximatePointsSearchTree = new LayeredDomainTree<ApproximatePoint>(aprxPts, Point.compX, Point.compY, wider);
        compileTime++;
    }
    private Node searchNodeFrom = Node.tempNode(0, 0),  searchNodeTo = Node.tempNode(0, 0);
    private Point searchPointFrom = Point.tempPoint(0, 0),  searchPointTo = Point.tempPoint(0, 0);
    private ApproximatePoint searchApproximatePointFrom = ApproximatePoint.tempApproximatePoint(0, 0),  searchApproximatePointTo = ApproximatePoint.tempApproximatePoint(0, 0);

    public List<Node> nodeDomainSearch(double x1, double y1, double x2, double y2, List<Node> outPts) {
        double t;
        if (x1 > x2) {
            t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            t = y1;
            y1 = y2;
            y2 = t;
        }
        searchNodeFrom.setXY(x1, y1);
        searchNodeTo.setXY(x2, y2);
        outPts.clear();
        return nodesSearchTree.domainSearch(outPts, searchNodeFrom, searchNodeTo);
    }

    /**
     * 
     * @param <E>
     * @param center
     * @param size half length of the square edge
     * @param outPts
     * @return
     */
    public <E extends Point> List<E> pointDomainSearch(E center, double size, List<E> outPts) {
        size = size > 0 ? size : -size;
        double x1 = center.x - size,
                x2 = center.x + size,
                y1 = center.y - size,
                y2 = center.y + size;
        outPts.clear();
        switch (center.type()) {
            case Node:
                searchNodeFrom.setXY(x1, y1);
                searchNodeTo.setXY(x2, y2);
                nodesSearchTree.domainSearch((List<Node>) outPts, searchNodeFrom, searchNodeTo);
                break;
            case ApproximatPoint:
                searchApproximatePointFrom.setXY(x1, y1);
                searchApproximatePointTo.setXY(x2, y2);
                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, searchApproximatePointFrom, searchApproximatePointTo);
            default:
                throw new UnsupportedOperationException();
        }
        return outPts;
    }

    public <E extends Point> List<E> pointDomainSearch(E corner1, E corner2, List<E> outPts) {
        if (corner1.x > corner2.x) {
            double t = corner2.x;
            corner2.x = corner1.x;
            corner1.x = t;
        }
        if (corner2.y > corner2.y) {
            double t = corner2.y;
            corner2.y = corner1.y;
            corner1.y = t;
        }
        switch (corner1.type()) {
            case Node:
                nodesSearchTree.domainSearch((List<Node>) outPts, (Node) corner1, (Node) corner2);
                break;
            case ApproximatPoint:
                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, (ApproximatePoint) corner1, (ApproximatePoint) corner2);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return outPts;
    }
    private LinkedList<ApproximatePoint> segmentSearchApproximatePointList = new LinkedList<ApproximatePoint>();
    private TreeSet<Segment> segmentSearchSet = new TreeSet<Segment>(ModelElement.comparator);

    public List<Segment> segmentSearch(double x1, double y1, double x2, double y2, List<Segment> outSegs) {
        outSegs.clear();
        if (x1 > x2) {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        searchApproximatePointFrom.setXY(x1 - segmentApproximateSize, y1 - segmentApproximateSize);
        searchApproximatePointTo.setXY(x2 + segmentApproximateSize, y2 + segmentApproximateSize);
        approximatePointsSearchTree.domainSearch(segmentSearchApproximatePointList, searchApproximatePointFrom, searchApproximatePointTo);
        segmentSearchSet.clear();
        for (ApproximatePoint p : segmentSearchApproximatePointList) {
            if (p.x <= x2 && p.x >= x1 || p.y <= y2 && p.y >= y1) {
                segmentSearchSet.add(p.segment);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.l.x, p.l.y)) {
                segmentSearchSet.add(p.segment);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.r.x, p.r.y)) {
                segmentSearchSet.add(p.segment);
            }
        }
        outSegs.addAll(segmentSearchSet);
        return outSegs;
    }
    private TreeSet<SegmentRoute> segmentRouteSearchSet = new TreeSet<SegmentRoute>(ModelElement.comparator);

    public List<SegmentRoute> segmentRouteSearch(double x1, double y1, double x2, double y2, List<SegmentRoute> outSegs) {
        outSegs.clear();
        if (x1 > x2) {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        searchApproximatePointFrom.setXY(x1 - segmentApproximateSize, y1 - segmentApproximateSize);
        searchApproximatePointTo.setXY(x2 + segmentApproximateSize, y2 + segmentApproximateSize);
        approximatePointsSearchTree.domainSearch(segmentSearchApproximatePointList, searchApproximatePointFrom, searchApproximatePointTo);
        segmentRouteSearchSet.clear();
        for (ApproximatePoint p : segmentSearchApproximatePointList) {
            if (p.x <= x2 && p.x >= x1 || p.y <= y2 && p.y >= y1) {
                segmentRouteSearchSet.add(p.segment.route);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.l.x, p.l.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.l.x, p.l.y)) {
                segmentRouteSearchSet.add(p.segment.route);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.r.x, p.r.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.r.x, p.r.y)) {
                segmentRouteSearchSet.add(p.segment.route);
            }
        }
        outSegs.addAll(segmentRouteSearchSet);
        return outSegs;
    }

    public static <E extends Point> boolean canSeeEach(E e1, E e2, Collection<ApproximatePoint> aps) {
        for (ApproximatePoint ap : aps) {
            if (EYMath.isLineSegmentIntersect(e1.x, e1.y, e2.x, e2.y, ap.x, ap.y, ap.l.x, ap.l.y) || EYMath.isLineSegmentIntersect(e1.x, e1.y, e2.x, e2.y, ap.x, ap.y, ap.r.x, ap.r.y)) {
                return false;
            }
        }
        return true;
    }
    private int nodesNetShapeTime = 0;
    private Path2D nodesNetPath = new Path2D.Double();

    public Shape nodesNetShape(AffineTransform tr) {
        if (nodesNetShapeTime != compileTime) {

            setNodesFlag(0);
            nodesNetPath.reset();
            Node n = nodes.get(0);
            nodesNetPath = n.neighborNetPath(nodesNetPath);
            nodesNetShapeTime = compileTime;
        }
        return nodesNetPath.createTransformedShape(tr);
    }

    public void setNodesFlag(int flag) {
        for (Node n : nodes) {
            n.flag = flag;
        }
    }

    /**
     *
     * @param size
     * @param flatness
     * @return
     */
    private LinkedList<ApproximatePoint> approximatePoint(double size, double flatness) {
        for (SegmentRoute sr : routes) {
//            System.out.println("sr.type"+sr.type());
            aprxPts.addAll(sr.approximatePoints(size, flatness));
        }
//        System.out.println("aprxPts.size() = " + aprxPts.size());
        return aprxPts;
    }
    private ApproximatePoint tempApproximatePointSearchAp1 = ApproximatePoint.tempApproximatePoint(0, 0);
    private ApproximatePoint tempApproximatePointSearchAp2 = ApproximatePoint.tempApproximatePoint(0, 0);

    public List<ApproximatePoint> approximatePointSearch(List<ApproximatePoint> list, ApproximatePoint from, ApproximatePoint to) {
        if (from.x > to.x) {
            tempApproximatePointSearchAp1.x = to.x;
            tempApproximatePointSearchAp2.x = from.x;
        } else {
            tempApproximatePointSearchAp1.x = from.x;
            tempApproximatePointSearchAp2.x = to.x;
        }
        if (from.y > to.y) {
            tempApproximatePointSearchAp1.y = to.y;
            tempApproximatePointSearchAp2.y = from.y;
        } else {
            tempApproximatePointSearchAp1.y = from.y;
            tempApproximatePointSearchAp2.y = to.y;
        }
        return approximatePointsSearchTree.domainSearch(list, tempApproximatePointSearchAp1, tempApproximatePointSearchAp2);
    }

    public List<ApproximatePoint> approximatePointSearch(List<ApproximatePoint> list, double x1, double y1, double x2, double y2) {
        if (x1 < x2) {
            tempApproximatePointSearchAp1.x = x1;
            tempApproximatePointSearchAp2.x = x2;
        } else {
            tempApproximatePointSearchAp1.x = x2;
            tempApproximatePointSearchAp2.x = x1;
        }
        if (y1 < y2) {
            tempApproximatePointSearchAp1.y = y1;
            tempApproximatePointSearchAp2.y = y2;
        } else {
            tempApproximatePointSearchAp1.y = y2;
            tempApproximatePointSearchAp2.y = y1;
        }
        return approximatePointsSearchTree.domainSearch(list, tempApproximatePointSearchAp1, tempApproximatePointSearchAp2);
    }

    public LinkedList<ApproximatePoint> getApproximatePts() {
        return aprxPts;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Triangle> getTriangles() {
        return triangles;
    }

    public static void main(String[] args) {
        GeometryModel gm = new GeometryModel();
        gm.addShape(new Rectangle2D.Double(0, 0, 48, 12));
    }

   

    @Override
    public void writeModelBuffer(BufferedImage modelImage, ModelPanelManager manager) {
        Graphics2D g2 = modelImage.createGraphics();
//        g2.setBackground(Color.WHITE);
//         g2.clearRect(0, 0, modelImage.getWidth(), modelImage.getHeight());
        //g2.setComposite(AlphaComposite.Clear);

        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0,0,modelImage.getWidth(),modelImage.getHeight());
     
        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();
//        if (showModelShape) {
//            for (SegmentRoute route : routes) {
//
//                for (Segment segment : route.getSegments()) {
//                    path.moveTo(segment.getLeftVertex().getX(), segment.getRightVertex().getY());
//                    segment.addToPath(path);
//                }
//            }
//        }
        path.append(modelShape, false);
        g2.setColor(showModelShapeColor);
        g2.draw(path.createTransformedShape(tx));
        path.reset();


        if (showNode) {
            System.out.println("showNode"+nodes.size());
            path.append(manager.viewMarker(nodes, showNodeSize, showNodeMarkerType), false);
        }
        g2.setColor(showNodeColor);
        g2.draw(path.createTransformedShape(null));
        path.reset();
        
        if (showNodeNeighborNet) {
            if (nodes.size() > 0) {
                Node n = nodes.get(0);
                n.neighborNetPath(path);
            }
        }
        g2.setColor(showNodeNeigborNetColor);
        g2.draw(path.createTransformedShape(tx));
        path.reset();

        if(showApproximatePoint){
            path.append(manager.viewMarker(aprxPts, showApproximatePointSize, showApproximatePointType),false);
        }
        g2.setColor(showApproximateColor);
        g2.draw(path.createTransformedShape(null));
        path.reset();
    }
    boolean showModelShape = true;
    boolean showNode = true;
    boolean showNodeNeighborNet = false;
    boolean showApproximatePoint = true;
    boolean showApproximatePointRoute = false;
    double showNodeSize = 5;
    double showApproximatePointSize=3;
    Color showModelShapeColor = Color.BLACK;
    Color showNodeColor = Color.BLUE;
    Color showNodeNeigborNetColor=Color.GRAY;
    Color showApproximateColor=Color.BLACK;
    ModelPanelManager.ViewMarkerType showNodeMarkerType = ModelPanelManager.ViewMarkerType.Rectangle;
        ModelPanelManager.ViewMarkerType  showApproximatePointType=ModelPanelManager.ViewMarkerType.Rectangle;
    ReentrantLock showLock = new ReentrantLock();

    @Override
    public boolean isRubberAutoClear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void selecting(int x1, int y1, int x2, int y2, ModelPanelManager vt, BufferedImage rubberImage) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean selected(int x1, int y1, int x2, int y2, ModelPanelManager vt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearRubber(BufferedImage rubberImage, ModelPanelManager aThis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
