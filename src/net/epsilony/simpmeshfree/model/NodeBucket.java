/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import static java.lang.Math.abs;

/**
 * 存放几何位于基内的结点以及可能会影响到到这个域中坐标的支持点
 * <p> changelist 
 * <br> 0.20 通过了CantilevelTest </br>
 * <br> 0.11 通过了RPIMTest </br>
 * <br> 0.10 初建 </br>
 * </p>
 * @version 0.20
 * @author M.Yuan J.-J.Chen
 */
public class NodeBucket {
double xMin, yMin, xMax, yMax;
    public LinkedList<Node> nodes; //位于NodeBucket中的结点s
    public LinkedList<Node> supportNodes; //可能的支持域点，即影响域与本NodeBucket有交集的点。
    
    public void addSupportNodes(Node node){
        supportNodes.add(node);
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public LinkedList<Node> getSupportNodes() {
        return supportNodes;
    }
    public NodeBucket(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        nodes = new LinkedList<Node>();
        supportNodes=new LinkedList<Node>();
    }

    public double getXMax() {
        return xMax;
    }

    public double getXMin() {
        return xMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getYMin() {
        return yMin;
    }

    public int nodesSize() {
        return nodes.size();
    }

    /**
     * 将Bucket分裂为两半，即原来的bucket变成原来的一半大小，并另新建一个NodeBucket，占据缩小的那半空间
     * <br>分裂过程中将NodeBucket中的节点按位置重新分配入调整过大小的NodeBucket与新建的NodeBucket中</br>
     * <br>分裂的原则是，如果x向宽度大，则沿y向中轴线分裂，返之如y向宽度大则沿x向对称轴分裂</br>
     * @return 分裂中新生成的那个NodeBucket
     */
    public NodeBucket fission() {
        double t;
        NodeBucket fBucket;
        if (abs(yMin - yMax) > abs(xMin - xMax)) {
            t=yMax;
            yMax=(yMin+yMax)/2;
            fBucket = new NodeBucket(xMin,yMax,xMax,t);
        } else {
            t=xMax;
            xMax=(xMin+xMax)/2;
            fBucket = new NodeBucket(xMax,yMin,t,yMax);
        }
        LinkedList<Node> tnodes=nodes;
        nodes=new LinkedList<Node>();
        for (Node node:tnodes){
            if(isLocateIn(node)){
                addNode(node);
            }else{
                fBucket.addNode(node);
            }
        }
        return fBucket;
    }

    public boolean isLocateIn(Node node) {
        return node.getX() < xMax && node.getX() >= xMin && node.getY() >= yMin && node.getY() < yMax;
    }

    public boolean addNode(Node arg0) {
        return nodes.add(arg0);
    }
    
    /**是否与一个以x,y为中心的，边长为2*dis的正方形框子相交
     * 注：如返回值为false，则不会与一个以x,y为中心的半径为dis的圆形区域相交
     * @param x
     * @param y
     * @param dis
     * @return 
     */
    public boolean isSqureIntersected( double x, double y, double dis) {
        double xLMin = x - dis;
        double yLMin = y - dis;
        double xLMax = x + dis;
        double yLMax = y + dis;
        return !(xMax < xLMin || xMin > xLMax || yMax < yLMin || yMin > yLMax);
    }
    
     public boolean isLocateIn(double x,double y){
        return x<xMax&&y<yMax&&y>yMin&&x>xMin;
    }

     /**
      * 获取外形
      * @return 可用于java2D Graphics2D.draw
      */
     public Shape getShape(){
         return new Rectangle2D.Double(xMin, yMin, xMax-xMin, yMax-yMin);
     }
}
