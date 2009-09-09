/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;

import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class Node extends Point implements Serializable{

    public static Node tempNode(double x, double y) {
        Node n = new Node();
        n.x = x;
        n.y = y;
        return n;
    }
    public static final Comparator<Node> nodeComparator = new NodeComparator();

    static class NodeComparator implements Comparator<Node>, Serializable {

        @Override
        public int compare(Node o1, Node o2) {
            return o1.index - o2.index;
        }
    }
    transient static ModelElementIndexManager nodeIM = new ModelElementIndexManager();
    transient TreeSet<Node> neighbors = new TreeSet<Node>(nodeComparator);
    transient LinkedList<Triangle> triangles = new LinkedList<Triangle>();
    transient LinkedList<Node> affectedNodes = new LinkedList<Node>();
    transient LinkedList<Node> supportDomainNodes = affectedNodes;
    public int flag;
    int matrixIndex;
    int bandWidth;

    public int getMatrixIndex() {
        return matrixIndex;
    }

    public void setMatrixIndex(int matrixIndex) {
        this.matrixIndex = matrixIndex;
    }

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

    protected Node() {
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

    public Path2D neighborNetPath(Path2D path) {
        flag = 1;
        for (Node n : neighbors) {
            path.moveTo(x, y);
            path.lineTo(n.x, n.y);
            if (n.flag == 0) {
                n.flag = 2;
                n.neighborNetPathTool(path);
            }
        }
        return path;
    }

    private void neighborNetPathTool(Path2D path) {
        for (Node n : neighbors) {
            if (n.flag > flag || n.flag == 0) {
                path.moveTo(x, y);
                path.lineTo(n.x, n.y);
                if (n.flag == 0) {
                    n.flag = flag + 1;
                    n.neighborNetPathTool(path);
                }
            }

        }
    }

    public ArrayList<Node> bfsTraverse(int layer, ArrayList<Node> list) {
        list.clear();
        if (layer < 0) {
            return list;
        }
        Node node;
        flag = index;
        int f = flag, start = 0, end = 1;
        list.add(this);
        for (int i = 0; i < layer; i++) {
            for (int j = start; j < end; j++) {
                node = list.get(j);
                for (Node n : node.neighbors) {
                    if (n.flag != f) {
                        n.flag = f;
                        list.add(n);
                    }
                }
            }
            start = end;
            end = list.size();
            if (start == end) {
                break;
            }
        }
        return list;
    }

    public ArrayList<Node> bfsTraverse(ArrayList<Node> list) {
        list.clear();
        flag = index;
        int f = flag;
        list.add(this);
        for (int i = 0; i < list.size(); i++) {
            for (Node n : list.get(i).neighbors) {
                if (n.flag != f) {
                    list.add(n);
                    n.flag = f;
                }
            }
        }
        return list;
    }

    public ArrayList<Node> dfsTraverse(ArrayList<Node> list) {
        list.clear();

        return list;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * generate the bandWidth of matrixIndex
     * <br>the difference between {@link Node#getBandWidth() getBandWidth()} is that bandWidth() calculate the bandWidth and setBandWidth()
     * and getBandWidth mearly returns the bandWidth field value</br>
     * <br>the difference between {@link Node#bandWith(int) bandWidth(int)} is that bandWidth(int) calulcate the asumption situation of index
     * and won't call setBandWidth()</br>
     * @return matrixIndex
     */
    public int bandWidth() {
        int size = 0;
        for (Node n : neighbors) {
            if (Math.abs(n.matrixIndex - matrixIndex) > size) {
                size = Math.abs(n.matrixIndex - matrixIndex);
            }
        }
        bandWidth = size;
        return size;
    }

    /**
     * @see Node#bandWidth()
     * @param index the assumption index
     * @return the node band with by the assumpted index
     *
     */
    public int bandWith(int index) {
        int size = 0;
        for (Node n : neighbors) {
            if (Math.abs(n.matrixIndex - index) > size) {
                size = Math.abs(n.matrixIndex - index);
            }
        }
        return size;
    }

    public int getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(int bandWidth) {
        this.bandWidth = bandWidth;
    }

    public TreeSet<Node> getNeighbors() {
        return neighbors;
    }
    double ux, uy;

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
//    protected void copyNode(Node n) {
//        setXY(n);
//        setIndex(n.getIndex());
//    }
}
