/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
            logger.info("Equation solved");
        }

        @Override
        public void setProcessor(WeakformProcessor processor) {
            super.setProcessor(processor);
            logger = Logger.getLogger(processor.getClass());
        }
    }

    public static WeakformProcessorMonitor recorder(){
        return new Recorder();
    }
    
    public static class Recorder extends Adapter {

        public List<List<List<QuadraturePoint>>> qpRecords;
        public List<List<List<List<Node>>>> supNdsRecords;
        public List<List<List<TDoubleArrayList[]>>> shapeFunValsRecords;
        public static final int BALANCE_IDX = 0, DIRICHLET_IDX = 1, NEUMANN_IDX = 2;

        @Override
        public void beforeProcess(ExecutorService executor, int coreNum) {
            super.beforeProcess(executor, coreNum);
            initRecords(coreNum);
        }

        private void initRecords(int coreNum) {
            qpRecords = new ArrayList<>(3);
            shapeFunValsRecords = new ArrayList<>(3);
            supNdsRecords = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                qpRecords.add(new ArrayList<List<QuadraturePoint>>(coreNum));
                shapeFunValsRecords.add(new ArrayList<List<TDoubleArrayList[]>>(coreNum));
                supNdsRecords.add(new ArrayList<List<List<Node>>>());
                for (int j = 0; j < coreNum; j++) {
                    qpRecords.get(i).add(new LinkedList<QuadraturePoint>());
                    shapeFunValsRecords.get(i).add(new LinkedList<TDoubleArrayList[]>());
                    supNdsRecords.get(i).add(new LinkedList<List<Node>>());
                }
            }
        }

        @Override
        public void balanceAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc, int asmId) {
            super.balanceAsmed(qp, nodes, shapeFunVals, volBc, asmId);
            List<QuadraturePoint> qpRecord = qpRecords.get(BALANCE_IDX).get(asmId);
            List<List<Node>> supNdsRecord = supNdsRecords.get(BALANCE_IDX).get(asmId);
            List<TDoubleArrayList[]> shapeFunRecord = shapeFunValsRecords.get(BALANCE_IDX).get(asmId);

            qpRecord.add(new QuadraturePoint(qp));
            supNdsRecord.add(new ArrayList<>(nodes));
            shapeFunRecord.add(CommonUtils.copyTDoubleArrayListArray(shapeFunVals));
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
