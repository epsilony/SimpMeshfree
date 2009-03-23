/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;

/**
 *@deprecated 
 * @author epsilon
 */
public class QuadBezierSegment extends Segment{

    public QuadBezierSegment(Point v1, Point v2, Point v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    Point v1,v2,v3;
    @Override
    public void addToPath(Path2D path) {
        path.quadTo(v2.x, v2.y, v3.x, v3.y);
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.QuadBezierSegment;
    }

    @Override
    public boolean intersectLine(Point v1, Point v2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  
    @Override
    public double[] parameterPoint(double t, double[] pt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getLeftVertex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getRightVertex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLeftVertex(Point v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRightVertex(Point v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LinkedList<ApproximatePoint> approximatePoints(double size, double flatness,LinkedList<ApproximatePoint> aprxPts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
