/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import java.util.List;
import net.epsilony.geom.Coordinate;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface ShapeFunction {

    Vector[] values(Coordinate center, List<Node> nodes, Collection<Boundary> boundaries, Vector[] results);
    
    void setPDTypes(PartialDiffType[] types);
}
