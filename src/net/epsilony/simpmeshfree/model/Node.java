/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class Node {

    public enum NodeType {

        NORMAL, ESSENTIAL;
    }
    NodeType type;
    double x, y;
    static int maxIndex;
    int index = maxIndex++;

    public NodeType getType() {
        return type;
    }
    
    double infRadius; // influnce radius

    public double getInfRadius() {
        return infRadius;
    }

    public void setInfRadius(double infRadius) {
        this.infRadius = infRadius;
    }
    

    public Node(double x, double y) {
        type = NodeType.NORMAL;
        this.x = x;
        this.y = y;
    }

    public Node(double x, double y, NodeType type) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public void asEssential() {
        type = NodeType.ESSENTIAL;
    }

    public boolean isEssential() {
        return type == NodeType.ESSENTIAL;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getIndex() {
        return index;
    }
   
    /**
     * 判断点(x,y)与节点的距离是不是在dis之内
     * @param x
     * @param y
     * @return 点(x,y)与节点的距离在dis之内则为true
     */
    public boolean isInDistance(double x,double y,double dis){
        return (this.x - x) * (this.x -x) + (this.y - y) * (this.y - y) <= dis * dis;
    }
    
    /**
     * 判断点(x,y)是否在节点的影响域内
     * @param x
     * @param y
     * @return 点(x,y)在节点的影响域内则返回true
     */
    public boolean isInfluenced(double x,double y){
        return (this.x - x) * (this.x -x) + (this.y - y) * (this.y - y) <= infRadius * infRadius;
    }
}
