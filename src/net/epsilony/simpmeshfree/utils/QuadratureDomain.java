/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface QuadratureDomain {
    void setPower(int power);
    int size();
    double coordinateAndWeight(int index,Coordinate output);
}
