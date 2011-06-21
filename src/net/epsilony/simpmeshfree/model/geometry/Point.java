/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 *
 * @author epsilon
 */
public class Point extends Point2D.Double implements Indexing {

    int index;
    public static Comparator<Point> compX;
    public static Comparator<Point> compY;


    static {
        compX = new Comparator<Point>() {

            @Override
            public int compare(Point o1, Point o2) {
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

        compY = new Comparator<Point>() {

            @Override
            public int compare(Point o1, Point o2) {
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

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    @Override
    public String toString() {
        return String.format("%s:%d-(%.2f, %.2f)", Point.class.getSimpleName(),index, x, y);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index=index;
    }
    
}
