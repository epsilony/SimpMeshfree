/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.model.BoundaryCondition.Type;

/**
 *
 * @author epsilon
 */
public class BCNode <COORD,VCOORD> extends Node<COORD>{
    public BoundaryCondition<COORD,VCOORD> bc;

    public Type getType() {
        return bc.getType();
    }

    public VCOORD getValue(COORD pos) {
        return bc.getValue(pos);
    }     
}
