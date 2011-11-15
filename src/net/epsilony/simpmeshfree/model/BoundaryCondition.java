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
public interface BoundaryCondition {
    enum Type{Neumann,Dirichlet,Volume};
    double[] getValue(Coordinate pos);
    Type getType();
}
