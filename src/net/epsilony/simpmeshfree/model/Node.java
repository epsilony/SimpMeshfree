/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.geom.Path2D;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import net.epsilony.simpmeshfree.model.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

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
    static ModelElementIndexManager nodeIM = new ModelElementIndexManager();
    TreeSet<Node> neighbors = new TreeSet<Node>(nodeComparator);
    LinkedList<Triangle> triangles = new LinkedList<Triangle>();
    LinkedList<Node> affectedNodes = new LinkedList<Node>();
    public int flag;

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
        this.x = x;
        this.y = y;
        this.index = nodeIM.getNewIndex();
    }

    public Node(Point p) {
        setXY(p);
        this.index = nodeIM.getNewIndex();
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

    public Path2D getNeighborNetPath(Path2D path) {
        flag = 1;
        for (Node n : neighbors) {
            path.moveTo(x, y);
            path.lineTo(n.x, n.y);
            if (n.flag == 0) {
                n.flag = 2;
                n.getNeighborNetPathTool(path);
            }
        }
        return path;
    }

    void getNeighborNetPathTool(Path2D path) {
        for (Node n : neighbors) {
            if (n.flag > flag || n.flag == 0) {
                path.moveTo(x, y);
                path.lineTo(n.x, n.y);
                if (n.flag == 0) {
                    n.flag = flag + 1;
                    n.getNeighborNetPathTool(path);
                }
            }

        }
    }


//    protected void copyNode(Node n) {
//        setXY(n);
//        setIndex(n.getIndex());
//    }
}
