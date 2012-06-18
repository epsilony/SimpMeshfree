/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.simpmeshfree.utils.CountableQuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterators;
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
    CountableQuadraturePointIterator balanceIterator;
    CountableQuadraturePointIterator dirichletIterator;
    CountableQuadraturePointIterator neumannIterator;

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
        int aviCoreNum = Runtime.getRuntime().availableProcessors();
        process(aviCoreNum);
    }

    /**
     * 进行刚阵与广义力阵的组装
     *
     * @param coreNum 并行计算的线程数，如coreNum>availableProcessors()则实际线程数减为
     * availableProcessors()
     */
    public void process(int coreNum) {


        int[] numOut = new int[1];
        QuadraturePointIterator qpIter = workProblem.volumeIterator(numOut);
        balanceIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));

        qpIter = workProblem.neumannIterator(numOut);
        neumannIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));

        qpIter = workProblem.dirichletIterator(numOut);
        dirichletIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));

        ExecutorService executor = Executors.newFixedThreadPool(coreNum);

        logger.info(String.format("Start processing the weak form problem by %d parrel assembliers", coreNum));



        for (int i = 0; i < coreNum; i++) {
            executor.execute(new ProcessCore(i));
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                break;
            } finally {
                logger.info(String.format("Assemblied: balance %d/%d, Dirichlet %d/%d, Neumann %d/%d", balanceIterator.dispatchedNum(), balanceIterator.sunNum(), dirichletIterator.dispatchedNum(), dirichletIterator.sunNum(), neumannIterator.dispatchedNum(), neumannIterator.sunNum()));
            }
        }
        assemblier.uniteAvators();
    }

    class ProcessCore implements Runnable {

        int id;
        ShapeFunction shapeFunction;
        WeakformAssemblier weakformAssemblier;

        public int getId() {
            return id;
        }

        public ProcessCore(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            ShapeFunction shapeFun = shapeFunFactory.factory();
            WeakformAssemblier assemblierAvator = assemblier.avatorInstance();
            shapeFunction=shapeFun;
            weakformAssemblier=assemblierAvator;

            assemblyBalanceEquation(shapeFun, assemblierAvator);

            if (null != neumannIterator) {
                assemblyNeumann(shapeFun, assemblierAvator);
            }

            if (null != dirichletIterator) {
                assemblyDirichlet(shapeFun, assemblierAvator);
            }
        }
    }

    void assemblyBalanceEquation(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
        VolumeCondition volumnBoundaryCondition = workProblem.volumeCondition();
        shapeFun.setDiffOrder(1);
        QuadraturePoint qp = new QuadraturePoint();
        Coordinate qPoint = qp.coordinate;
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(1);
        while (balanceIterator.next(qp)) {
            shapeFun.values(qPoint, null, shapeFunVals, shapeFunNodes);
            assemblierAvator.asmBalance(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition);
        }
    }

    void assemblyNeumann(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {
        shapeFun.setDiffOrder(0);
        QuadraturePoint qp = new QuadraturePoint();
        ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
        while (neumannIterator.next(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
            assemblierAvator.asmNeumann(qp, shapeFunNds, shapeFunVals);
        }
    }

    void assemblyDirichlet(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator) {

        ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
        shapeFun.setDiffOrder(0);
        QuadraturePoint qp = new QuadraturePoint();
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
        while (dirichletIterator.next(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
            assemblierAvator.asmDirichlet(qp, shapeFunNds, shapeFunVals);
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

    public void solveEquation() {
        logger.info("Start solving equation");
        equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
        logger.info("Equation solved");
    }

    public DenseVector getNodesValue() {
        return equationResultVector;
    }
}
