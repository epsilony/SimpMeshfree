/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.utils.geom.Coordinate;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model2d.BCQuadratureIterables2D;
import net.epsilony.simpmeshfree.model2d.WeakFormProcessor2D;

/**
 * Quadrature point with the information of boundary condition</br>
 * commonly used by weak form processors like {@link WeakFormProcessor2D} 
 * in applying boundary conditions at {@link WeakFormProcessor2D#assemblyDirichlet() }
 * and {@link WeakFormProcessor2D#assemblyNeumann() }</br>
 * the {@link Iterable} wrappers are in {@link BCQuadratureIterables2D} </br>
 * ps: be aware that the volume force is supplied in {@link VolumeCondition}</br>
 * @see BoundaryCondition
 * @author epsilonyuan@gmail.com
 */
public class BCQuadraturePoint  extends QuadraturePoint{
    /**
     * the boundary condition at quadrature point
     */
    public BoundaryCondition boundaryCondition;
    
    /**
     * the parameter of boundary at quadrature point (Optional)
     */
    public Coordinate parameter;

    public BCQuadraturePoint() {
        parameter=new Coordinate();
    }
    
}
