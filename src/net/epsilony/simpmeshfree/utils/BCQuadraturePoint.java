/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.BoundaryCondition.Type;
/**
 *
 * @author epsilon
 */
public class BCQuadraturePoint <COORD,VCOORD> extends QuadraturePoint<COORD>{
    public BoundaryCondition<COORD,VCOORD> bc;

    public VCOORD getValue(COORD pos) {
        return bc.getValue(pos);
    }

    public Type getType() {
        return bc.getType();
    }
    
}
