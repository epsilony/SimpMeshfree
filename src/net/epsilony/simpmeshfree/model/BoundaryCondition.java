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
public interface BoundaryCondition {
    Boundary getBoundary();
    boolean[] valueByParameter(Coordinate parameter,double[] results);
    boolean[] valueByCoordinate(Coordinate coord,double[] results);
    
}
