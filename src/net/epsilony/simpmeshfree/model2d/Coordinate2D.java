/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Comparator;

/**
 *
 * @author epsilon
 */
public class Coordinate2D{

    public double x,y;
    public static Comparator<Coordinate2D> compX;
    public static Comparator<Coordinate2D> compY;


    static {
        compX = new Comparator<Coordinate2D>() {

            @Override
            public int compare(Coordinate2D o1, Coordinate2D o2) {
                double t = o1.x - o2.x;
                if (t > 0) {
                    return 1;
                } else {
                    if (t < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        };

        compY = new Comparator<Coordinate2D>() {

            @Override
            public int compare(Coordinate2D o1, Coordinate2D o2) {
                double t = o1.y - o2.y;
                if (t > 0) {
                    return 1;
                } else {
                    if (t < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        };
    }

    public Coordinate2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate2D(Coordinate2D p) {
        this.x = p.x;
        this.y = p.y;
    }

    @Override
    public String toString() {
        return String.format("%s:%d-(%.2f, %.2f)", Coordinate2D.class.getSimpleName(), x, y);
    }
}
