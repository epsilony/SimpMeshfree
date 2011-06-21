/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

/**
 *
 * @author epsilon
 */
public class BoundaryNode extends Node {
    public BoundaryNode(ApproximatePoint approximatePoint) {
        super(approximatePoint);
        segmentParm=approximatePoint.segmentParm;
        segment=approximatePoint.segment;
    }

    public BoundaryNode(double x,double y){
        super(x,y);
        this.x=x;
        this.y=y;
    }
    
//    ApproximatePoint approximatePoint;
    public double segmentParm;
    public Segment segment;

    public Segment getSegment() {
        return segment;
    }

    public double getSegmentParm() {
        return segmentParm;
    }
}
