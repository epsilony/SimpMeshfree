/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import java.util.List;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakformProblem {

    QuadraturePointIterator volumeIterator();

    QuadraturePointIterator neumannIterator();

    QuadraturePointIterator dirichletIterator();
    
    List<Node> dirichletNodes();

    VolumeCondition volumeCondition();
}
