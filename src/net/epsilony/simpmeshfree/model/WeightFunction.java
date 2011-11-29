/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.PartDiffOrdSettable;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeightFunction extends PartDiffOrdSettable{

    double[] values(Node node, Coordinate point, double[] results);
}
