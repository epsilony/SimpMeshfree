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
import net.epsilony.simpmeshfree.sfun.ShapeFunctionPacker;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterators;
import net.epsilony.utils.SomeFactory;
import net.epsilony.utils.WithId;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Node;
import net.epsilony.math.EquationSolver;
import net.epsilony.spfun.InfluenceDomainSizer;
import net.epsilony.spfun.ShapeFunction;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakformProcessor {

    WeakformProcessorMonitor monitor;

    public WeakformProcessorMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(WeakformProcessorMonitor monitor) {
        this.monitor = monitor;
        monitor.setProcessor(this);
    }
    SomeFactory<ShapeFunction> shapeFunFactory;
    SomeFactory<SupportDomainCritierion> critierionFactory;
    SomeFactory<InfluenceDomainSizer> infDomainFactory;
    WeakformAssemblier assemblier;
    WeakformProblem workProblem;
    public EquationSolver equationSolver;
    DenseVector equationResultVector;
    public int processThreadsNum = Integer.MAX_VALUE;
    private int dim;
    QuadraturePointIterator balanceIterator;
    QuadraturePointIterator dirichletIterator;
    QuadraturePointIterator neumannIterator;
    public static final int MAX_SUPPORT_NODES_GUESS = 50;
    ArrayList<ProcessCore> processCores = new ArrayList<>(Runtime.getRuntime().availableProcessors());

    /**
     *
     * @param shapeFun
     * @param assemblierAvator
     * @param workProblem
     * @param power
     * @param equationSolver
     */
    public WeakformProcessor(SomeFactory<SupportDomainCritierion> critierionFactory, SomeFactory<InfluenceDomainSizer> infDomainFactory, SomeFactory<ShapeFunction> shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver) {
        this.critierionFactory = critierionFactory;
        this.infDomainFactory = infDomainFactory;
        this.shapeFunFactory = shapeFunFactory;
        this.assemblier = assemblier;
        this.workProblem = workProblem;
        this.equationSolver = equationSolver;
        setDim(2);
    }

    /**
     *
     * @param shapeFun
     * @param assemblierAvator
     * @param workProblem
     * @param power
     * @param equationSolver
     */
    public WeakformProcessor(SomeFactory<ShapeFunction> shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver, int dim) {
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

    public void process(int coreNum) {
        if (null == monitor) {
            monitor = new WeakformProcessorMonitors.SimpLogger();
            monitor.setProcessor(this);
        }


        QuadraturePointIterator qpIter = workProblem.volumeIterator();
        balanceIterator = (qpIter == null ? null : QuadraturePointIterators.synchronizedWrapper(qpIter));

        qpIter = workProblem.neumannIterator();
        neumannIterator = (qpIter == null ? null : QuadraturePointIterators.synchronizedWrapper(qpIter));

        qpIter = workProblem.dirichletIterator();
        dirichletIterator = (qpIter == null ? null : QuadraturePointIterators.synchronizedWrapper(qpIter));

        ExecutorService executor = Executors.newFixedThreadPool(coreNum);

        monitor.beforeProcess(executor, coreNum);
        processCores.clear();
        for (int i = 0; i < coreNum; i++) {
            processCores.add(new ProcessCore(i));
           
        }
        for (ProcessCore pc:processCores){
            executor.execute(pc);
        }
        executor.shutdown();

        monitor.processStarted(executor);

        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(100, TimeUnit.MICROSECONDS);
            } catch (InterruptedException ex) {
                break;
            }
        }

        for (ProcessCore processCore : processCores) {
            assemblier.uniteIn(processCore.getAssemblierAvator());
        }
        monitor.avatorUnited(assemblier);
    }

    public ArrayList<ProcessCore> getProcessCores() {
        return processCores;
    }
    
    

    public ShapeFunctionPacker genShapeFunctionPacker() {
        ShapeFunction shapeFun = shapeFunFactory.produce();

        InfluenceDomainSizer infDomainSizer = infDomainFactory.produce();
        SupportDomainCritierion critierion = critierionFactory.produce();

        return new ShapeFunctionPacker(shapeFun, critierion, infDomainSizer, dim, MAX_SUPPORT_NODES_GUESS);
    }

    public class ProcessCore implements Runnable, WithId {

        int id;
        WeakformAssemblier assemblierAvator;
        ShapeFunctionPacker shapeFunPacker;

        @Override
        public int getId() {
            return id;
        }

        public ProcessCore(int id) {
            this.id = id;
        }

        public WeakformAssemblier getAssemblierAvator() {
            return assemblierAvator;
        }

        public ShapeFunctionPacker getShapeFunPacker() {
            return shapeFunPacker;
        }

        @Override
        public void run() {
            shapeFunPacker = genShapeFunctionPacker();
            ShapeFunction shapeFun = shapeFunPacker.shapeFun;

            assemblierAvator = assemblier.produce();
            monitor.avatorInited(assemblierAvator, shapeFun, id);

            monitor.avatorInited(assemblierAvator, shapeFun, id);

            assemblyBalanceEquation(shapeFunPacker, assemblierAvator, id);

            if (null != neumannIterator) {
                assemblyNeumann(shapeFunPacker, assemblierAvator, id);
            }

            if (null != dirichletIterator) {
                assemblyDirichlet(shapeFunPacker, assemblierAvator, id);
            }
        }

        @Override
        public void setId(int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    void assemblyBalanceEquation(ShapeFunctionPacker shapeFunPacker, WeakformAssemblier assemblierAvator, int id) {


        VolumeCondition volumnBoundaryCondition = workProblem.volumeCondition();
        final int diffOrder = 1;
        shapeFunPacker.setDiffOrder(diffOrder);
        QuadraturePoint qp = new QuadraturePoint();

        ArrayList<Node> shapeFunNodes = new ArrayList<>(MAX_SUPPORT_NODES_GUESS);
        while (balanceIterator.next(qp)) {
            Coordinate qPoint = qp.coordinate;
            TDoubleArrayList[] shapeFunVals = shapeFunPacker.values(qPoint, null, shapeFunNodes);
            assemblierAvator.asmBalance(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition);
            monitor.balanceAsmed(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition, id);
        }
    }

    void assemblyNeumann(ShapeFunctionPacker shapeFunPacker, WeakformAssemblier assemblierAvator, int id) {
        final int diffOrder = 0;
        shapeFunPacker.setDiffOrder(diffOrder);
        QuadraturePoint qp = new QuadraturePoint();

        ArrayList<Node> shapeFunNodes = new ArrayList<>(MAX_SUPPORT_NODES_GUESS);
        while (neumannIterator.next(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            TDoubleArrayList[] shapeFunVals = shapeFunPacker.values(qPoint, bound, shapeFunNodes);
            assemblierAvator.asmNeumann(qp, shapeFunNodes, shapeFunVals);
            monitor.neumannAsmed(qp, shapeFunNodes, shapeFunVals, id);
        }
    }

    void assemblyDirichlet(ShapeFunctionPacker shapeFunPacker, WeakformAssemblier assemblierAvator, int id) {


        final int diffOrder = 0;
        shapeFunPacker.setDiffOrder(diffOrder);
        QuadraturePoint qp = new QuadraturePoint();


        ArrayList<Node> shapeFunNodes = new ArrayList<>(MAX_SUPPORT_NODES_GUESS);
        while (dirichletIterator.next(qp)) {
            Coordinate qPoint = qp.coordinate;
            Boundary bound = qp.boundary;
            TDoubleArrayList[] shapeFunVals = shapeFunPacker.values(qPoint, bound, shapeFunNodes);
            assemblierAvator.asmDirichlet(qp, shapeFunNodes, shapeFunVals);
            monitor.dirichletAsmed(qp, shapeFunNodes, shapeFunVals, id);
        }
    }

    private void setDim(int dim) {
        if (dim < 2 || dim > 3) {
            throw new IllegalArgumentException("The problem dimension should be 2D or 3D only, illegal dim: " + dim);
        }
        this.dim = dim;
    }

    public void solveEquation() {
        monitor.beforeEquationSolve();
        equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
        monitor.equationSolved();
    }

    public DenseVector getNodesValue() {
        return equationResultVector;
    }
}
