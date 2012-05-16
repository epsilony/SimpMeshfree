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
    boolean isByCoordinate();
    
    Boundary getBoundary();
    
    /**
     * boundary condition value respect to the parameter of boundary
     * @param parameter in 2D, boundary is circle and the parameter.x is only used, 
     * in 3D the parameter.x .y are used.
     * @param results 
     * @return results
     */
    boolean[] values(Coordinate input,double[] results);
    
}
