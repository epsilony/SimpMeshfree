/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.model.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class ApproximatePoint extends Point{
    static ModelElementIndexManager aPointIm = new ModelElementIndexManager();

    @Override
    public ModelElementIndexManager getIndexManager() {
        return aPointIm;
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.ApproximatPoint;
    }

    ApproximatePoint l,r;
    Segment attachedSegment;
    double segmentParm;

    public ApproximatePoint(double x,double y,Segment attachedSegment,double segmentParm) {
        this.x=x;
        this.y=y;
        index=aPointIm.getNewIndex();
        this.attachedSegment = attachedSegment;
        this.segmentParm=segmentParm;
    }

    public ApproximatePoint getL() {
        return l;
    }

    public void setL(ApproximatePoint l) {
        this.l = l;
    }

    public ApproximatePoint getR() {
        return r;
    }

    public void setR(ApproximatePoint r) {
        this.r = r;
    }

    @Override
    public String toString() {
        return String.format("%s%d:(%f.1, %f.1)-%s%d l:%d r:%d", type(),index,x,y,attachedSegment.type(),attachedSegment.index,l.index,r.index);
    }



}
