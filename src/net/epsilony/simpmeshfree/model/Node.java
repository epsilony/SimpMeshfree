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

    double x, y;
    static int maxIndex;
    int index=maxIndex++;
    public Node(double xGive, double yGive) {
        x = xGive;
        y = yGive;
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
}
