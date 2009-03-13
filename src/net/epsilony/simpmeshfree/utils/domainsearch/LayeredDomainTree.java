/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils.domainsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;
import net.epsilony.simpmeshfree.model.Point;

/**
 *
 * @author epsilon
 */
public class LayeredDomainTree {

    ArrayList<Point> points;
    static final Comparator<Point> yComp = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
//            if (o1.getY() == o2.getY()) {
//                return 0;
//            }
            if (o1.getY() < o2.getY()) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    static final Comparator<Point> xComp = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
//            if (o1.getX() == o2.getX()) {
//                return 0;
//            }
            if (o1.getX() < o2.getX()) {
                return -1;
            } else {
                return 1;
            }
        }
    };
    Comparator<Point> firstComp;
    Comparator<Point> secondComp;
    TreeSet<Point> outTree;

    class LayeredDomainTreeNode extends Point {

        int start, end;
        int layer;
        Point pt;
        LayeredDomainTreeNode left, right;
        LayeredDomainTreeNode father;
        LayeredDomainTreeNode leftChild, rightChild;
        TreeSet<Point> innerTree = new TreeSet<Point>(secondComp);

        LayeredDomainTreeNode(int start, int end, int layer, LayeredDomainTreeNode father) {
            this.start = start;
            this.end = end;
            this.layer = layer;
            this.father = father;
            innerTree.addAll(points.subList(start, end));
            if (start >= end) {
                pt = points.get(start);
                x = pt.getX();
                y = pt.getY();
                outTree.add(this);

                return;
            }
            leftChild = new LayeredDomainTreeNode(start, (start + end) / 2, layer + 1, this);
            rightChild = new LayeredDomainTreeNode((start + end) / 2 + 1, end, layer + 1, this);
            leftChild.right = rightChild;
            rightChild.left = leftChild;
        }

        void linkLayer() {
            if (leftChild != null && left != null && left.rightChild != null) {
                leftChild.left = left.rightChild;
            }
            if (rightChild != null && right != null && right.leftChild != null) {
                rightChild.right = right.leftChild;
            }
            if (leftChild != null) {
                leftChild.linkLayer();
            }
            if (rightChild != null) {
                rightChild.linkLayer();
            }
        }
    }
    LayeredDomainTreeNode fatherNode;

    public LayeredDomainTree(ArrayList<Point> points, boolean wider) {
        this.points = points;

        if (wider) {
            firstComp = xComp;
            secondComp = yComp;
        } else {
            firstComp = yComp;
            secondComp = xComp;
        }

        Collections.sort(points, firstComp);
        outTree = new TreeSet<Point>(firstComp);
        fatherNode = new LayeredDomainTreeNode(0, points.size() - 1, 0, null);
    }

    public List<Point> searchFirst(Point from, Point to, List<Point> outPts) {
        LayeredDomainTreeNode nFrom = (LayeredDomainTreeNode) outTree.floor(from);
        LayeredDomainTreeNode nTo = (LayeredDomainTreeNode) outTree.ceiling(to);
        outPts.clear();
        if (nFrom == null || nTo == null) {
            return outPts;
        }


        SortedSet<Point> tempSet;
        while (nFrom.layer > nTo.layer) {
            if (nFrom.father.right == nFrom) {
                outPts.addAll(nFrom.innerTree.subSet(from, true, to, true));
                nFrom = nFrom.right;
            } else {
                nFrom = nFrom.father;
            }
        }
        while (nTo.layer > nFrom.layer) {
            if (nTo.father.left == nTo) {
                outPts.addAll(nTo.innerTree.subSet(from, true, to, true));
                nTo = nTo.left;
            } else {
                nTo = nTo.father;
            }
        }
        while (nFrom.right != nTo) {
            if (nFrom.father.right == nFrom) {
                outPts.addAll(nFrom.innerTree.subSet(from, true, to, true));
                nFrom = nFrom.right;
            } else {
                nFrom = nFrom.father;
            }
            while (nTo.layer > nFrom.layer) {
                if (nTo.father.left == nTo) {
                    outPts.addAll(nTo.innerTree.subSet(from, true, to, true));
                    nTo = nTo.left;
                } else {
                    nTo = nTo.father;
                }
            }
        }
        if (nFrom.father == nTo.father) {
            outPts.addAll(nFrom.father.innerTree.subSet(from, true, to, true));
        } else {
            outPts.addAll(nFrom.innerTree.subSet(from,true,to,true));
            outPts.addAll(nTo.innerTree.subSet(from,true,to,true));
        }

        return outPts;
    }

    public static void main(String[] args) {
        Point[] pts = new Point[]{new Point(3, 0),
            new Point(6, 0),
            new Point(2, 0),
            new Point(1, 0),
            new Point(4, 0),
            new Point(1, 0),
            new Point(1, 0),
            new Point(5, 0),
            new Point(4, 0),
            new Point(6, 0),};
        ArrayList<Point> points = new ArrayList<Point>(pts.length);
        for (Point p : pts) {
            points.add(p);
        }
        System.out.println(Arrays.toString(pts));
        System.out.println(points);
        LayeredDomainTree dTree = new LayeredDomainTree(points, true);
        System.out.println("points = " + points);
        for (int i = 0; i < 8; i++) {
            Point p = Point.tempPoint(i, 0);
            LayeredDomainTreeNode node = (LayeredDomainTreeNode) dTree.outTree.ceiling(p);
            if (node != null) {
                System.out.println(i + " " + node.pt);
            } else {
                System.out.println(i + "null");
            }
        }
        NavigableSet<Point> decentTree = dTree.outTree.descendingSet();
        for (int i = 0; i < 8; i++) {
            Point p = Point.tempPoint(i, 0);
            LayeredDomainTreeNode node = (LayeredDomainTreeNode) decentTree.ceiling(p);
            if (node != null) {
                System.out.println(i + " " + node.pt);
            } else {
                System.out.println(i + "null");
            }
        }
        System.out.println("outTree.last() = " + ((LayeredDomainTreeNode) dTree.outTree.last()).pt);
        System.out.println("dTree.size() = " + dTree.outTree.size());
    }
}
