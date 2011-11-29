/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Comparator;
import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class Node{

    public int id;
    public Coordinate coordinate;

    public Node(double x, double y) {
        coordinate = new Coordinate(x, y);
    }

    public Node(double x, double y, double z) {
        coordinate = new Coordinate(x, y, z);
    }

    public Node(Coordinate coord) {
        coordinate = coord;
    }

    public Node() {
        coordinate = new Coordinate();
    }

    @Override
    public String toString() {
        Coordinate coord = coordinate;
        return String.format("n(%f,%f,%f)", coord.x, coord.y, coord.z);
    }

    public static Comparator<Node> comparatorByDim(final int dim) {
        switch (dim) {
            case 0:
                return new Comparator<Node>() {

                    @Override
                    public int compare(Node o1, Node o2) {
                        double x1 = o1.coordinate.x;
                        double x2 = o2.coordinate.x;
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
                        double y1 = o1.coordinate.y;
                        double y2 = o2.coordinate.y;
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
                        double z1 = o1.coordinate.z;
                        double z2 = o2.coordinate.z;
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
}
