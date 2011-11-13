/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import java.util.List;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface ShapeFunction<COORD> {

    Vector[] values(COORD center, List<Node<COORD>> nodes, Collection<Boundary<COORD>> boundaries, Vector[] results);
    
    void setPDTypes(PartialDiffType[] types);
}
