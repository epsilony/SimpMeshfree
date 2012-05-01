/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Comparator;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class Coordinate2Ds{

    public static Comparator<Coordinate> compX;
    public static Comparator<Coordinate> compY;


    static {
        compX = new Comparator<Coordinate>() {

            @Override
            public int compare(Coordinate o1, Coordinate o2) {
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

        compY = new Comparator<Coordinate>() {

            @Override
            public int compare(Coordinate o1, Coordinate o2) {
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

    public static String toString(Coordinate coord) {
        return String.format("%d-(%.2f, %.2f)", coord.x, coord.y);
    }
}
