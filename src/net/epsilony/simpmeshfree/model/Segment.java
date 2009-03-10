/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

import java.awt.geom.Path2D;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
abstract public class Segment extends ModelElement{

    static ModelElementIndexManager segmentIM=new ModelElementIndexManager();
    LinkedList<Node> attachedNodes=new LinkedList<Node>();
    @Override
    public ModelElementIndexManager getIndexManager() {
        return segmentIM;
    }
    protected Segment(){
        index=segmentIM.getNewIndex();
    }
    public Point [] pts;
    abstract public void addToPath(Path2D path);
    abstract public boolean intersectLine(Point v1,Point v2);
    abstract public LinkedList<ApproximatePoint> approximatePoints(double size,double flatness,LinkedList<ApproximatePoint> aprxPts);
    SegmentRoute route=null;

    public SegmentRoute getRoute() {
        return route;
    }

    public void setRoute(SegmentRoute route) {
        this.route = route;
    }
    protected Segment(boolean temp){
        if(!temp){
            index=segmentIM.getNewIndex();
        }
    }

    abstract public Point getLeftVertex();
    abstract public Point getRightVertex();
    abstract public void setLeftVertex(Point v);
    abstract public void setRightVertex(Point v);

    abstract public double[] getPoint(double t,double[] pt);

    @Override
    public String toString() {

        StringBuffer sb=new StringBuffer();
        sb.append(type());
        sb.append(":");
        for(int i=0;i<pts.length;i++){
            sb.append(String.format("(%f.1, %f.1) ", pts[i].x,pts[i].y));
        }
        return sb.toString();
    }


}
