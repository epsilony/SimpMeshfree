/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class NodeDirichlet extends NodeBoundary{
    public NodeDirichlet(double x,double y){
        initNode(x,y);
        this.type=PrimitiveType.NodeDirichlet;
    }

    public NodeDirichlet(Node x){
        copyNode(x);
        this.type=PrimitiveType.NodeDirichlet;
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
