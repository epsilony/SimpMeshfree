/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.EquationSolver;
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
     *
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
        int sumDirichlet = workProblem.dirichletQuadraturePointsNum(quadraturePower);
        int sumNeumann = workProblem.neumannQudaraturePointsNum(quadraturePower);
        int sumBalance = workProblem.balanceQuadraturePointsNum(quadraturePower);
        for (int i = 0; i < aviCoreNum; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    ShapeFunction shapeFun = shapeFunFactory.factory();
                    WeakFormAssemblier assemblierAvator = assemblier.avatorInstance();

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
                logger.info(String.format("Assemblied: balance %d/%d, Dirichlet %d/%d, Neumann %d/%d", balanceCount.get(), sumBalance, dirichletCount.get(), sumDirichlet, neumannCount.get(), sumNeumann));
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
    AtomicInteger balanceCount = new AtomicInteger();

    void assemblyBalanceEquation(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
        VolumeCondition volumnBoundaryCondition = workProblem.getVolumeCondition();
        shapeFun.setOrder(1);
        DenseVector[] shapeFunVals = new DenseVector[3];
        QuadraturePoint qp = new QuadraturePoint();
        Coordinate qPoint = qp.coordinate;
        while (nextBalanceQuadraturePoint(qp)) {
            shapeFun.values(qPoint, null, shapeFunVals, shapeFunNodes);
            assemblierAvator.asmBalance(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition);
            balanceCount.incrementAndGet();
        }
    }
    ShapeFunction shapeFunction;

    public List<double[]> result(List<Coordinate> coords,List<Boundary> bnds) {

        if (null == shapeFunction) {
            shapeFunction = shapeFunFactory.factory();
        }
        shapeFunction.setOrder(1);
        LinkedList<double[]> results=new LinkedList<>();
        DenseVector[] shapeFunctions = new DenseVector[3];
        ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
        Iterator<Boundary> bndIter=(bnds!=null?bnds.iterator():null);
        for (Coordinate coord : coords) {
            Boundary bnd=(bndIter!=null?bndIter.next():null);


            shapeFunction.values(coord, bnd, shapeFunctions, shapeFunNodes);

            double[] result = new double[2];
            int nodeCount = 0;
            for (Node nd : shapeFunNodes) {
                int index = nd.id * 2;
                double shapeValue = shapeFunctions[0].get(nodeCount++);
                result[0] += shapeValue * equationResultVector.get(index);
                result[1] += shapeValue * equationResultVector.get(index + 1);
            }
            results.add(result);
        }
        return results;
    }
    AtomicInteger dirichletCount = new AtomicInteger();

    void assemblyNeumann(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {
        shapeFun.setOrder(0);
        DenseVector[] shapeFunVals = new DenseVector[3];
        BCQuadraturePoint qp = new BCQuadraturePoint();
        ArrayList<Node> shapeFunNds=new ArrayList<>(arrayListSize);
        while (nextNeumannQuadraturePoint(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundaryCondition.getBoundary();
            shapeFun.values(qPoint, bound,shapeFunVals, shapeFunNds);
            assemblierAvator.asmNeumann(qp, shapeFunNds, shapeFunVals);
            dirichletCount.incrementAndGet();
        }


    }
    AtomicInteger neumannCount = new AtomicInteger();

    void assemblyDirichlet(ShapeFunction shapeFun, WeakFormAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
        shapeFun.setOrder(0);
        DenseVector[] shapeFunctions = new DenseVector[3];
        BCQuadraturePoint qp = new BCQuadraturePoint();
        while (nextDirichletQuadraturePoint(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundaryCondition.getBoundary();
            shapeFun.values(qPoint, bound, shapeFunctions, shapeFunNds);
            assemblierAvator.asmDirichlet(qp, shapeFunNds, shapeFunctions);
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
