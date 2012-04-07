/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Comparator;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class LineBoundary2D implements Boundary {

    public LineBoundary2D(Coordinate rear, Coordinate front) {
        this.rear = rear;
        this.front = front;
    }

    public LineBoundary2D() {
    }

    
    
    public Coordinate rear, front;

    @Override
    public Coordinate getPoint(int index) {
        switch (index) {
            case 0:
                return rear;
            case 1:
                return front;
            default:
                throw new IndexOutOfBoundsException(String.format("The index must betwean [0,2), but requires %d", index));
        }
    }

    @Override
    public int pointsSize() {
        return 2;
    }

    @Override
    public Coordinate centerPoint(Coordinate result) {
        result.x = (front.x + rear.x) / 2;
        result.y = (front.y + rear.y) / 2;
        return result;
    }

    @Override
    public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
        double dx_dt = front.x - rear.x;
        double dy_dt = front.y - rear.y;
        results[0].x = dx_dt;
        results[0].y = dy_dt;
        return results;
    }

    @Override
    public Coordinate valueByParameter(Coordinate par, Coordinate result) {
        double t = par.x;
        result.x = front.x * t + rear.x * (1 - t);
        result.y = front.y * t + rear.y * (1 - t);
        return result;
    }

    public static Comparator<LineBoundary2D> comparatorByDim(final int dim) {
        switch (dim) {
            case 0:
                return new Comparator<LineBoundary2D>() {

                    @Override
                    public int compare(LineBoundary2D o1, LineBoundary2D o2) {
                        double t1 = o1.front.x + o1.rear.x;
                        double t2 = o2.front.x + o2.rear.x;
                        if (t1 < t2) {
                            return -1;
                        } else if (t1 == t2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };
            case 1:
                return new Comparator<LineBoundary2D>() {

                    @Override
                    public int compare(LineBoundary2D o1, LineBoundary2D o2) {
                        double t1 = o1.front.y + o1.rear.y;
                        double t2 = o2.front.y + o2.rear.y;
                        if (t1 < t2) {
                            return -1;
                        } else if (t1 == t2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };
            default:
                throw new IllegalArgumentException();
        }
    }

    public static double longestLength(LineBoundary2D[] boundaries) {
        double longest = 0;
        for (int i = 0; i < boundaries.length; i++) {
            LineBoundary2D bound = boundaries[i];
            double dx = bound.front.x - bound.rear.x;
            double dy = bound.front.y - bound.rear.y;
            double sq = dx * dx + dy * dy;
            if (longest < sq) {
                longest = sq;
            }
        }
        return Math.sqrt(longest);
    }
}
