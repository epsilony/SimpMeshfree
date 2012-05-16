/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;
import java.util.List;
import net.epsilony.utils.geom.Coordinate;

/**
 * 用于可视(visible),衍射(diffraction)以即透明度(transparency)准则(criterion)</br>
 * 目前只实现了Visible于{@link BoundaryBasedFilters2D}</br>
 * 一般来说，本类需应用于{@link ShapeFunction}中的两个地方，一个是{@link ShapeFunction#values(net.epsilony.geom.Coordinate, java.util.List, java.util.Collection, no.uib.cipr.matrix.DenseVector[], java.util.List) }
 * 中的开头，调用{@link #filterNodes(java.util.Collection, net.epsilony.geom.Coordinate, java.util.List, java.util.List) }用以过滤结点，另一个是在{@link ShapeFunction#values(net.epsilony.geom.Coordinate, java.util.List, no.uib.cipr.matrix.DenseVector[]) ShapeFunction.values()}
 * 中求权函数的值时在{@link WeightFunction#values(net.epsilony.simpmeshfree.model.Node, net.epsilony.geom.Coordinate, double[]) }中调用{@link #distance(net.epsilony.simpmeshfree.model.Node, net.epsilony.geom.Coordinate, double[]) }的
 * @see WeightFunctions2D
 * @see ShapeFunctions2D
 * @author epsilonyuan@gmail.com
 */
public interface BoundaryBasedCritieron{
    /**
     * @param center
     * @param centerBound 
     * @param outputNodes
     * @return supportDomainRadius 
     */
    double setCenter(Coordinate center,Boundary centerBound,List<Node> outputNodes);
    
    DistanceFunction distanceFunction();
}
