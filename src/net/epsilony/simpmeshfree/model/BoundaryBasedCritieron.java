/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import java.util.Collection;
import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.simpmeshfree.model2d.WeightFunctions2D;
import net.epsilony.simpmeshfree.utils.PartDiffOrdSettable;

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
public interface BoundaryBasedCritieron extends PartDiffOrdSettable {
    /**
     * filter the nodes which are conflict to the criterion
     * @param boundaries
     * @param center
     * @param nodes nodes to be filtered
     * @param results the filtered results list
     */
    void filterNodes(Collection<Boundary> boundaries,Coordinate center,List<Node> nodes,List<Node> results);
    
    /**
     * calculate the distance betwean center and node or the distance partial dirivatives respect to center's coordinates
     * @param node
     * @param center
     * @param result
     * @return result, the content is set by {@link PartDiffOrdSettable#setOrders(net.epsilony.simpmeshfree.utils.PartDiffOrd[]) }
     */
    double[] distance(Node node,Coordinate center,double[] result);  
    
    /**
     * 
     * @return true if {@link #setOrders(net.epsilony.simpmeshfree.utils.PartDiffOrd[]) } and {@link #distance(net.epsilony.simpmeshfree.model.Node, net.epsilony.geom.Coordinate, double[]) } are avilable
     */
    boolean isDistanceTrans();
}
