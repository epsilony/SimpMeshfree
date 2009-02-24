/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class NodeNeumann extends NodeBoundary{
 double tx, ty;

    public NodeNeumann(Node n) {
        copyNode(n);
        setType(PrimitiveType.NodeBoundary);
    }

    public NodeNeumann(double x, double y) {
        initNode(x,y);
        this.type=PrimitiveType.NodeNeumann;

    }
    public void setTx(double tx) {
        this.tx = tx;
    }

    public void setTy(double ty) {
        this.ty = ty;
    }

    public void setTxy(double tx,double ty){
        this.tx=tx;
        this.ty=ty;
    }

    public double getTx() {
        return tx;
    }

    public double getTy() {
        return ty;
    }
}
