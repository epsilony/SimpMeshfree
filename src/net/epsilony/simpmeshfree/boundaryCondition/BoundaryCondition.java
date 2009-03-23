/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.boundaryCondition;

import net.epsilony.simpmeshfree.model.geometry.BoundaryNode;
import net.epsilony.simpmeshfree.model.*;

/**
 *
 * @author epsilon
 */
public interface BoundaryCondition {
    BoundaryNode.BoundaryConditionType type();
    public double[] boundaryValues(double x,double y,double result[]);
    public double[] boundaryValues(double parm,double result[]);
}
