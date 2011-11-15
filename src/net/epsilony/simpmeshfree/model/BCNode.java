/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.model.BoundaryCondition.Type;

/**
 *
 * @author epsilon
 */
public class BCNode extends Node{
    public BoundaryCondition bc;

    public Type getType() {
        return bc.getType();
    }

    public double[] getValue(Coordinate pos) {
        return bc.getValue(pos);
    }     
}
