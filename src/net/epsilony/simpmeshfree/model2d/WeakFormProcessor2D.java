/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.epsilony.geom.Coordinate;
import net.epsilony.geom.GeometryMath;
import net.epsilony.math.util.EquationSolver;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.ShapeFunctionFactory;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakFormAssemblier;
import net.epsilony.simpmeshfree.model.WeakFormProblem;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.util.CenterSearcher;
import no.uib.cipr.matrix.DenseVector;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakFormProcessor2D {

    ShapeFunctionFactory shapeFunFactory;
    public int arrayListSize = 100;
    WeakFormAssemblier assemblier;
    WeakFormProblem workProblem;
    int quadraturePower;
    private final EquationSolver equationSolver;
    DenseVector equationResultVector;
    Logger logger = Logger.getLogger(this.getClass());

    /**
     * 
     * @param shapeFun
     * @param assemblier
     * @param workProblem
     * @param power
     * @param equationSolver 
     */
    public WeakFormProcessor2D(ShapeFunctionFactory shapeFunFactory, WeakFormAssemblier assemblier, WeakFormProblem workProblem, int power, EquationSolver equationSolver) {
        this.shapeFunFactory = shapeFunFactory;
        this.assemblier = assemblier;
        this.workProblem = workProblem;
        this.quadraturePower = power;
        this.equationSolver = equationSolver;
    }

    public void process() {
//        process(1);
        process(Integer.MAX_VALUE);
    }

    /**
     * 进行刚阵与广义力阵的组装
     * @param coreNum 并行计算的线程数，如coreNum>availableProcessors()则实际线程数减为
     * availableProcessors()
     */
    public void process(int coreNum) {
        int aviCoreNum = Runtime.getRuntime().availableProcessors();
        aviCoreNum = (coreNum > aviCoreNum ? aviCoreNum : coreNum);

        balanceIterator = workProblem.volumeIterable(quadraturePower).iterator();
        neumannItrator = workProblem.neumannIterable(quadraturePower).iterator();
        dirichletIterator = workProblem.dirichletIterable(quadraturePower).iterator();
        ExecutorService executor = Executors.newFixedThreadPool(aviCoreNum);

        logger.info(String.format("Start processing the weak form problem by %d parrel assembliers", aviCoreNum));

        balanceCount.set(0);
        dirichletCount.set(0);
        neumannCount.set(0);
        int sumDirichlet=workProblem.dirichletQuadraturePointsNum(quadraturePower);
        int sumNeumann=workProblem.neumannQudaraturePointsNum(quadraturePower);
        int sumBalance=workProblem.balanceQuadraturePointsNum(quadraturePower);
        for (int i = 0; i < aviCoreNum; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    ShapeFunction shapeFun = shapeFunFactory.factory();
                    WeakFormAssemblier assemblierAvator = assemblier.divisionInstance();

                    assemblyBalanceEquation(shapeFun, assemblierAvator);

                    if (null != neumannItrator) {
                        assemblyNeumann(shapeFun, assemblierAvator);
                    }

                    if (null != dirichletIterator) {
                        assemblyDirichlet(shapeFun, assemblierAvator);
                    }
                }
            });
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                break;
            } finally {
                logger.info(String.format("Assemblied: balance %d/%d, Dirichlet %d/%d, Neumann %d/%d", balanceCount.get(),sumBalance,dirichletCount.get(),sumDirichlet,neumannCount.get(),sumNeumann));
            }
        }

        assemblier.uniteAvators();
    }

    public DenseVector getNodesResult() {
        return equationResultVector;
    }
    final Object balanceLock = new Object();

    boolean nextBalanceQuadraturePoint(QuadraturePoint qp) {
        synchronized (balanceLock) {
            if (balanceIterator.hasNext()) {
                QuadraturePoint oriQp = balanceIterator.next();
                Coordinate oriCoord = oriQp.coordinate;
                Coordinate coord = qp.coordinate;
                coord.x = oriCoord.x;
                coord.y = oriCoord.y;
                qp.weight = oriQp.weight;
                return true;
            } else {
                return false;
            }
        }
    }
    Iterator<QuadraturePoint> balanceIterator;

    AtomicInteger balanceCount=new AtomicInteger();
    void assemblyBalanceEquation(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {

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
        QuadraturePoint qp = new QuadraturePoint();
        Coordinate qPoint = qp.coordinate;
        while (nextBalanceQuadraturePoint(qp)) {
            nodeSearcher.search(qPoint, searchedNodes);
            boundarySearcher.search(qPoint, searchedBoundaries);
            shapeFun.values(qPoint, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);
            assemblierAvator.asmBalance(qp, filteredNodes, shapeFunctions, volumnBoundaryCondition);
            balanceCount.incrementAndGet();
        }
    }
    ShapeFunction shapeFunction;

    public List<double[]> result(Iterable<Coordinate> coords) {
        LinkedList<Node> searchedNodes = new LinkedList<>();
        LinkedList<Node> filteredNodes = new LinkedList<>();
        LinkedList<Boundary> searchedBoundaries = new LinkedList<>();
        CenterSearcher<Coordinate, Node> nodeSearcher = workProblem.nodeSearcher();
        CenterSearcher<Coordinate, Boundary> boundarySearcher = workProblem.boundarySearcher();
        PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()};

        if (null == shapeFunction) {
            shapeFunction = shapeFunFactory.factory();
        }
        shapeFunction.setOrders(types);

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

            shapeFunction.values(coord, searchedNodes, searchedBoundaries, shapeFunctions, filteredNodes);

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

    AtomicInteger dirichletCount=new AtomicInteger();
    void assemblyNeumann(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {
        Iterable<BCQuadraturePoint> dirichletIter = workProblem.neumannIterable(quadraturePower);
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

        BCQuadraturePoint qp = new BCQuadraturePoint();

        while (nextNeumannQuadraturePoint(qp)) {
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
            assemblierAvator.asmNeumann(qp, filteredNodes, shapeFunctions);
            dirichletCount.incrementAndGet();
        }


    }

    AtomicInteger neumannCount=new AtomicInteger();
    void assemblyDirichlet(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {
        Iterable<BCQuadraturePoint> neumannIter = workProblem.dirichletIterable(quadraturePower);
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
        BCQuadraturePoint qp = new BCQuadraturePoint();
        while (nextDirichletQuadraturePoint(qp)) {
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
            assemblierAvator.asmDirichlet(qp, filteredNodes, shapeFunctions);
            neumannCount.incrementAndGet();
        }

    }

    void solveEquation() {
        equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
    }
    Iterator<BCQuadraturePoint> neumannItrator;
    final Object neumannLock = new Object();

    private boolean nextNeumannQuadraturePoint(BCQuadraturePoint qp) {
        synchronized (neumannLock) {
            if (neumannItrator.hasNext()) {
                BCQuadraturePoint oriQp = neumannItrator.next();
                Coordinate coord = qp.coordinate;
                Coordinate oriCoord = oriQp.coordinate;
                coord.x = oriCoord.x;
                coord.y = oriCoord.y;
                Coordinate par = qp.parameter;
                Coordinate oriPar = oriQp.parameter;
                par.x = oriPar.x;
                qp.weight = oriQp.weight;
                qp.boundaryCondition = oriQp.boundaryCondition;
                return true;
            } else {
                return false;
            }
        }
    }
    Iterator<BCQuadraturePoint> dirichletIterator;
    final Object dirichletLock = new Object();

    private boolean nextDirichletQuadraturePoint(BCQuadraturePoint qp) {
        synchronized (dirichletLock) {
            if (dirichletIterator.hasNext()) {
                BCQuadraturePoint oriQp = dirichletIterator.next();
                Coordinate coord = qp.coordinate;
                Coordinate oriCoord = oriQp.coordinate;
                coord.x = oriCoord.x;
                coord.y = oriCoord.y;
                Coordinate par = qp.parameter;
                Coordinate oriPar = oriQp.parameter;
                par.x = oriPar.x;
                qp.weight = oriQp.weight;
                qp.boundaryCondition = oriQp.boundaryCondition;
                return true;
            } else {
                return false;
            }
        }
    }
}