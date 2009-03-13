/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
    LinkedList<ApproximatePoint> aprxPts = new LinkedList<ApproximatePoint>();
    LinkedList<Point> holes = new LinkedList<Point>();
    ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    ArrayList<Node> nodes = new ArrayList<Node>();
    final AffineTransform itrx = new AffineTransform();
    private int compileTime = 0;

    public Point newPoint(double x, double y) {
        Point p = new Point(x, y);
        points.add(p);
        return p;
    }
    final double[] addShapeTds = new double[6];

    public void addHole(double x,double y){
        holes.add(Point.tempPoint(x, y));
    }

    public void addShape(Shape shape) {
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
                        rt.compile();
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
                    rt.compile();
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

    public void compile(double size, double flatness, String switches) {
        approximatePoint(size, flatness);
        triangleJni.setSwitchs(switches);
        triangleJni.setPointsSegments(aprxPts, points);
        double[] tds = new double[holes.size() * 2];
        int i = 0;
        for (Point p : holes) {
            tds[i * 2] = p.x;
            tds[i * 2+1] = p.y;
            i++;
        }
        triangleJni.setHoles(tds, holes.size());
        triangleJni.triangleFun();
        triangleJni.getNodesTriangles(nodes, triangles);
        compileTime++;
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
}
