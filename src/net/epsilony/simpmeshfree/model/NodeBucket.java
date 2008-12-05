/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.LinkedList;
import static java.lang.Math.abs;

/**
 *
 * @author M.Yuan J.-J.Chen
 */
public class NodeBucket {
double xMin, yMin, xMax, yMax;
    public LinkedList<Node> nodes;
    public LinkedList<Node> supportNodes;
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
     * 将Bucket分裂为两半，即原来的bucket变成原来的一半大小，并另新建一个bucket<br>
     * 分裂过程中将bucket中的节点按位置重新分配入调整过大小的bucket与新建的bucket中
     * @return 分裂中新生成的Bucket
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
}
