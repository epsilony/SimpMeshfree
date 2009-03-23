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
    public enum BoundaryConditionType{
        Dirichlet,Neumann;
    }

    public class NodeBoundaryCondition{
        BoundaryConditionType type;
        double u,v;


        public BoundaryConditionType getType() {
            return type;
        }

        public double getU() {
            return u;
        }

        public double getV() {
            return v;
        }

        public NodeBoundaryCondition(double utx,double vty,BoundaryConditionType type){
            this.u = utx;
            this.v = vty;
            this.type = type;

        }

    }

    NodeBoundaryCondition boundaryCondition;

    public NodeBoundaryCondition getBoundaryCondition() {
        return boundaryCondition;
    }

    public BoundaryNode(ApproximatePoint approximatePoint) {
        super(approximatePoint);
        this.approximatePoint = approximatePoint;
        segmentParm=approximatePoint.segmentParm;
    }
    
    ApproximatePoint approximatePoint;
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
