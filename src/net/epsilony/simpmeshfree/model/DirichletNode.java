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
public class DirichletNode extends BoundaryNode{

    public DirichletNode(double x, double y) {
        super(x, y);
    }

    public DirichletNode(Point p) {
        super(p);
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.DirichletNode;
    }

    double ux,uy;
    public void setUxy(double ux,double uy){
        this.ux=ux;
        this.uy=uy;
    }

    public double getUx() {
        return ux;
    }

    public double getUy() {
        return uy;
    }
    
}
