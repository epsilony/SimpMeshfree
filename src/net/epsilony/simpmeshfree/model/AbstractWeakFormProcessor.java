/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.epsilony.geom.Coordinate;
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
public abstract class AbstractWeakFormProcessor {

    LayeredRangeTree<Node> nodesTree;
    LayeredRangeTree<Boundary> boundariesTree;
    ShapeFunction shapeFun;

    abstract FromToCalculator getFromToCalulator();
    Matrix matrix;
    Vector vector;

    abstract PartialDiffType[] getVolumeShapeFunctionTypes();

    abstract PartialDiffType[] getNeumannShapeFunctionTypes();

    abstract Iterable<QuadraturePoint> getVolumeQuadratureIterable();

    abstract BoundaryCondition getVolumeBoundaryCondtion();

    abstract Iterable<BCQuadraturePoint> getDirichletBCQuadratureIterable();

    abstract Iterable<BCQuadraturePoint> getNeumannBCQuadratureIterable();

    Collection<Node> searchNodes(Node from, Node to, Collection<Node> addTo) {
        nodesTree.search(addTo, from, to);
        return addTo;
    }

    Collection<Boundary> searchBoundaris(Boundary from, Boundary to, Collection<Boundary> addTo) {
        boundariesTree.search(addTo, from, to);
        return addTo;
    }

    private void initNodesSearch(Collection<Node> nodes, List<Comparator<Node>> comps) {
        nodesTree = new LayeredRangeTree<>(nodes, comps);
    }

    private void initBoundariesSearch(Collection<Boundary> boundaries, List<Comparator<Boundary>> comps) {
        boundariesTree = new LayeredRangeTree<>(boundaries, comps);
    }

    void setEuqation(Matrix matrix, Vector vector) {
        this.matrix = matrix;
        this.vector = vector;
    }

    abstract WeakFormAssemblier getAssemblier();

    public ShapeFunction getShapeFun() {
        return shapeFun;
    }

    private void setShapeFun(ShapeFunction shapeFun) {
        this.shapeFun = shapeFun;
    }

    protected AbstractWeakFormProcessor(ShapeFunction shapeFun, Collection<Node> nodes, List<Comparator<Node>> nodeComps, Collection<Boundary> boundaries, List<Comparator<Boundary>> boundComps) {
        setShapeFun(shapeFun);
        initNodesSearch(nodes, nodeComps);
        initBoundariesSearch(boundaries, boundComps);
    }

    void assemblyEquation(int arrayListSize) {
        Iterable<QuadraturePoint> volIter = getVolumeQuadratureIterable();
        ArrayList<Node> searchedNodes = new ArrayList<>(arrayListSize);
        ArrayList<Boundary> seachedBoundaries = new ArrayList<>(arrayListSize);
        Node fromNode = new Node();
        Node toNode = new Node();
        Boundary.CenterPointOnlyBoundary fromBoundary = new Boundary.CenterPointOnlyBoundary();
        Boundary.CenterPointOnlyBoundary toBoundary = new Boundary.CenterPointOnlyBoundary();
        FromToCalculator fromToCalculator = getFromToCalulator();
        Coordinate from = new Coordinate();
        Coordinate to = new Coordinate();
        fromNode.coordinate = from;
        toNode.coordinate = to;
        fromBoundary.centerPoint = from;
        toBoundary.centerPoint = to;
        WeakFormAssemblier assemblier = getAssemblier();

        BoundaryCondition volumnBoundaryCondition = getVolumeBoundaryCondtion();
        if (null == volumnBoundaryCondition) {
            PartialDiffType[] types = new PartialDiffType[]{PartialDiffType.X(), PartialDiffType.Y()};
            shapeFun.setPDTypes(types);
            DenseVector[] vectors = new DenseVector[types.length];
            
            for (QuadraturePoint qp : volIter) {
                Coordinate qPoint = qp.point;
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
            for (QuadraturePoint qp : volIter) {
                Coordinate qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);

                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmBalance(qp, searchedNodes, vectors, volumnBoundaryCondition);
            }
        }

        Iterable<BCQuadraturePoint> dirichletIter = getDirichletBCQuadratureIterable();
        if (null != dirichletIter) {
            
            PartialDiffType[] types = new PartialDiffType[]{PartialDiffType.ORI()};
            shapeFun.setPDTypes(types);
            DenseVector[] vectors = new DenseVector[types.length];
            for (BCQuadraturePoint qp : dirichletIter) {
                Coordinate qPoint = qp.point;
                fromToCalculator.calculate(qPoint, from, to);
                searchedNodes.clear();
                seachedBoundaries.clear();
                searchNodes(fromNode, toNode, searchedNodes);
                searchBoundaris(fromBoundary, toBoundary, seachedBoundaries);

                shapeFun.values(qPoint, searchedNodes, seachedBoundaries, vectors);
                assemblier.asmDirichlet(qp, searchedNodes, vectors);
            }
        }

        Iterable<BCQuadraturePoint> neumannIter = getNeumannBCQuadratureIterable();
        if (null != neumannIter) {

            PartialDiffType[] types = getNeumannShapeFunctionTypes();
            shapeFun.setPDTypes(types);
            Vector[] vectors = new Vector[types.length];
            for (BCQuadraturePoint qp : neumannIter) {
                Coordinate qPoint = qp.point;
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
