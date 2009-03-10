/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.geom.Path2D;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.model.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class Node extends Point{

    static ModelElementIndexManager nodeIM=new ModelElementIndexManager();
    LinkedList<Node> neighbors=new LinkedList<Node>();
    LinkedList<Triangle> attachedTriangles=new LinkedList<Triangle>();
    Segment attachedSegment=null;
    LinkedList<Node> affectedNodes=new LinkedList<Node>();
    public int nodeFlag;

    @Override
    public ModelElementType type() {
        return ModelElementType.Node;
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return nodeIM;
    }


    public boolean addNeighbor(Node e) {
        return neighbors.add(e);
    }

    double infRadius; // influnce radius

    public double getInfRadius() {
        return infRadius;
    }

    public void setInfRadius(double infRadius) {
        this.infRadius = infRadius;
    }

    public Node(double x, double y) {
        this.x=x;
        this.y=y;
        this.index=nodeIM.getNewIndex();
    }

    public  Node(Point p){
        setXY(p);
        this.index=nodeIM.getNewIndex();
    }

    /**
     * 判断点(x,y)与节点的距离是不是在dis之内
     * @param x
     * @param y
     * @return 点(x,y)与节点的距离在dis之内则为true
     */
    public boolean isInDistance(double x, double y, double dis) {
        return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y) <= dis * dis;
    }

    /**
     * 判断点(x,y)是否在节点的影响域内
     * @param x
     * @param y
     * @return 点(x,y)在节点的影响域内则返回true
     */
    public boolean isInfluenced(double x, double y) {
        return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y) <= infRadius * infRadius;
    }

    public void getNeighborNet(Path2D path){
        nodeFlag=1;
        for(Node n:neighbors){
            if(n.nodeFlag!=0){
                path.moveTo(x, y);
                path.lineTo(n.x, n.y);
                n.getNeighborNet(path);
            }
        }
    }

//    protected void copyNode(Node n) {
//        setXY(n);
//        setIndex(n.getIndex());
//    }
}
