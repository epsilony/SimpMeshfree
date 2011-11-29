/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import java.util.Collection;
import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.PartDiffOrdSettable;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import no.uib.cipr.matrix.DenseVector;

/**
 * 形函数 Shape Function。</br>
 * 使用时首先通过
 * {@link #setPDTypes(net.epsilony.simpmeshfree.model.PartialDiffType[])}
 * 设定{@link #values(net.epsilony.geom.Coordinate, java.util.List, no.uib.cipr.matrix.DenseVector[]) }
 * 与{@link #values(net.epsilony.geom.Coordinate, java.util.List, java.util.Collection, no.uib.cipr.matrix.DenseVector[], java.util.List) }
 * 的输出样式</br>
 * 典型的实现为{@link ShapeFunctions2D.MLS}</br>
 * @see PartialDiffType
 * @author epsilonyuan@gmail.com
 */
public interface ShapeFunction extends PartDiffOrdSettable{
    
    DenseVector[] values(Coordinate center,List<Node> nodes,DenseVector[] results);

    DenseVector[] values(Coordinate center, List<Node> nodes, Collection<Boundary> boundaries, DenseVector[] results,List<Node> filteredNodes);
}
