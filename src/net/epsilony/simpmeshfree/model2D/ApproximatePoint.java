/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;

import java.io.Serializable;
import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class ApproximatePoint extends Point implements Serializable {

    public static ApproximatePoint tempApproximatePoint(double x, double y) {
        ApproximatePoint ap = new ApproximatePoint();
        ap.x = x;
        ap.y = y;
        return ap;
    }
    static ModelElementIndexManager aPointIm = new ModelElementIndexManager();

    protected ApproximatePoint() {
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return aPointIm;
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.ApproximatPoint;
    }
    ApproximatePoint back, front;
    Segment segment;
    double segmentParm;

    public ApproximatePoint(double x, double y, Segment attachedSegment, double segmentParm) {
        this.x = x;
        this.y = y;
        index = aPointIm.getNewIndex();
        this.segment = attachedSegment;
        this.segmentParm = segmentParm;
    }

    public ApproximatePoint getBack() {
        return back;
    }

    public double getSegmentParm() {
        return segmentParm;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setBack(ApproximatePoint l) {
        this.back = l;
    }

    public ApproximatePoint getFront() {
        return front;
    }

    public void setFront(ApproximatePoint r) {
        this.front = r;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(String.format("%s %d:(%.2f, %.2f)-%s t=%.4f", type(), index, x, y, segment, segmentParm));
        if (null == front) {
            sb.append("front null");
        } else {
            sb.append("front:");
            sb.append(front.segmentParm);
        }
        if (null== back) {
            sb.append("back null");
        } else {
            sb.append("back:");
            sb.append(back.segmentParm);
        }
        return sb.toString();
    }
}
