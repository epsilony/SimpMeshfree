/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.utils.PartDiffOrdered;
import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author epsilon
 */
public interface UnivariatePartDiffFunction extends PartDiffOrdered{
    TDoubleArrayList values(double x, TDoubleArrayList results);
}
