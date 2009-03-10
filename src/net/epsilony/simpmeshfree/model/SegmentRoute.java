/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author epsilon
 */
public class SegmentRoute extends LinkedList<Segment> {

    private LinkedList<ApproximatePoint> aprxPts;
    private boolean changed;

    @Override
    public boolean add(Segment e) {
        changed=true;
        return super.add(e);
    }


    @Override
    public void addFirst(Segment e) {
        changed=true;
        super.addFirst(e);
    }

    @Override
    public void addLast(Segment e) {
        changed=true;
        super.addLast(e);
    }

    @Override
    public void clear() {
        changed=true;
        super.clear();
    }

    /*public class SegmentRouteBoundsNode{
    double xmin,ymin,xmax,ymax;
    LineSegment bound;
    SegmentRouteBoundsNode[] routeBoundsNode;
    
    public SegmentRouteBoundsNode(double xmin, double ymin, double xmax, double ymax) {
    this.xmin = xmin;
    this.ymin = ymin;
    this.xmax = xmax;
    this.ymax = ymax;
    }
    
    }*/

//    LinkedList<LineSegment> routeBounds=new LinkedList<LineSegment>();
    public void compile() {
        Point v;
        Segment l, r;
        l = null;
        r = null;
        for (Segment s : this) {
            r = s;
            v = new Vertex(r.getLeftVertex(), l, r);
            r.setLeftVertex(v);
            l = r;
        }
        v = this.getFirst().getLeftVertex();
        ((Vertex) v).l = r;
        r.setRightVertex(v);
    }

    public LinkedList<Vertex> getVertex() {
        LinkedList<Vertex> vs = new LinkedList<Vertex>();
        for (Segment s : this) {
            vs.add((Vertex) s.getLeftVertex());
        }
        return vs;
    }

    double formSize,formFlatness;
    public LinkedList<ApproximatePoint> approximatePoints(double size, double flatness) {
        if (null == aprxPts || changed||formSize!=size||formFlatness!=flatness) {
            aprxPts = new LinkedList<ApproximatePoint>();
            for (Segment s : this) {
                s.approximatePoints(size, flatness, aprxPts);
            }
            changed=false;
            formSize=size;
            formFlatness=flatness;
        }
        ApproximatePoint tAp=aprxPts.getLast();
        for(ApproximatePoint ap:aprxPts){
            ap.l=tAp;
            tAp.r=ap;
            tAp=ap;
        }
        return aprxPts;
    }
}
