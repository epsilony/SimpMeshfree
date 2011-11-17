/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface Boundary{
    Coordinate getBoudaryCoordinate(int index);
    int getBoudaryElementsSize();
    Coordinate getCenterPoint(int index,Coordinate result);
    public static class CenterPointOnlyBoundary implements Boundary{
        Coordinate centerPoint;

        @Override
        public Coordinate getBoudaryCoordinate(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBoudaryElementsSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Coordinate getCenterPoint(int index, Coordinate result) {
            return centerPoint;
        }
    }
}
