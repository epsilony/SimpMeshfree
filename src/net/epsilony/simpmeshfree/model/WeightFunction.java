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
public interface WeightFunction {

    void setPDTypes(PartialDiffType[] types);

    double[] values(Node node, Coordinate point, double[] results);
}
