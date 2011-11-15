/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.BoundaryCondition.Type;
/**
 *
 * @author epsilon
 */
public class BCQuadraturePoint  extends QuadraturePoint{
    public BoundaryCondition bc;

    public double[] getValue(Coordinate pos) {
        return bc.getValue(pos);
    }

    public Type getType() {
        return bc.getType();
    }
    
}
