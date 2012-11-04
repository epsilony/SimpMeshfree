/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.WithId;
import java.util.Comparator;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class Node extends Coordinate implements WithId{

    public int id;
    
    @Override
    public int getId(){
        return id;
    }
    
    @Override
    public void setId(int id){
        this.id = id;
    }

    public Node(double x, double y) {
        super(x,y);
    }

    public Node(double x, double y, double z) {
        super(x, y, z);
    }

    public Node(Coordinate coord) {
        super(coord);
    }

    public Node() {
    }

    @Override
    public String toString() {
        return String.format("n%d:(%f,%f,%f)", id,x, y, z);
    }

    public static Comparator<Node> comparatorByDim(final int dim) {
        switch (dim) {
            case 0:
                return new Comparator<Node>() {

                    @Override
                    public int compare(Node o1, Node o2) {
                        double x1 = o1.x;
                        double x2 = o2.x;
                        if (x1 < x2) {
                            return -1;
                        } else if (x1 == x2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                };
            case 1:
                return new Comparator<Node>() {

                    @Override
                    public int compare(Node o1, Node o2) {
                        double y1 = o1.y;
                        double y2 = o2.y;
                        if (y1 < y2) {
                            return -1;
                        } else if (y1 == y2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                };
            case 2:
                return new Comparator<Node>() {

                    @Override
                    public int compare(Node o1, Node o2) {
                        double z1 = o1.z;
                        double z2 = o2.z;
                        if (z1 < z2) {
                            return -1;
                        } else if (z1 == z2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                };
            default:
                throw new IllegalArgumentException();
        }
    }
    public static void main(String[] args) {
        Node nd=new Node();
        System.out.println("nd="+nd);
    }
}
