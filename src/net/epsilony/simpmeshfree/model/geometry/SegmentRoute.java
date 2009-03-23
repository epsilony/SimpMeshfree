/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.LinkedList;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class SegmentRoute extends ModelElement {

    static ModelElementIndexManager routeIm = new ModelElementIndexManager();
    LinkedList<Segment> segments = new LinkedList<Segment>();
    private LinkedList<ApproximatePoint> aprxPts;
    private boolean changed;

    public LinkedList<Segment> getSegments() {
        return segments;
    }

    public SegmentRoute() {
        index = routeIm.getNewIndex();
    }

    public Segment getLast() {
        return segments.getLast();
    }

    public Segment getFirst() {
        return segments.getFirst();
    }

    public void clear() {
        segments.clear();
        changed = true;
    }

    public boolean add(Segment e) {
        changed = true;
        e.route = this;
        return segments.add(e);
    }
    double formSize, formFlatness;

    public LinkedList<ApproximatePoint> approximatePoints(double size, double flatness) {
        if (null == aprxPts || changed || formSize != size || formFlatness != flatness) {
            aprxPts = new LinkedList<ApproximatePoint>();
            for (Segment s : segments) {
                s.approximatePoints(size, flatness, aprxPts);
            }          
            ApproximatePoint tAp = aprxPts.getLast();
            for (ApproximatePoint ap : aprxPts) {
                ap.l = tAp;
                tAp.r = ap;
                tAp = ap;
            }
            changed = false;
            formSize = size;
            formFlatness = flatness;
        }
        return aprxPts;
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.SegmentRoute;
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return routeIm;
    }
}
