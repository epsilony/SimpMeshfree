/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.util.LinkedList;
import net.epsilony.math.util.EYMath;
import static net.epsilony.math.util.EYMath.*;

/**
 *
 * @author epsilon
 */
public class CubicBezierSegment extends Segment {

    public static CubicBezierSegment fromQuadBezierPoints(Point v1, Point v2, Point v3) {
        Point v4 = new Point(v3.x, v3.y);
        v3.x = v2.x * 2 / 3 + v3.x / 3;
        v3.y = v2.y * 2 / 3 + v3.y / 3;
        v2.x = v2.x * 2 / 3 + v1.x / 3;
        v2.y = v2.y * 2 / 3 + v1.y / 3;
        return new CubicBezierSegment(v1, v2, v3, v4);
    }

    public CubicBezierSegment(Point v1, Point v2, Point v3, Point v4) {
        pts = new Point[4];
        this.pts[0] = v1;
        this.pts[1] = v2;
        this.pts[2] = v3;
        this.pts[3] = v4;
    }

    @Override
    public void addToPath(Path2D path) {
        path.curveTo(pts[1].x, pts[1].y, pts[2].x, pts[2].y, pts[3].x, pts[3].y);
    }

    @Override
    public boolean isIntersectByLine(Point v1, Point v2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] parameterPoint(double t, double[] pt) {
        return cubicBezierPoint(t, pts[0].x, pts[0].y, pts[1].x, pts[1].y, pts[2].x, pts[2].y, pts[3].x, pts[3].y, pt);
    }

    @Override
    public Point getFirstVertex() {
        return pts[0];
    }

    @Override
    public Point getLastVertex() {
        return pts[3];
    }

    @Override
    public void setFirstVertex(Point v) {
        pts[0] = v;
    }

    @Override
    public void setLastVertex(Point v) {
        pts[3] = v;
    }

    @Override
    public LinkedList<ApproximatePoint> GenerateApproximatePoints(double size, double flatness, LinkedList<ApproximatePoint> aprxPts) {
        aprxPts.add(new ApproximatePoint(pts[0].x, pts[0].y, this, 0));
        double [] ctrlPts=new double[]{pts[0].x,pts[0].y,pts[1].x,pts[1].y,pts[2].x,pts[2].y,pts[3].x,pts[3].y};
        cubicBezierApproximatePoints(size*size, flatness*flatness, 0, 1, ctrlPts, aprxPts);
//        aprxPts.add(new ApproximatePoint(pts[3].x, pts[3].y, this, 1));
        return aprxPts;
    }

    public static double results[]=new double[2];
    private void cubicBezierApproximatePoints(double sizeSqr, double flatnessSqr, double startParm, double endParm, double[] ctrlPts, LinkedList<ApproximatePoint> aprxPts) {
        if (EYMath.cubicBezierCurveFlatnessSqr(ctrlPts) > flatnessSqr ||
                (ctrlPts[0] - ctrlPts[6]) * (ctrlPts[0] - ctrlPts[6]) + (ctrlPts[1] - ctrlPts[7]) * (ctrlPts[1] - ctrlPts[7]) > sizeSqr) {

            double[] ctrlPtsR = EYMath.halfDivideCubicBezierCurve(ctrlPts, new double[8]);
            double t=(startParm+endParm)/2;

            cubicBezierApproximatePoints(sizeSqr, flatnessSqr, startParm, t, ctrlPts, aprxPts);
            parameterPoint(t, results);
            aprxPts.add(new ApproximatePoint(results[0], results[1], this, t));
            cubicBezierApproximatePoints(sizeSqr, flatnessSqr, t, endParm, ctrlPtsR, aprxPts);
        }
    }

    @Override
    public double[] parameterDifference(double t, double[] pt) {
        double p01,p12,p23,p02,p13,p02d,p13d;
        p01=pts[1].x*(1-t)+t*(pts[0].x);
        p12=pts[2].x*(1-t)+t*(pts[1].x);
        p23=pts[3].x*(1-t)+t*(pts[2].x);
        p02=(1-t)*p01+t*p12;
        p13=(1-t)*p12+t*p23;
        p02d=(1-t)*(pts[1].x-pts[0].x)-p01+p12+t*(pts[2].x-pts[1].x);
        p13d=(1-t)*(pts[2].x-pts[1].x)-p12+p23+t*(pts[3].x-pts[2].x);
        pt[0]=(1-t)*p02d-p02+p13+t*p13d;

        p01=pts[1].y*(1-t)+t*(pts[0].y);
        p12=pts[2].y*(1-t)+t*(pts[1].y);
        p23=pts[3].y*(1-t)+t*(pts[2].y);
        p02=(1-t)*p01+t*p12;
        p13=(1-t)*p12+t*p23;
        p02d=(1-t)*(pts[1].y-pts[0].y)-p01+p12+t*(pts[2].y-pts[1].y);
        p13d=(1-t)*(pts[2].y-pts[1].y)-p12+p23+t*(pts[3].y-pts[2].y);
        pt[1]=(1-t)*p02d-p02+p13+t*p13d;
        return pt;
    }

}
