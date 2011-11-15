/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import java.util.List;
import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public interface BoundaryBasedFilter {
     void setPDTypes(PartialDiffType[] types);
    void filterNodes(Collection<Boundary> bns,Coordinate center,List<Node> nodes,List<Node> results);
    double distanceSqure(Node node,Coordinate center);
    double distance(Node node,Coordinate center);
    
}
