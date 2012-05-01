/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import net.epsilony.simpmeshfree.utils.PartDiffOrdered;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface DistanceFunction extends PartDiffOrdered{
    TDoubleArrayList values(Coordinate pt,TDoubleArrayList results);
    
    void setCenter(Coordinate center);
}
