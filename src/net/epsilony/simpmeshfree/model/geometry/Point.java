/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Point2D;
import java.util.Comparator;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

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

    public static Point tempPoint(double x, double y) {
        Point p = new Point();
        p.x = x;
        p.y = y;
        return p;
    }

    public static Point tempMaxIndexPoint(double x, double y) {
        Point p = new Point();
        p.x = x;
        p.y = y;
        p.index = pointIm.getMax() + 1;
        return p;
    }

    static ModelElementIndexManager pointIm = new ModelElementIndexManager();

    protected Point(double x, double y, Boolean temp) {
        if (!temp) {
            index = pointIm.getNewIndex();
        }
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y) {
        index = pointIm.getNewIndex();
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
        index = pointIm.getNewIndex();
    }

    protected Point() {
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setXY(Point p) {
        this.x = p.x;
        this.y = p.y;
    }
    
    public void setXY(double x,double y){
        this.x=x;
        this.y=y;
    }

    public Point2D setPoint2D(Point2D p2) {
        p2.setLocation(x, y);
        return p2;
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
    
    public static void main(String[] args) {
        Point p=new Point(3,4);
        p.index=144;
        System.out.println(p);
    }
}
