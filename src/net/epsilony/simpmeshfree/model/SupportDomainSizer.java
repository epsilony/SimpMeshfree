/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface SupportDomainSizer {
    //outputs can be null
    double domain(Coordinate center, List<Node> outputs);
    
}
