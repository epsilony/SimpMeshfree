/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class NodeBoundary extends Node {
    protected NodeBoundary() {
    }

    public NodeBoundary(double x, double y) {

        initNode(x, y);
        this.type=PrimitiveType.NodeBoundary;
    }


    Primitive related;

    public Primitive getRelated() {
        return related;
    }

    public void setRelated(Primitive related) {
        this.related = related;
    }
}
