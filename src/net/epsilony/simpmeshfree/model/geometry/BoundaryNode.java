/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;


import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;

/**
 *
 * @author epsilon
 */
public class BoundaryNode extends Node {
    public BoundaryNode(ApproximatePoint approximatePoint) {
        super(approximatePoint);
//        this.approximatePoint = approximatePoint;
        segmentParm=approximatePoint.segmentParm;
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
