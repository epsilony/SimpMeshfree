/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.model.ModelElement.ModelElementType;

/**
 *
 * @author epsilon
 */
public class BoundaryNode extends Node {

    public BoundaryNode(Point p) {
        super(p);
    }

    public BoundaryNode(double x, double y) {
        super(x, y);
    }
    
    Segment segment;

    public Segment getSegment() {
        return segment;
    }
    

    @Override
    public ModelElementType type() {
        return ModelElementType.BoundaryNode;
    }

}
