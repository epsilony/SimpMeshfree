/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.CenterSearcher;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakFormProblem {
    List<Boundary> getBoundaries();
    
    CenterSearcher<Coordinate, Boundary> boundarySearcher();
    
    Iterable<BCQuadraturePoint> neumannIterable(int power);
    
    int dirichletQuadraturePointsNum(int power);

    Iterable<BCQuadraturePoint> dirichletIterable(int power);

    int neumannQudaraturePointsNum(int power);
    
    CenterSearcher<Coordinate, Node> nodeSearcher();

    Iterable<QuadraturePoint> volumeIterable(int power);
    
    int balanceQuadraturePointsNum(int power);

    VolumeCondition getVolumeCondition();
    
    NodeSupportDomainSizer nodeSupportDomainSizer();
    
    List<Node> getNodes();
    
}
