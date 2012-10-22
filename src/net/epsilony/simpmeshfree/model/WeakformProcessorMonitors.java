/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.epsilony.simpmeshfree.model.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class WeakformProcessorMonitors {

    public static abstract class Adapter implements WeakformProcessorMonitor {

        protected WeakformProcessor processor;
        protected int coreNum;
        protected ExecutorService executor;

        @Override
        public void avatorInited(WeakformAssemblier asm, ShapeFunction shapeFun, int asmId) {
        }

        @Override
        public void balanceAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc, int asmId) {
        }

        @Override
        public void dirichletAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
        }

        @Override
        public void neumannAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
        }

        @Override
        public void beforeProcess(ExecutorService executor, int coreNum) {
            this.executor = executor;
            this.coreNum = coreNum;
        }

        @Override
        public void processStarted(ExecutorService executor) {
        }

        @Override
        public void avatorUnited(WeakformAssemblier asm) {
        }

        @Override
        public void beforeEquationSolve() {
        }

        @Override
        public void equationSolved() {
        }

        @Override
        public WeakformProcessor getProcessor() {
            return processor;
        }

        @Override
        public void setProcessor(WeakformProcessor processor) {
            this.processor = processor;
        }
    }

    public static WeakformProcessorMonitor simpLogger() {
        return new SimpLogger();
    }

    public static class SimpLogger extends Adapter {

        Logger logger;
        int avatorNum;

        @Override
        public void avatorInited(WeakformAssemblier asm, ShapeFunction shapeFun, int asmId) {
            avatorNum++;
        }

        @Override
        public void beforeProcess(ExecutorService executor, int coreNum) {
            logger.info(String.format("Start processing the weak form problem by %d parrel assembliers", coreNum));
            logger.info("Using "+processor.assemblier.getClass().getSimpleName());

        }

        @Override
        public void processStarted(ExecutorService executor) {
            QuadraturePointIterator balanceIterator = processor.balanceIterator;
            QuadraturePointIterator dirichletIterator = processor.dirichletIterator;
            QuadraturePointIterator neumannIterator = processor.neumannIterator;
            while (!executor.isTerminated()) {
                try {
                    executor.awaitTermination(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    break;
                } finally {
                    logger.info(String.format("Assemblied: balance %d/%d, Dirichlet %d/%d, Neumann %d/%d", balanceIterator.getDispatchedNum(), balanceIterator.getSumNum(), dirichletIterator.getDispatchedNum(), dirichletIterator.getSumNum(), neumannIterator.getDispatchedNum(), neumannIterator.getSumNum()));
                }
            }
        }

        @Override
        public void avatorUnited(WeakformAssemblier asm) {
            logger.info("Processing finished!");
        }

        @Override
        public void beforeEquationSolve() {
            logger.info("Start solving equation");
        }

        @Override
        public void equationSolved() {
            logger.info("Equation solved, using "+ processor.equationSolver);
        }

        @Override
        public void setProcessor(WeakformProcessor processor) {
            super.setProcessor(processor);
            logger = Logger.getLogger(processor.getClass());
        }
    }

    public static WeakformProcessorMonitor recorder() {
        return new Recorder();
    }

    public static class ShapeFunctionRecordNode implements Serializable{

        public QuadraturePoint qp;
        public ArrayList<Node> supNds;
        public TDoubleArrayList[] shapeFunVals;

        public final void set(QuadraturePoint qp, List<Node> supNds, TDoubleArrayList[] shapeFunVals) {
            this.qp = new QuadraturePoint(qp);
            this.supNds = new ArrayList<>(supNds);
            this.shapeFunVals = CommonUtils.copyTDoubleArrayListArray(shapeFunVals);
        }

        public ShapeFunctionRecordNode(QuadraturePoint qp, List<Node> supNds, TDoubleArrayList[] shapeFunVals) {
            set(qp, supNds, shapeFunVals);
        }
    }

    public static class Recorder extends Adapter {

        public ArrayList<ArrayList<List<ShapeFunctionRecordNode>>> shapeFunRecords = new ArrayList<>(3);
        public static final int BALANCE_IDX = 0, DIRICHLET_IDX = 1, NEUMANN_IDX = 2;

        @Override
        public void beforeProcess(ExecutorService executor, int coreNum) {
            super.beforeProcess(executor, coreNum);
            initRecords(coreNum);
        }

        private void initRecords(int coreNum) {
            for (int i = 0; i < 3; i++) {
                shapeFunRecords.add(new ArrayList<List<ShapeFunctionRecordNode>>(coreNum));
                for (int j = 0; j < coreNum; j++) {
                    shapeFunRecords.get(i).add(new LinkedList<ShapeFunctionRecordNode>());
                }
            }
        }

        @Override
        public void balanceAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc, int asmId) {
            super.balanceAsmed(qp, nodes, shapeFunVals, volBc, asmId);
            recordShapeFunction(BALANCE_IDX,asmId, qp, nodes, shapeFunVals);
        }

        @Override
        public void dirichletAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
            super.dirichletAsmed(qp, nodes, shapeFunVals, asmId);
            recordShapeFunction(DIRICHLET_IDX,asmId, qp, nodes, shapeFunVals);
        }

        @Override
        public void neumannAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
            super.neumannAsmed(qp, nodes, shapeFunVals, asmId);
            recordShapeFunction(NEUMANN_IDX,asmId, qp, nodes, shapeFunVals);
        }

        private void recordShapeFunction(int recordIdx,int asmId, QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals) {
            List<ShapeFunctionRecordNode> record = shapeFunRecords.get(recordIdx).get(asmId);
            record.add(new ShapeFunctionRecordNode(qp, nodes, shapeFunVals));
        }
    }

    public static WeakformProcessorMonitor compact(Collection<WeakformProcessorMonitor> monitors) {
        return new Compact(monitors);
    }

    public static class Compact implements WeakformProcessorMonitor {

        List<WeakformProcessorMonitor> monitors;

        public Compact(Collection<WeakformProcessorMonitor> monitors) {
            this.monitors = new ArrayList<>(monitors);
        }

        @Override
        public void avatorInited(WeakformAssemblier asm, ShapeFunction shapeFun, int asmId) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.avatorInited(asm, shapeFun, asmId);
            }
        }

        @Override
        public void balanceAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc, int asmId) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.balanceAsmed(qp, nodes, shapeFunVals, volBc, asmId);
            }
        }

        @Override
        public void dirichletAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.dirichletAsmed(qp, nodes, shapeFunVals, asmId);
            }
        }

        @Override
        public void neumannAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.neumannAsmed(qp, nodes, shapeFunVals, asmId);
            }
        }

        @Override
        public void beforeProcess(ExecutorService executor, int coreNum) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.beforeProcess(executor, coreNum);
            }
        }

        @Override
        public void processStarted(ExecutorService executor) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.processStarted(executor);
            }
        }

        @Override
        public void avatorUnited(WeakformAssemblier asm) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.avatorUnited(asm);
            }
        }

        @Override
        public void beforeEquationSolve() {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.beforeEquationSolve();
            }
        }

        @Override
        public void equationSolved() {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.equationSolved();
            }
        }

        @Override
        public WeakformProcessor getProcessor() {
            return monitors.get(0).getProcessor();
        }

        @Override
        public void setProcessor(WeakformProcessor processor) {
            for (WeakformProcessorMonitor monitor : monitors) {
                monitor.setProcessor(processor);
            }
        }
    }
}
