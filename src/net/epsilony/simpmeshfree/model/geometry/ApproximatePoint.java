/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

/**
 *
 * @author epsilon
 */
public class ApproximatePoint extends Point {

    ApproximatePoint rear, front;
    Segment segment;
    double segmentParm;

    public ApproximatePoint(double x, double y, Segment attachedSegment, double segmentParm) {
        super(x,y);
        this.segment = attachedSegment;
        this.segmentParm = segmentParm;
    }

    ApproximatePoint(double x, double y) {
        super(x,y);
    }

    public ApproximatePoint getRear() {
        return rear;
    }

    public double getSegmentParm() {
        return segmentParm;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setRear(ApproximatePoint l) {
        this.rear = l;
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
        sb.append(String.format("%s %d:(%.2f, %.2f)-%s t=%.4f", ApproximatePoint.class.getSimpleName(), index, x, y, segment, segmentParm));
        if (null == front) {
            sb.append("front null");
        } else {
            sb.append("front:");
            sb.append(front.segmentParm);
        }
        if (null== rear) {
            sb.append("back null");
        } else {
            sb.append("back:");
            sb.append(rear.segmentParm);
        }
        return sb.toString();
    }
}
