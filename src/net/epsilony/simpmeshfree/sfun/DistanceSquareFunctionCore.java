/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import net.epsilony.utils.PartDiffOrdered;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface DistanceSquareFunctionCore extends PartDiffOrdered{
    double[] value(Coordinate pt,Coordinate center,double[] results);
}
