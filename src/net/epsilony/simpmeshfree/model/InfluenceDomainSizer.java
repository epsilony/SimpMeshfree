/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;

/**
 *
 * @author epsilon
 */
public interface InfluenceDomainSizer {
    double getSize(Node nd);
    
    double getMaxSize();
}
