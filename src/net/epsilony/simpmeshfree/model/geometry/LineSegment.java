/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.util.LinkedList;
import static net.epsilony.math.util.EYMath.*;

/**
 *
 * @author epsilon
 */
public class LineSegment extends Segment {

    public double lengthSqr(){
        return (pts[0].x-pts[1].x)*(pts[0].x-pts[1].x)+(pts[0].y-pts[1].y)*(pts[0].y+pts[1].y);
    }

    private static double [] results=new double[2];
    private static double [] results2=new double[2];
    public double lengthSqr(double startParm,double endParm){
        parameterPoint(startParm,results);
        parameterPoint(endParm,results2);
        return (results[0]-results2[0])*(results[0]-results2[0])+(results[1]-results2[1])*(results[1]-results2[1]);
    }

    public LineSegment(Point v1, Point v2) {
        pts=new Point[2];
        pts[0]=v1;
        pts[1]=v2;
    }

    @Override
    public void addToPath(Path2D path) {
        path.lineTo(pts[1].x, pts[1].y);
    }


    @Override
    public boolean isIntersectByLine(Point v1, Point v2) {
        return isLineSegmentIntersect(pts[0].x, pts[0].y, pts[1].x, pts[1].y, v1.x, v1.y, v2.x, v2.y);
    }

    @Override
    public double[] parameterPoint(double t, double[] pt) {
        pt[0]=pts[0].x*(1-t)+pts[1].x*t;
        pt[1]=pts[0].y*(1-t)+pts[1].y*t;
        return pt;
    }

    @Override
    public Point getFirstVertex() {
        return pts[0];
    }

    @Override
    public Point getLastVertex() {
        return pts[1];
    }


    @Override
    public LinkedList<ApproximatePoint> GenerateApproximatePoints(double size,double flatness,LinkedList<ApproximatePoint> aprxPts) {
        double sizeSqr=size*size;
        aprxPts.add(new ApproximatePoint(pts[0].x,pts[0].y,this,0));
        lineSegmentApproximatPoints(sizeSqr, 0, 1, aprxPts);
//        aprxPts.add(new ApproximatePoint(pts[1].x,pts[1].y,this,1));
        return aprxPts;
    }

    private void lineSegmentApproximatPoints(double sizeSqr,double startParm,double endParm,LinkedList<ApproximatePoint> aprxPts){
        if(sizeSqr<lengthSqr(startParm,endParm)){
            double t=(startParm+endParm)/2;
            lineSegmentApproximatPoints(sizeSqr,startParm,t,aprxPts);
            parameterPoint(t,results);
            aprxPts.add(new ApproximatePoint(results[0], results[1], this, t));
            lineSegmentApproximatPoints(sizeSqr,t, endParm, aprxPts);
        }
    }

    @Override
    public void setFirstVertex(Point v) {
        pts[0]=v;
    }

    @Override
    public void setLastVertex(Point v) {
        pts[1]=v;
    }

    @Override
    public double[] parameterDifference(double t, double[] pt) {
        pt[0]=pts[1].x-pts[0].x;
        pt[1]=pts[1].y-pts[0].y;
        return pt;
    }
}