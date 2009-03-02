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
    
    ModelElement related;

    public ModelElement getRelated() {
        return related;
    }

    public void setRelated(ModelElement related) {
        this.related = related;
    }

    @Override
    public ModelElementType getType() {
        return ModelElementType.BoundaryNode;
    }

}
