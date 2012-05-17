/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Coordinate;

/**
 * information class of boundary condition
 * ps: be aware that the volume force is supplied in {@link VolumeCondition}</br>
 * @author epsilonyuan@gmail.com
 */
public interface BoundaryCondition {
  
    /**
     * 
     * @param bnd
     * @return whether the first argument of {@link #values(net.epsilony.utils.geom.Coordinate, double[]) values} is a Cartesian coordinate or {@code bnd}'s parametic coordinate. true:
     * Cartesian, false:parametic
     */
    boolean setBoundary(Boundary bnd);
    /**
     * boundary condition value respect to the Cartesian Coordinate or parameter of {@link Boundary}
     * the {@link setBoundary} should be called first when evaluating on a fresh {@link Boundary}
     * @param parameter in 2D, boundary is circle and the parameter.x is only used, 
     * in 3D the parameter.x .y are used.
     * @param results 
     * @return results
     */
    void values(Coordinate input,double[] results,boolean[] validities);
    
}
