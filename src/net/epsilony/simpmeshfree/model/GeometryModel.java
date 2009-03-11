/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

/**
 *
 * @author epsilon
 */
public class GeometryModel {

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
    AffineTransform itrx = new AffineTransform();

    public Point newPoint(double x, double y) {
        Point p = new Point(x, y);
        points.add(p);
        return p;
    }
    double[] tds = new double[6];

    public void addShape(Shape shape) {
        PathIterator pi = shape.getPathIterator(itrx);
        boolean hasMoveTo = false;
        boolean hasClose = false;
        Point lastEnd = null;
        Point routeHead = null;
        SegmentRoute rt = null;
        Point tp, tp1, tp2;
        while (!pi.isDone()) {
            switch (pi.currentSegment(tds)) {
                case PathIterator.SEG_MOVETO:
                    if (hasMoveTo && !hasClose) {
                        rt.add(new LineSegment(lastEnd, routeHead));
                        rt.compile();
                        routes.add(rt);
                    }
                    lastEnd = new Point(tds[0], tds[1]);
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
                    System.out.println(rt);
                    rt.compile();
                    routes.add(rt);
                    break;
                case PathIterator.SEG_LINETO:
                    tp = new Point(tds[0], tds[1]);

                    rt.add(new LineSegment(lastEnd, tp));
                    lastEnd = tp;
//                    System.out.println("line");
                    break;
                case PathIterator.SEG_QUADTO:
                    tp = new Point(tds[0], tds[1]);
                    tp1 = new Point(tds[2], tds[3]);
                    System.out.println("quad");
                    rt.add(CubicBezierSegment.fromQuadBezierPoints(lastEnd, tp, tp1));
                    lastEnd = tp1;
                    break;
                case PathIterator.SEG_CUBICTO:
                    tp = new Point(tds[0], tds[1]);
                    tp1 = new Point(tds[2], tds[3]);
                    tp2 = new Point(tds[4], tds[5]);
                    rt.add(new CubicBezierSegment(lastEnd, tp, tp1, tp2));
                    lastEnd = tp2;
                    break;
            }
            pi.next();
        }
        System.out.println("routes.size() = " + routes.size());

    }

    public LinkedList<ApproximatePoint> approximatePoint(double size, double flatness) {
        LinkedList<ApproximatePoint> aprxPts = new LinkedList<ApproximatePoint>();

        for (SegmentRoute sr : routes) {
            aprxPts.addAll(sr.approximatePoints(size, flatness));
        }
//        System.out.println("aprxPts.size() = " + aprxPts.size());
        return aprxPts;
    }

    public static void main(String[] args) {
        GeometryModel gm = new GeometryModel();
        gm.addShape(new Rectangle2D.Double(0, 0, 48, 12));
    }
}
