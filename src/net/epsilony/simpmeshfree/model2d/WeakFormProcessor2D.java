/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.geom.GeometryMath;
import net.epsilony.math.util.EquationSolver;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakFormAssemblier;
import net.epsilony.simpmeshfree.model.WorkProblem;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.util.CenterSearcher;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakFormProcessor2D {

    ShapeFunction shapeFun;
    public int arrayListSize = 100;
    WeakFormAssemblier assemblier;
    WorkProblem workProblem;
    int quadraturePower;
    private final EquationSolver equationSolver;
    DenseVector equationResultVector;

    /**
     * 
     * @param shapeFun
     * @param assemblier
     * @param workProblem
     * @param power
     * @param equationSolver 
     */
    public WeakFormProcessor2D(ShapeFunction shapeFun, WeakFormAssemblier assemblier, WorkProblem workProblem, int power, EquationSolver equationSolver) {
        this.shapeFun = shapeFun;
        this.assemblier = assemblier;
        this.workProblem = workProblem;
        this.quadraturePower = power;
        this.equationSolver = equationSolver;
    }

    public void process() {
        assemblyBalanceEquation();

        assemblyDirichlet();

        assemblyNeumann();

    }

    public DenseVector getNodesResult() {
        return equationResultVector;
    }

    void assemblyBalanceEquation() {

        ArrayList<Node> searchedNodes = new ArrayList<>(arrayListSize);
        ArrayList<Node> filteredNodes = new ArrayList<>(arrayListSize);
        ArrayList<Boundary> searchedBoundaries = new ArrayList<>(arrayListSize);

        VolumeCondition volumnBoundaryCondition = workProblem.getVolumeCondition();
        Iterable<QuadraturePoint> volIter = workProblem.volumeIterable(quadraturePower);
        CenterSearcher<Coordinate, Node> nodeSearcher = workProblem.nodeSearcher();
        CenterSearcher<Coordinate, Boundary> boundarySearcher = workProblem.boundarySearcher();

        PartDiffOrd[] types;
        if (null == volumnBoundaryCondition) {
            types = new PartDiffOrd[]{PartDiffOrd.X(), PartDiffOrd.Y()};

        } else {
            types = new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()};
        }
        shapeFun.setOrders(types);

        DenseVector[] shapeFunctions = new DenseVector[types.length];

        for (QuadraturePoint qp : volIter) {
            Coordinate qPoint = qp.coordinate;
            nodeSearcher.search(qPoint, searchedNodes);
            boundarySearcher.search(qPoint, searchedBoundaries);
            shapeFun.values(qPoint, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);
            assemblier.asmBalance(qp, filteredNodes, shapeFunctions, volumnBoundaryCondition);
        }
    }

    public List<double[]> result(Iterable<Coordinate> coords) {
        LinkedList<Node> searchedNodes = new LinkedList<>();
        LinkedList<Node> filteredNodes = new LinkedList<>();
        LinkedList<Boundary> searchedBoundaries = new LinkedList<>();
        CenterSearcher<Coordinate, Node> nodeSearcher = workProblem.nodeSearcher();
        CenterSearcher<Coordinate, Boundary> boundarySearcher = workProblem.boundarySearcher();
        PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()};

        shapeFun.setOrders(types);

        DenseVector[] shapeFunctions = new DenseVector[types.length];

        LineBoundary2D line1 = new LineBoundary2D(), line2 = new LineBoundary2D();
        LinkedList<double[]> results = new LinkedList<>();
        for (Coordinate coord : coords) {

            nodeSearcher.search(coord, searchedNodes);
            boundarySearcher.search(coord, searchedBoundaries);

            Iterator<Boundary> boundIter = searchedBoundaries.iterator();
            while (boundIter.hasNext()) {
                Boundary bound = boundIter.next();
                Coordinate front = bound.getPoint(bound.pointsSize() - 1);
                Coordinate rear = bound.getPoint(0);
                if (front == coord || rear == coord) {
                    break;
                }
                if (GeometryMath.crossProduct(rear.x, rear.y, front.x, front.y, coord.x, coord.y) == 0) {
                    double cx = coord.x;
                    double cy = coord.y;
                    double t = (cx - rear.x) * (cx - front.x) + (cy - front.y) * (cy - rear.y);
                    if (t > 0) {
                        continue;
                    }

                    line1.rear = rear;
                    line1.front = coord;
                    line2.rear = coord;
                    line2.front = front;
                    boundIter.remove();
                    searchedBoundaries.add(line1);
                    searchedBoundaries.add(line2);
                    break;
                }
            }

            shapeFun.values(coord, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);

            double[] result = new double[2];
            int nodeCount = 0;
            for (Node nd : filteredNodes) {
                int index = nd.id * 2;
                double shapeValue = shapeFunctions[0].get(nodeCount++);
                result[0] += shapeValue * equationResultVector.get(index);
                result[1] += shapeValue * equationResultVector.get(index + 1);
            }
            results.add(result);
        }
        return results;
    }

    void assemblyDirichlet() {
        Iterable<BCQuadraturePoint> dirichletIter = workProblem.dirichletIterable(quadraturePower);
        if (dirichletIter == null) {
            return;
        }

        LinkedList<Node> searchedNodes = new LinkedList<>();
        ArrayList<Node> filteredNodes = new ArrayList<>(arrayListSize);
        ArrayList<Boundary> searchedBoundaries = new ArrayList<>(arrayListSize);
        CenterSearcher<Coordinate, Node> nodeSearcher = workProblem.nodeSearcher();
        CenterSearcher<Coordinate, Boundary> boundarySearcher = workProblem.boundarySearcher();

        PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI()};
        shapeFun.setOrders(types);

        DenseVector[] shapeFunctions = new DenseVector[types.length];

        LineBoundary2D line1 = new LineBoundary2D(), line2 = new LineBoundary2D();

        for (BCQuadraturePoint qp : dirichletIter) {
            Coordinate qPoint = qp.coordinate;
            nodeSearcher.search(qPoint, searchedNodes);
            boundarySearcher.search(qPoint, searchedBoundaries);

            Boundary bound = qp.boundaryCondition.getBoundary();
            if (qPoint != bound.getPoint(0) && qPoint != bound.getPoint(bound.pointsSize() - 1)) {
                Iterator<Boundary> boundIter = searchedBoundaries.iterator();
                while (boundIter.hasNext()) {
                    if (bound == boundIter.next()) {
                        line1.rear = bound.getPoint(0);
                        line1.front = qp.coordinate;
                        line2.rear = qp.coordinate;
                        line2.front = bound.getPoint(bound.pointsSize() - 1);
                        boundIter.remove();
                        searchedBoundaries.add(line1);
                        searchedBoundaries.add(line2);
                        break;
                    }
                }
            }

            shapeFun.values(qPoint, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);
            assemblier.asmDirichlet(qp, filteredNodes, shapeFunctions);
        }


    }

    void assemblyNeumann() {
        Iterable<BCQuadraturePoint> neumannIter = workProblem.neumannIterable(quadraturePower);
        if (null == neumannIter) {
            return;
        }

        LinkedList<Node> searchedNodes = new LinkedList<>();
        ArrayList<Node> filteredNodes = new ArrayList<>(arrayListSize);
        ArrayList<Boundary> searchedBoundaries = new ArrayList<>(arrayListSize);
        CenterSearcher<Coordinate, Node> nodeSearcher = workProblem.nodeSearcher();
        CenterSearcher<Coordinate, Boundary> boundarySearcher = workProblem.boundarySearcher();

        PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI()};
        shapeFun.setOrders(types);
        DenseVector[] shapeFunctions = new DenseVector[types.length];
        LineBoundary2D line1 = new LineBoundary2D(), line2 = new LineBoundary2D();

        for (BCQuadraturePoint qp : neumannIter) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundaryCondition.getBoundary();

            nodeSearcher.search(qPoint, searchedNodes);
            boundarySearcher.search(qPoint, searchedBoundaries);

            if (qPoint != bound.getPoint(0) && qPoint != bound.getPoint(bound.pointsSize() - 1)) {
                Iterator<Boundary> boundIter = searchedBoundaries.iterator();
                while (boundIter.hasNext()) {
                    if (bound == boundIter.next()) {
                        line1.rear = bound.getPoint(0);
                        line1.front = qp.coordinate;
                        line2.rear = qp.coordinate;
                        line2.front = bound.getPoint(bound.pointsSize() - 1);
                        searchedBoundaries.add(line1);
                        searchedBoundaries.add(line2);
                        break;
                    }
                }
            }

            shapeFun.values(qPoint, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);
            assemblier.asmNeumann(qp, filteredNodes, shapeFunctions);
        }

    }

    void solveEquation() {
        equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
    }
}
