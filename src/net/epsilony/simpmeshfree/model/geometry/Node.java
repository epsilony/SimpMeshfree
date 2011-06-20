/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author epsilon
 */
public class Node extends Point {

    public static final Comparator<Node> nodeComparator = new Comparator<Node>() {

        @Override
        public int compare(Node o1, Node o2) {
            return o1.index - o2.index;
        }
    };

    double infRadius; // influnce radius

    public double getInfRadius() {
        return infRadius;
    }

    public void setInfRadius(double infRadius) {
        this.infRadius = infRadius;
    }

    public Node(double x, double y) {
        super(x,y);
    }

    public Node(Point p) {
        super(p);
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

    public double ux, uy;

    public double getUx() {
        return ux;
    }

    public void setUx(double ux) {
        this.ux = ux;
    }

    public double getUy() {
        return uy;
    }

    public void setUy(double uy) {
        this.uy = uy;
    }

}
