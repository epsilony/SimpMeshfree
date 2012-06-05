/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.EquationSolver;
import no.uib.cipr.matrix.DenseVector;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakformProcessor {

    ShapeFunctionFactory shapeFunFactory;
    public int arrayListSize = 100;
    WeakformAssemblier assemblier;
    WeakformProblem workProblem;
    public EquationSolver equationSolver;
    DenseVector equationResultVector;
    Logger logger = Logger.getLogger(this.getClass());
    public int processThreadsNum = Integer.MAX_VALUE;
    private int dim;

    /**
     *
     * @param shapeFun
     * @param assemblier
     * @param workProblem
     * @param power
     * @param equationSolver
     */
    public WeakformProcessor(ShapeFunctionFactory shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver) {
        this.shapeFunFactory = shapeFunFactory;
        this.assemblier = assemblier;
        this.workProblem = workProblem;
        this.equationSolver = equationSolver;
        setDim(2);
    }

    /**
     *
     * @param shapeFun
     * @param assemblier
     * @param workProblem
     * @param power
     * @param equationSolver
     */
    public WeakformProcessor(ShapeFunctionFactory shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver, int dim) {
        this.shapeFunFactory = shapeFunFactory;
        this.assemblier = assemblier;
        this.workProblem = workProblem;
        this.equationSolver = equationSolver;
        setDim(dim);
    }

    public void process() {
        process(processThreadsNum);
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

        int[] numOut = new int[1];
        balanceIterator = workProblem.volumeIterator(numOut);
        int sumBalance = numOut[0];
        neumannItrator = workProblem.neumannIterator(numOut);
        int sumNeumann = numOut[0];
        dirichletIterator = workProblem.dirichletIterator(numOut);
        int sumDirichlet = numOut[0];

        balanceCount.set(0);
        dirichletCount.set(0);
        neumannCount.set(0);

        ExecutorService executor = Executors.newFixedThreadPool(aviCoreNum);

        logger.info(String.format("Start processing the weak form problem by %d parrel assembliers", aviCoreNum));



        for (int i = 0; i < aviCoreNum; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    ShapeFunction shapeFun = shapeFunFactory.factory();
                    WeakformAssemblier assemblierAvator = assemblier.avatorInstance();

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
    final Object balanceLock = new Object();

    boolean nextBalanceQuadraturePoint(QuadraturePoint qp) {
        synchronized (balanceLock) {
            return balanceIterator.next(qp);
        }
    }
    
    QuadraturePointIterator balanceIterator;
    AtomicInteger balanceCount = new AtomicInteger();
    QuadraturePointIterator neumannItrator;
    final Object neumannLock = new Object();

    private boolean nextNeumannQuadraturePoint(QuadraturePoint qp) {
        synchronized (neumannLock) {
            return neumannItrator.next(qp);
        }
    }
    QuadraturePointIterator dirichletIterator;
    final Object dirichletLock = new Object();

    private boolean nextDirichletQuadraturePoint(QuadraturePoint qp) {
        synchronized (dirichletLock) {
            return dirichletIterator.next(qp);
        }
    }

    void assemblyBalanceEquation(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
        VolumeCondition volumnBoundaryCondition = workProblem.volumeCondition();
        shapeFun.setDiffOrder(1);
        QuadraturePoint qp = new QuadraturePoint();
        Coordinate qPoint = qp.coordinate;
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(1);
        while (nextBalanceQuadraturePoint(qp)) {
            shapeFun.values(qPoint, null, shapeFunVals, shapeFunNodes);
            assemblierAvator.asmBalance(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition);
            balanceCount.incrementAndGet();
        }
    }
    public AtomicInteger dirichletCount = new AtomicInteger();
    public AtomicInteger neumannCount = new AtomicInteger();

    void assemblyNeumann(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {
        shapeFun.setDiffOrder(0);
        QuadraturePoint qp = new QuadraturePoint();
        ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
        while (nextNeumannQuadraturePoint(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
            assemblierAvator.asmNeumann(qp, shapeFunNds, shapeFunVals);
            neumannCount.incrementAndGet();
        }
    }

    void assemblyDirichlet(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
        shapeFun.setDiffOrder(0);
        QuadraturePoint qp = new QuadraturePoint();
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
        while (nextDirichletQuadraturePoint(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
            assemblierAvator.asmDirichlet(qp, shapeFunNds, shapeFunVals);
            dirichletCount.incrementAndGet();
        }
    }

    private void setDim(int dim) {
        if (dim < 2 || dim > 3) {
            throw new IllegalArgumentException("The problem dimension should be 2D or 3D only, illegal dim: " + dim);
        }
        this.dim = dim;
    }

    private TDoubleArrayList[] initShapeFunVals(int diffOrder) {
        if (dim == 2) {
            return ShapeFunctions2D.initOutputResult(diffOrder);
        } else {
            //TODO 3Dissue
            throw new UnsupportedOperationException();
        }
    }
    ShapeFunction shapeFunction;

    public void solveEquation() {
        equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
    }

    public List<double[]> result(List<? extends Coordinate> coords, List<? extends Boundary> bnds) {

        if (null == shapeFunction) {
            shapeFunction = shapeFunFactory.factory();
        }
        shapeFunction.setDiffOrder(1);
        LinkedList<double[]> results = new LinkedList<>();
        ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
        Iterator<? extends Boundary> bndIter = (bnds != null ? bnds.iterator() : null);
        TDoubleArrayList[] shapeFunVals = ShapeFunctions2D.initOutputResult(1);
        for (Coordinate coord : coords) {
            Boundary bnd = (bndIter != null ? bndIter.next() : null);


            shapeFunction.values(coord, bnd, shapeFunVals, shapeFunNodes);

            double[] result = new double[2];
            int nodeCount = 0;
            for (Node nd : shapeFunNodes) {
                int index = nd.id * 2;
                double shapeValue = shapeFunVals[0].get(nodeCount++);
                result[0] += shapeValue * equationResultVector.get(index);
                result[1] += shapeValue * equationResultVector.get(index + 1);
            }
            results.add(result);
        }
        return results;
    }

    public DenseVector getNodesValue() {
        return equationResultVector;
    }
}
