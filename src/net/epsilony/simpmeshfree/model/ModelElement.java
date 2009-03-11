/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.LinkedList;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public abstract class ModelElement {

    public enum ModelElementType {

        Point, LineSegment, Node, NeumannNode, DirichletNode, RegularizeNode, Triangle, BoundaryNode, QuadBezierSegment, CubicBezierSegment, Vertex, ApproximatPoint, SegmentRoute;
    }

    abstract public ModelElementType type();
    ModelElement attaching=null;

    abstract public ModelElementIndexManager getIndexManager();
    int index;
    LinkedList<ModelElement> attached = null;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    protected ModelElement() {
    }

}
