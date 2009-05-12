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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.utils.ModelImageWriter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.util.collection.LayeredDomainTree;

/**
 *
 * @author epsilon
 */
public class GeometryModel implements ModelImageWriter{

    public LinkedList<Route> getRoutes() {
        return routes;
    }

    LinkedList<Route> routes = new LinkedList<Route>();

    LinkedList<ApproximatePoint> approximatePoints = new LinkedList<ApproximatePoint>();

    private int compileCounter = 0;

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


    double[] addShapeTemps = new double[6];


    public void addShape(Shape shape) {
//        this.modelShape = shape;
        PathIterator pi = shape.getPathIterator(null);
        boolean hasMoveTo = false;
        boolean hasClose = false;
        Point lastEnd = null;
        Point routeHead = null;
        Route rt = null;
        Point tp, tp1, tp2;
        while (!pi.isDone()) {
            switch (pi.currentSegment(addShapeTemps)) {
                case PathIterator.SEG_MOVETO:
                    if (hasMoveTo && !hasClose) {
                        rt.add(new LineSegment(lastEnd, routeHead));
//                        rt.compile();
                        routes.add(rt);
                    }
                    lastEnd = new Point(addShapeTemps[0], addShapeTemps[1]);
                    rt = new Route();
                    routeHead = lastEnd;
                    hasMoveTo = true;
                    hasClose = false;
                    break;
                case PathIterator.SEG_CLOSE:
                    if (lastEnd.x != routeHead.x || lastEnd.y != routeHead.y) {
                        rt.add(new LineSegment(lastEnd, routeHead));
//                        System.out.println("close");
                    } else {
                        rt.getLast().setLastVertex(rt.getFirst().getFirstVertex());
//                        System.out.println(rt.getLast().getLastVertex().x+" "+rt.getLast().getLastVertex().getY());
//                        System.out.println("close2");
                    }

                    hasClose = true;
//                    System.out.println(rt);
//                    rt.compile();
                    routes.add(rt);
                    break;
                case PathIterator.SEG_LINETO:
                    tp = new Point(addShapeTemps[0], addShapeTemps[1]);

                    rt.add(new LineSegment(lastEnd, tp));
                    lastEnd = tp;
//                    System.out.println("line");
                    break;
                case PathIterator.SEG_QUADTO:
                    tp = new Point(addShapeTemps[0], addShapeTemps[1]);
                    tp1 = new Point(addShapeTemps[2], addShapeTemps[3]);
//                    System.out.println("quad");
                    rt.add(CubicBezierSegment.fromQuadBezierPoints(lastEnd, tp, tp1));
                    lastEnd = tp1;
                    break;
                case PathIterator.SEG_CUBICTO:
                    tp = new Point(addShapeTemps[0], addShapeTemps[1]);
                    tp1 = new Point(addShapeTemps[2], addShapeTemps[3]);
                    tp2 = new Point(addShapeTemps[4], addShapeTemps[5]);
                    rt.add(new CubicBezierSegment(lastEnd, tp, tp1, tp2));
                    lastEnd = tp2;
                    break;
            }
            pi.next();
        }
//        System.out.println("routes.size() = " + routes.size());

    }


    public double getSegmentApproximateSize() {
        return segmentApproximateSize;
    }

    public double getSegmentFlatness() {
        return segmentFlatness;
    }

    public void compile(double size, double flatness) {
        segmentApproximateSize = size;
        segmentFlatness = flatness;
        GenerateApproximatePoints(size, flatness);
        leftDown.setXY(approximatePoints.getFirst());
        rightUp.setXY(approximatePoints.getFirst());
        for (ApproximatePoint ap : approximatePoints) {
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
//        triangleJni.setSwitchs(switches);
//        triangleJni.setPointsSegments(GenerateApproximatePoints, points);
//        double[] tds = new double[holes.size() * 2];
//        int i = 0;
//        for (Point p : holes) {
//            tds[i * 2] = p.x;
//            tds[i * 2 + 1] = p.y;
//            i++;
//        }
//        triangleJni.setHoles(tds, holes.size());
//        triangleJni.triangleFun();
//        triangleJni.getNodesTriangles(nodes, triangles);
//        System.out.println("nodes.size() = " + nodes.size());
//        nodesSearchTree = new LayeredDomainTree<Node>(nodes, Point.compX, Point.compY, wider);
        approximatePointsSearchTree = new LayeredDomainTree<ApproximatePoint>(approximatePoints, Point.compX, Point.compY, wider);
        compileCounter++;
    }
    //用于加速搜索的临时变量
    private final ApproximatePoint searchApproximatePointFrom = ApproximatePoint.tempApproximatePoint(0, 0),  searchApproximatePointTo = ApproximatePoint.tempApproximatePoint(0, 0);

//    public List<Node> nodeDomainSearch(double x1, double y1, double x2, double y2, List<Node> outPts) {
//        double t;
//        if (x1 > x2) {
//            t = x1;
//            x1 = x2;
//            x2 = t;
//        }
//        if (y1 > y2) {
//            t = y1;
//            y1 = y2;
//            y2 = t;
//        }
//        searchNodeFrom.setXY(x1, y1);
//        searchNodeTo.setXY(x2, y2);
//        outPts.clear();
//        return nodesSearchTree.domainSearch(outPts, searchNodeFrom, searchNodeTo);
//    }

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
            case ApproximatPoint:
                searchApproximatePointFrom.setXY(x1, y1);
                searchApproximatePointTo.setXY(x2, y2);
                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, searchApproximatePointFrom, searchApproximatePointTo);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return outPts;
    }

    /**
     * search the Point link geometry elements in the domain that has corner1 and corner2
     * corner1 and corner2 maybe changed  during the search!!!
     * @param <E>
     * @param corner1
     * @param corner2
     * @param outPts
     * @return
     */
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
            case ApproximatPoint:
                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, (ApproximatePoint) corner1, (ApproximatePoint) corner2);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return outPts;
    }
    private final LinkedList<ApproximatePoint> segmentSearchApproximatePointList = new LinkedList<ApproximatePoint>();
    private final TreeSet<Segment> segmentSearchSet = new TreeSet<Segment>(ModelElement.indexComparator);

    /**
     * 搜索与直线(x1,y1)-(x2,y2)有可能相交的LineSegment
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param outSegs
     * @return
     */
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
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.back.x, p.back.y)) {
                segmentSearchSet.add(p.segment);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.front.x, p.front.y)) {
                segmentSearchSet.add(p.segment);
            }
        }
        outSegs.addAll(segmentSearchSet);
        return outSegs;
    }
    private final TreeSet<Route> segmentRouteSearchSet = new TreeSet<Route>(ModelElement.indexComparator);

    public List<Route> segmentRouteSearch(double x1, double y1, double x2, double y2, List<Route> outSegs) {
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
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.back.x, p.back.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.back.x, p.back.y)) {
                segmentRouteSearchSet.add(p.segment.route);
                continue;
            }
            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.front.x, p.front.y) ||
                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.front.x, p.front.y)) {
                segmentRouteSearchSet.add(p.segment.route);
            }
        }
        outSegs.addAll(segmentRouteSearchSet);
        return outSegs;
    }


    public static boolean canSeeEach(Point e1, Point e2, Collection<ApproximatePoint> aps) {
        for (ApproximatePoint ap : aps) {
            if (EYMath.isLineSegmentIntersect(e1.x, e1.y, e2.x, e2.y, ap.x, ap.y, ap.back.x, ap.back.y) || EYMath.isLineSegmentIntersect(e1.x, e1.y, e2.x, e2.y, ap.x, ap.y, ap.front.x, ap.front.y)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canSeeEach(double x1,double y1,double x2,double y2, Collection<ApproximatePoint> aps) {
        for (ApproximatePoint ap : aps) {
            if (EYMath.isLineSegmentIntersect(x1, y1, x2, y2, ap.x, ap.y, ap.back.x, ap.back.y) || EYMath.isLineSegmentIntersect(x1, y1, x2, y2, ap.x, ap.y, ap.front.x, ap.front.y)) {
                return false;
            }
        }
        return true;
    }

    public List<ApproximatePoint> getDomainAffectApproximatePoint(List<ApproximatePoint> aps,double x,double y,double r){
        approximatePointSearch(aps,x-r-segmentApproximateSize,
                y-r-segmentApproximateSize,
                x+r+segmentApproximateSize,
                y+r+segmentApproximateSize);
        return aps;
    }

    public LinkedList<Point> getHolesXYs(){
        LinkedList<Point> holesXYs=new LinkedList<Point>();
        for(Route route:routes){
            if(!route.isCounterClockwise()){
                Point holePoint = route.getHolePoint();
                holesXYs.add(holePoint);
            }
        }
        return holesXYs;
    }

    /**
     *
     * @param size
     * @param flatness
     * @return
     */
    private LinkedList<ApproximatePoint> GenerateApproximatePoints(double size, double flatness) {
        for (Route sr : routes) {
//            System.out.println("sr.type"+sr.type());
            approximatePoints.addAll(sr.GenerateApproximatePoints(size, flatness));
        }
//        System.out.println("GenerateApproximatePoints.size() = " + GenerateApproximatePoints.size());
        return approximatePoints;
    }
    private final ApproximatePoint tempApproximatePointSearchAp1 = ApproximatePoint.tempApproximatePoint(0, 0);
    private final ApproximatePoint tempApproximatePointSearchAp2 = ApproximatePoint.tempApproximatePoint(0, 0);

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

    public LinkedList<ApproximatePoint> getApproximatePoints() {
        return approximatePoints;
    }

    public void addToPath(Path2D path){
        for(Route route:routes){
            route.addToPath(path);
        }
    }


    public static void main(String[] args) {
        GeometryModel gm = new GeometryModel();
        gm.addShape(new Rectangle2D.Double(0, 0, 48, 12));
    }


    @Override
    public void writeModelBuffer(BufferedImage modelImage, ModelPanelManager manager) {
        Graphics2D g2 = modelImage.createGraphics();


        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());

        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();


        if (showApproximatePoint) {
            path.append(manager.viewMarker(approximatePoints, showApproximatePointSize, showApproximatePointType), false);
        }
        g2.setColor(showApproximateColor);
        g2.draw(path.createTransformedShape(null));
        path.reset();
    }
    boolean showModelShape = true;

    boolean showApproximatePoint = true;
    boolean showApproximatePointRoute = false;

    double showApproximatePointSize = 3;
    Color showModelShapeColor = Color.BLACK;

    Color showNodeNeigborNetColor = Color.GRAY;
    Color showApproximateColor = Color.BLACK;
    ModelPanelManager.ViewMarkerType showNodeMarkerType = ModelPanelManager.ViewMarkerType.Rectangle;
    ModelPanelManager.ViewMarkerType showApproximatePointType = ModelPanelManager.ViewMarkerType.Rectangle;

}
