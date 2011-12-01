/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.util.CenterSearcher;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WorkProblem {
    List<Boundary> getBoundaries();
    
    CenterSearcher<Coordinate, Boundary> boundarySearcher();
    
    Iterable<BCQuadraturePoint> dirichletIterable(int power);

    Iterable<BCQuadraturePoint> neumannIterable(int power);

    CenterSearcher<Coordinate, Node> nodeSearcher();

    Iterable<QuadraturePoint> volumeIterable(int power);

    VolumeCondition getVolumeCondition();
    
    NodeSupportDomainSizer nodeSupportDomainSizer();
    
    List<Node> getNodes();
    
}
