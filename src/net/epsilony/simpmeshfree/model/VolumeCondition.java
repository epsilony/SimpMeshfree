/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface VolumeCondition {
    double[] value(Coordinate pos);
}
