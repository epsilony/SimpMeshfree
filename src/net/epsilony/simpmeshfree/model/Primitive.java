/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class Primitive {

    public enum PrimitiveType {
        PointNormal,PointVertex, SegmentLine, NodeNormal,NodeBoundary, NodeNeumann, NodeDirichlet, NodeRegularize;
    }

    PrimitiveType type;

    public PrimitiveType getType() {
        return type;
    }

    protected Primitive(){

    }

    protected void setType(PrimitiveType type) {
        this.type = type;
    }

}
