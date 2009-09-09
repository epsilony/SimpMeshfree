/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.simpmeshfree.utils.ModelPanelManager.ViewMarkerType;
import net.epsilony.util.collection.LayeredDomainTree;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class Model implements ModelImagePainter,Serializable {

    transient static Logger log = Logger.getLogger(Model.class);

    public LinkedList<Route> getRoutes() {
        return routes;
    }
    LinkedList<Route> routes = new LinkedList<Route>();
    LinkedList<ApproximatePoint> approximatePoints = new LinkedList<ApproximatePoint>();
    transient private int compileCounter = 0;
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
    transient double[] addShapeTemps = new double[6];

    public void addShape(Shape shape) {
        if (log.isDebugEnabled()) {
            log.debug("Start addShape()");
        }
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
                        rt.close();
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
                    rt.close();
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
        if (log.isDebugEnabled()) {
            log.debug("End of addShape()");
        }

    }

    public double getSegmentApproximateSize() {
        return segmentApproximateSize;
    }

    public double getSegmentFlatness() {
        return segmentFlatness;
    }


    public void generateApproximatePoints(double size, double flatness) {
        log.info(String.format("Start compile(%6.3f, %6.3f", size, flatness));
        segmentApproximateSize = size;
        segmentFlatness = flatness;
        log.info("Start Generate Approximate Points");
        approximatePoints.clear();
        for (Route sr : routes) {
            approximatePoints.addAll(sr.generateApproximatePoints(size, flatness));
        }
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
        log.info(String.format("End of Generate Approximate Points%nleftDown(%5.2f,%5.2f),rightUp(%5.2f,%5.2f)", leftDown.x, leftDown.y, rightUp.x, rightUp.y));

        approximatePointsSearchTree = new LayeredDomainTree<ApproximatePoint>(approximatePoints, Point.compX, Point.compY, wider);
        compileCounter++;
        log.info(String.format("End of complile, compileCounter=%d", compileCounter));
    }
    //用于加速搜索的临时变量
//    private final ApproximatePoint searchApproximatePointFrom = ApproximatePoint.tempApproximatePoint(0, 0),  searchApproximatePointTo = ApproximatePoint.tempApproximatePoint(0, 0);

//    /**
//     *
//     * @param <E>
//     * @param center
//     * @param size half length of the square edge
//     * @param outPts
//     * @return
//     */
//    public <E extends Point> List<E> pointDomainSearch(E center, double size, List<E> outPts) {
//        size = size > 0 ? size : -size;
//        double x1 = center.x - size,
//                x2 = center.x + size,
//                y1 = center.y - size,
//                y2 = center.y + size;
//        outPts.clear();
//        switch (center.type()) {
//            case ApproximatPoint:
//                searchApproximatePointFrom.setXY(x1, y1);
//                searchApproximatePointTo.setXY(x2, y2);
//                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, searchApproximatePointFrom, searchApproximatePointTo);
//                break;
//            default:
//                throw new UnsupportedOperationException();
//        }
//        return outPts;
//    }

//    /**
//     * search the Point link geometry elements in the domain that has corner1 and corner2
//     * corner1 and corner2 maybe changed  during the search!!!
//     * @param <E>
//     * @param corner1
//     * @param corner2
//     * @param outPts
//     * @return
//     */
//    public <E extends Point> List<E> pointDomainSearch(E corner1, E corner2, List<E> outPts) {
//        if (corner1.x > corner2.x) {
//            double t = corner2.x;
//            corner2.x = corner1.x;
//            corner1.x = t;
//        }
//        if (corner2.y > corner2.y) {
//            double t = corner2.y;
//            corner2.y = corner1.y;
//            corner1.y = t;
//        }
//        switch (corner1.type()) {
//            case ApproximatPoint:
//                approximatePointsSearchTree.domainSearch((List<ApproximatePoint>) outPts, (ApproximatePoint) corner1, (ApproximatePoint) corner2);
//                break;
//            default:
//                throw new UnsupportedOperationException();
//        }
//        return outPts;
//    }
//    private final LinkedList<ApproximatePoint> segmentSearchApproximatePointList = new LinkedList<ApproximatePoint>();
//    private final TreeSet<Segment> segmentSearchSet = new TreeSet<Segment>(ModelElement.indexComparator);
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
        LinkedList<ApproximatePoint> segmentSearchApproximatePointList = new LinkedList<ApproximatePoint>();
        TreeSet<Segment> segmentSearchSet = new TreeSet<Segment>(ModelElement.indexComparator);
        ApproximatePoint searchApproximatePointFrom = ApproximatePoint.tempApproximatePoint(0, 0), searchApproximatePointTo = ApproximatePoint.tempApproximatePoint(0, 0);

        searchApproximatePointFrom.setXY(x1 - segmentApproximateSize, y1 - segmentApproximateSize);
        searchApproximatePointTo.setXY(x2 + segmentApproximateSize, y2 + segmentApproximateSize);
        approximatePointsSearchTree.domainSearch(segmentSearchApproximatePointList, searchApproximatePointFrom, searchApproximatePointTo);
        segmentSearchSet.clear();
        if (log.isDebugEnabled()) {
            log.debug(String.format("from:%s to:%s", searchApproximatePointFrom, searchApproximatePointTo));
        }
        for (ApproximatePoint p : segmentSearchApproximatePointList) {
            segmentSearchSet.add(p.segment);
            if (p.segmentParm == 0) {
                segmentSearchSet.add(p.back.segment);
            }
//            if (p.x <= x2 && p.x >= x1 || p.y <= y2 && p.y >= y1) {
//                segmentSearchSet.add(p.segment);
//                continue;
//            }
//            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.back.x, p.back.y) ||
//                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.back.x, p.back.y) ||
//                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.back.x, p.back.y) ||
//                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.back.x, p.back.y)) {
//                segmentSearchSet.add(p.segment);
//                continue;
//            }
//            if (EYMath.isLineSegmentIntersect(x1, y1, x1, y2, p.x, p.y, p.front.x, p.front.y) ||
//                    EYMath.isLineSegmentIntersect(x2, y1, x2, y2, p.x, p.y, p.front.x, p.front.y) ||
//                    EYMath.isLineSegmentIntersect(x1, y1, x2, y1, p.x, p.y, p.front.x, p.front.y) ||
//                    EYMath.isLineSegmentIntersect(x1, y2, x2, y2, p.x, p.y, p.front.x, p.front.y)) {
//                segmentSearchSet.add(p.segment);
//            }
        }
        outSegs.addAll(segmentSearchSet);
        return outSegs;
    }
    transient private final TreeSet<Route> segmentRouteSearchSet = new TreeSet<Route>(ModelElement.indexComparator);

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
        LinkedList<ApproximatePoint> segmentSearchApproximatePointList = new LinkedList<ApproximatePoint>();
        ApproximatePoint searchApproximatePointFrom = ApproximatePoint.tempApproximatePoint(0, 0), searchApproximatePointTo = ApproximatePoint.tempApproximatePoint(0, 0);

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

    public static boolean canSeeEach(double x1, double y1, double x2, double y2, Collection<ApproximatePoint> aps) {
        for (ApproximatePoint ap : aps) {
            if (EYMath.isLineSegmentIntersect(x1, y1, x2, y2, ap.x, ap.y, ap.back.x, ap.back.y) || EYMath.isLineSegmentIntersect(x1, y1, x2, y2, ap.x, ap.y, ap.front.x, ap.front.y)) {
                return false;
            }
        }
        return true;
    }

    public List<ApproximatePoint> getDomainAffectApproximatePoint(List<ApproximatePoint> aps, double x, double y, double r) {
        approximatePointSearch(aps, x - r - segmentApproximateSize,
                y - r - segmentApproximateSize,
                x + r + segmentApproximateSize,
                y + r + segmentApproximateSize);
        return aps;
    }

    public LinkedList<Point> getHolesXYs() {
        log.info("Start getHolesXYs");
        LinkedList<Point> holesXYs = new LinkedList<Point>();
        for (Route route : routes) {
            if (!route.isCounterClockwise()) {
                Point holePoint = route.getHolePoint();
                if (log.isDebugEnabled()) {
                    log.debug("Hole:");
                    log.debug(route);
                    log.debug(holePoint);
                }
                holesXYs.add(holePoint);
            }
        }
        log.info("End of getHolesXYs");
        return holesXYs;
    }

    public LinkedList<ApproximatePoint> generateApproximatePoints(double size,double flatness,LinkedList<ApproximatePoint> outputAps){
        outputAps.clear();
         log.info("Start Generating Rough Approximate Points for output");
        for (Route sr : routes) {
            outputAps.addAll(sr.generateApproximatePoints(size, flatness,outputAps));
        }
        log.info("End of GenerateApproximatePoints for output, size=" + outputAps.size());
        return outputAps;
    }
    public List<ApproximatePoint> approximatePointSearch(List<ApproximatePoint> list, ApproximatePoint from, ApproximatePoint to) {
        ApproximatePoint tempApproximatePointSearchAp1 = ApproximatePoint.tempApproximatePoint(0, 0);
        ApproximatePoint tempApproximatePointSearchAp2 = ApproximatePoint.tempApproximatePoint(0, 0);
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
         ApproximatePoint tempApproximatePointSearchAp1 = ApproximatePoint.tempApproximatePoint(0, 0);
        ApproximatePoint tempApproximatePointSearchAp2 = ApproximatePoint.tempApproximatePoint(0, 0);
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

    public void addToPath(Path2D path) {
        for (Route route : routes) {
            route.addToPath(path);
        }
    }

    public static void main(String[] args) {
        Model gm = new Model();
        gm.addShape(new Rectangle2D.Double(0, 0, 48, 12));
    }
    transient boolean showModelShape = true;
    transient boolean showApproximatePoints = true;
    transient boolean showApproximateRoute = false;
    transient double approximatePointScreenSize = 3;
    transient Color modelShapeColor = Color.BLACK;
    transient Color approximatePointsColor = Color.BLACK;
    transient Color approximateRouteColor = Color.LIGHT_GRAY;

    public double getApproximatePointScreenSize() {
        return approximatePointScreenSize;
    }

    public void setApproximatePointScreenSize(double approximatePointScreenSize) {
        this.approximatePointScreenSize = approximatePointScreenSize;
    }

    public Color getApproximatePointsColor() {
        return approximatePointsColor;
    }

    public void setApproximatePointsColor(Color approximatePointsColor) {
        this.approximatePointsColor = approximatePointsColor;
    }

    public Color getApproximateRouteColor() {
        return approximateRouteColor;
    }

    public void setApproximateRouteColor(Color approximateRouteColor) {
        this.approximateRouteColor = approximateRouteColor;
    }

    public Color getModelShapeColor() {
        return modelShapeColor;
    }

    public void setModelShapeColor(Color modelShapeColor) {
        this.modelShapeColor = modelShapeColor;
    }

    public boolean isShowApproximatePoints() {
        return showApproximatePoints;
    }

    public void setShowApproximatePoints(boolean showApproximatePoints) {
        this.showApproximatePoints = showApproximatePoints;
    }

    public boolean isShowApproximateRoute() {
        return showApproximateRoute;
    }

    public void setShowApproximateRoute(boolean showApproximateRoute) {
        this.showApproximateRoute = showApproximateRoute;
    }

    public boolean isShowModelShape() {
        return showModelShape;
    }

    public void setShowModelShape(boolean showModelShape) {
        this.showModelShape = showModelShape;
    }
    transient ModelPanelManager.ViewMarkerType approximatePointScreenType = ModelPanelManager.ViewMarkerType.Rectangle;

    public ViewMarkerType getApproximatePointScreenType() {
        return approximatePointScreenType;
    }

    public void setApproximatePointScreenType(ViewMarkerType approximatePointScreenType) {
        this.approximatePointScreenType = approximatePointScreenType;
    }

    public void addApproximateRouteToPath(Path2D path) {
        for (Route route : routes) {
            route.addApproximateRouteToPath(path);
        }
    }

    @Override
    public void paintModel(BufferedImage modelImage, ModelPanelManager manager) {
        Graphics2D g2 = modelImage.createGraphics();


//        g2.setComposite(AlphaComposite.Clear);
//        g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());

        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();


        if (showApproximatePoints) {
            path.append(manager.viewMarker(approximatePoints, approximatePointScreenSize, approximatePointScreenType), false);
            g2.setColor(approximatePointsColor);
            g2.draw(path.createTransformedShape(null));
        }

        path.reset();
        if (showApproximateRoute) {
            addApproximateRouteToPath(path);
            g2.setColor(approximateRouteColor);
            g2.draw(path.createTransformedShape(tx));
        }

        path.reset();
        if (showModelShape) {
            addToPath(path);
            g2.setColor(modelShapeColor);
            g2.draw(path.createTransformedShape(tx));
        }

    }
}
