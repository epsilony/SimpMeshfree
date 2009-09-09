/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;


import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;

/**
 *
 * @author epsilon
 */
public class BoundaryNode extends Node {
    public BoundaryNode(ApproximatePoint approximatePoint) {
        super(approximatePoint);
//        this.approximatePoint = approximatePoint;
        segmentParm=approximatePoint.segmentParm;
        segment=approximatePoint.segment;
    }

    public BoundaryNode(double x,double y){
        this.x=x;
        this.y=y;
    }
    
//    ApproximatePoint approximatePoint;
    double segmentParm;
    Segment segment;

    public Segment getSegment() {
        return segment;
    }

    public double getSegmentParm() {
        return segmentParm;
    }
    

    @Override
    public ModelElementType type() {
        return ModelElementType.BoundaryNode;
    }

}
