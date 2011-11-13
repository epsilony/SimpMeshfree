/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public interface Boundary<COORD> {
    COORD getBoudaryElement(int index);
    int getBoudaryElementsSize();
    COORD getCenterPoint(int index,COORD result);
    public static class CenterPointOnlyBoundary<COORD> implements Boundary<COORD>{
        COORD centerPoint;

        @Override
        public COORD getBoudaryElement(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBoudaryElementsSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public COORD getCenterPoint(int index, COORD result) {
            return centerPoint;
        }
    }
}
