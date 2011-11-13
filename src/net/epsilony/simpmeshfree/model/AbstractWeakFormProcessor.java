/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.epsilony.simpmeshfree.model.PartialDiffType;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.FromToCalculator;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.util.LayeredRangeTree;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public abstract class AbstractWeakFormProcessor<COORD, VCOORD> {

    LayeredRangeTree<Node<COORD>> nodesTree;
    LayeredRangeTree<Boundary<COORD>> boundariesTree;
    ShapeFunction<COORD> shapeFun;

    abstract FromToCalculator<COORD> getFromToCalulator();
    Matrix matrix;
    Vector vector;

    abstract PartialDiffType[] getVolumeShapeFunctionTypes();

    abstract PartialDiffType[] getNeumannShapeFunctionTypes();

    abstract Iterable<QuadraturePoint<COORD>> getVolumeQuadratureIterable();

    abstract BoundaryCondition<COORD, VCOORD> getVolumeBoundaryCondtion();

    abstract Iterable<BCQuadraturePoint<COORD, VCOORD>> getDirichletBCQuadratureIterable();

    abstract Iterable<BCQuadraturePoint<COORD, VCOORD>> getNeumannBCQuadratureIterable();

    Collection<Node<COORD>> searchNodes(Node from, Node to, Collection<Node<COORD>> addTo) {
        nodesTree.search(addTo, from, to);
        return addTo;
    }

    Collection<Boundary<COORD>> searchBoundaris(Boundary<COORD> from, Boundary<COORD> to, Collection<Boundary<COORD>> addTo) {
        boundariesTree.search(addTo, from, to);
        return addTo;
    }

    private void initNodesSearch(Collection<Node<COORD>> nodes, List<Comparator<Node<COORD>>> comps) {
        nodesTree = new LayeredRangeTree<>(nodes, comps);
    }

    private void initBoundariesSearch(Collection<Boundary<COORD>> boundaries, List<Comparator<Boundary<COORD>>> comps) {
        boundariesTree = new LayeredRangeTree<>(boundaries, comps);
    }

    void setEuqation(Matrix matrix, Vector vector) {
        this.matrix = matrix;
        this.vector = vector;
    }

    abstract WeakFormAssemblier<COORD, VCOORD> getAssemblier();

    public ShapeFunction<COORD> getShapeFun() {
        return shapeFun;
    }

    private void setShapeFun(ShapeFunction<COORD> shapeFun) {
        this.shapeFun = shapeFun;
    }

    protected AbstractWeakFormProcessor(ShapeFunction shapeFun, Collection<Node<COORD>> nodes, List<Comparator<Node<COORD>>> nodeComps, Collection<Boundary<COORD>> boundaries, List<Comparator<Boundary<COORD>>> boundComps) {
        setShapeFun(shapeFun);
        initNodesSearch(nodes, nodeComps);
        initBoundariesSearch(boundaries, boundComps);
    }

    void assemblyEquation(int arrayListSize) {
        Iterable<QuadraturePoint<COORD>> volIter = getVolumeQuadratureIterable();
        ArrayList<Node<COORD>> searchedNodes = new ArrayList<>(arrayListSize);
        ArrayList<Boundary<COORD>> seachedBoundaries = new ArrayList<>(arrayListSize);
        Node<COORD> fromNode = new Node<>();
        Node<COORD> toNode = new Node<>();
        Boundary.CenterPointOnlyBoundary<COORD> fromBoundary = new Boundary.CenterPointOnlyBoundary<>();
        Boundary.CenterPointOnlyBoundary<COORD> toBoundary = new Boundary.CenterPointOnlyBoundary<>();
        FromToCalculator<COORD> fromToCalculator = getFromToCalulator();
        COORD from = fromToCalculator.coordinateFactory();
        COORD to = fromToCalculator.coordinateFactory();
        fromNode.coordinate = from;
        toNode.coordinate = to;
        fromBoundary.centerPoint = from;
        toBoundary.centerPoint = to;
        WeakFormAssemblier<COORD, VCOORD> assemblier = getAssemblier();

        BoundaryCondition<COORD, VCOORD> volumnBoundaryCondition = getVolumeBoundaryCondtion();
        if (null == volumnBoundaryCondition) {
            PartialDiffType[] types = new PartialDiffType[]{PartialDiffType.X(), PartialDiffType.Y()};
            shapeFun.setPDTypes(types);
            DenseVector[] vectors = new DenseVector[types.length];
            
            for (QuadraturePoint<COORD> qp : volIter) {
                COORD qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);
                
                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmBalance(qp, searchedNodes, vectors, null);
            }
        } else {
            PartialDiffType[] types = getVolumeShapeFunctionTypes();
            shapeFun.setPDTypes(types);
            DenseVector[] vectors = new DenseVector[types.length];
            for (QuadraturePoint<COORD> qp : volIter) {
                COORD qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);

                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmBalance(qp, searchedNodes, vectors, volumnBoundaryCondition);
            }
        }

        Iterable<BCQuadraturePoint<COORD, VCOORD>> dirichletIter = getDirichletBCQuadratureIterable();
        if (null != dirichletIter) {
            
            PartialDiffType[] types = new PartialDiffType[]{PartialDiffType.ORI()};
            shapeFun.setPDTypes(types);
            DenseVector[] vectors = new DenseVector[types.length];
            for (BCQuadraturePoint<COORD, VCOORD> qp : dirichletIter) {
                COORD qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);

                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmDirichlet(qp, searchedNodes, vectors);
            }
        }

        Iterable<BCQuadraturePoint<COORD, VCOORD>> neumannIter = getNeumannBCQuadratureIterable();
        if (null != neumannIter) {

            PartialDiffType[] types = getNeumannShapeFunctionTypes();
            shapeFun.setPDTypes(types);
            Vector[] vectors = new Vector[types.length];
            for (BCQuadraturePoint<COORD, VCOORD> qp : neumannIter) {
                COORD qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);

                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmNeumann(qp, searchedNodes, vectors);
            }
        }


    }
}
