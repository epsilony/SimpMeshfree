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
public class NodeNeumann extends BoundaryNode {

    public NodeNeumann(double x, double y) {
        super(x, y);
    }

    public NodeNeumann(Point p) {
        super(p);
    }

    double tx, ty;

    @Override
    public ModelElementType getType() {
        return ModelElementType.NeumannNode;
    }

    public void setTx(double tx) {
        this.tx = tx;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public void setTxy(double tx, double ty) {
        this.tx = tx;
        this.ty = ty;
    }

    public double getTx() {
        return tx;
    }

    public double getTy() {
        return ty;
    }
}
