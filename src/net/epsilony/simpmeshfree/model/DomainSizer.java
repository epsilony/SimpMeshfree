/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface DomainSizer {
    //outputs can be null
    double domain(Coordinate center, List<Node> outputs);
    
    //infSizer can be null
    void setInfluenceDomainSizer(InfluenceDomainSizer infSizer);
}
