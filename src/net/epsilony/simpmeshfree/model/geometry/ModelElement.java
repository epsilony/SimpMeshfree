/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.Comparator;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public abstract class ModelElement {

    public enum ModelElementType {

        Point, LineSegment, Node,RegularizeNode, Triangle, BoundaryNode, QuadBezierSegment, CubicBezierSegment, Vertex, ApproximatPoint, SegmentRoute;
    }

    public static final Comparator<ModelElement> comparator=new Comparator<ModelElement>() {

        @Override
        public int compare(ModelElement o1, ModelElement o2) {
            return o1.index-o2.index;
        }
    };
    abstract public ModelElementType type();
    ModelElement attaching=null;

    abstract public ModelElementIndexManager getIndexManager();
    protected int index;
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
