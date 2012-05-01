/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface PartDiffCoordinateArrayFunction extends PartDiffOrdered{
    double[][] values(Coordinate coord,double[][] results);
    
    int getDim();
}
