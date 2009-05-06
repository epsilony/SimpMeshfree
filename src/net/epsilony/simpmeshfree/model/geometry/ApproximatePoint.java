/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.geometry;

import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class ApproximatePoint extends Point{
    public static ApproximatePoint tempApproximatePoint(double x,double y){
        ApproximatePoint ap=new ApproximatePoint();
        ap.x=x;
        ap.y=y;
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

    ApproximatePoint back,front;
    Segment segment;
    double segmentParm;

    public ApproximatePoint(double x,double y,Segment attachedSegment,double segmentParm) {
        this.x=x;
        this.y=y;
        index=aPointIm.getNewIndex();
        this.segment = attachedSegment;
        this.segmentParm=segmentParm;
    }

    public ApproximatePoint getL() {
        return back;
    }

    public void setL(ApproximatePoint l) {
        this.back = l;
    }

    public ApproximatePoint getR() {
        return front;
    }

    public void setR(ApproximatePoint r) {
        this.front = r;
    }

    @Override
    public String toString() {
        return String.format("%s%d:(%f.1, %f.1)-%s%d l:%d r:%d", type(),index,x,y,segment.type(),segment.index,back.index,front.index);
    }



}
