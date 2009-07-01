/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
abstract public class Segment extends ModelElement implements Serializable {

    static ModelElementIndexManager segmentIM = new ModelElementIndexManager();
//    LinkedList<BoundaryNode> nodes = new LinkedList<BoundaryNode>();
    Route route;
    LinkedList<BoundaryCondition> boundaryConditions;
    Segment back,front;

    public void setBack(Segment back) {
        this.back = back;
    }

    public void setFront(Segment front) {
        this.front = front;
    }


    public Segment getBack() {
        return back;
    }

    public Segment getFront() {
        return front;
    }

    /**
     * 获取该线段所赋的边界条件
     * @return 注：当没有赋矛边界条件时返回null
     */
    public LinkedList<BoundaryCondition> getBoundaryConditions() {
        return boundaryConditions;
    }

    /**
     * 如果输入的列表为空，则getBoundaryContitions会返回null
     * @param bcs  注如果输入的列表为空，则getBoundaryContitions会返回null
     */
    public void setBoundaryConditions(Collection<BoundaryCondition> bcs) {
        if (null != bcs) {
            if (bcs.size() == 0) {
                boundaryConditions = null;
            } else {
                if (null != boundaryConditions) {
                    boundaryConditions.clear();
                    boundaryConditions.addAll(bcs);
                }
            }
        } else {
            boundaryConditions = null;
        }
    }

    public boolean addBoundaryCondition(BoundaryCondition e) {
        if(null==boundaryConditions){
            boundaryConditions=new LinkedList<BoundaryCondition>();
        }
        return boundaryConditions.add(e);
    }



    protected Segment() {
        index = segmentIM.getNewIndex();
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return segmentIM;
    }
    public Point[] pts;

    /**
     * 主要设计为{@link Route#addToPath(java.awt.geom.Path2D)}调用，用来将几何模型转化为java所支持的格式
     * @param path
     */
    abstract public void addToPath(Path2D path);

    /**
     * segment是否与Line(v1-v2)相交
     * @param v1
     * @param v2
     * @return
     */
    abstract public boolean isIntersectByLine(Point v1, Point v2);

    abstract public LinkedList<ApproximatePoint> GenerateApproximatePoints(double size, double flatness, LinkedList<ApproximatePoint> aprxPts);

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    protected Segment(boolean temp) {
        if (!temp) {
            index = segmentIM.getNewIndex();
        }
    }

    abstract public Point getFirstVertex();

    abstract public Point getLastVertex();

    abstract public void setFirstVertex(Point v);

    abstract public void setLastVertex(Point v);

    abstract public double[] parameterPoint(double t, double[] pt);

    abstract public double[] parameterDifference(double t, double[] pt);

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(type());
        sb.append(":");
        for (int i = 0; i < pts.length; i++) {
            sb.append(String.format("(%.2f, %.2f) ", pts[i].x, pts[i].y));
        }
        return sb.toString();
    }
}
