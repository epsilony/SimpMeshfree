/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface Boundary {

    Coordinate getBoudaryPoint(int index);

    int getBoudaryPointsSize();

    Coordinate getCenterPoint(Coordinate result);
    
    Coordinate valueByParameter(Coordinate par,Coordinate result);

    Coordinate[] valuePartialByParameter(Coordinate par, Coordinate results[]);

    public static class CenterPointOnlyBoundary implements Boundary {

        Coordinate centerPoint;

        @Override
        public Coordinate getBoudaryPoint(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBoudaryPointsSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Coordinate getCenterPoint(Coordinate result) {
            return centerPoint;
        }

        @Override
        public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Coordinate valueByParameter(Coordinate par,Coordinate result) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
