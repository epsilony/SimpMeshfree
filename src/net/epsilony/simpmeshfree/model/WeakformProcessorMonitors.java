/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.epsilony.simpmeshfree.utils.CountableQuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class WeakformProcessorMonitors {

    public static class SimpLogger implements WeakformProcessorMonitor {

        Logger logger;
        private WeakformProcessor processor;
        int avatorNum;

        @Override
        public void avatorInited(WeakformAssemblier asm, ShapeFunction shapeFun, int asmId) {
            avatorNum++;
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
            logger.info(String.format("Start processing the weak form problem by %d parrel assembliers", coreNum));
            
        }
        
        @Override
        public void processStarted(ExecutorService executor){
            CountableQuadraturePointIterator balanceIterator=processor.balanceIterator;
            CountableQuadraturePointIterator dirichletIterator=processor.dirichletIterator;
            CountableQuadraturePointIterator neumannIterator=processor.neumannIterator;
            while (!executor.isTerminated()) {
                try {
                    executor.awaitTermination(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    break;
                } finally {
                    logger.info(String.format("Assemblied: balance %d/%d, Dirichlet %d/%d, Neumann %d/%d", balanceIterator.dispatchedNum(), balanceIterator.sunNum(), dirichletIterator.dispatchedNum(), dirichletIterator.sunNum(), neumannIterator.dispatchedNum(), neumannIterator.sunNum()));
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
        public WeakformProcessor getProcessor() {
            return processor;
        }

        @Override
        public void setProcessor(WeakformProcessor processor) {
            this.processor = processor;
            logger = Logger.getLogger(processor.getClass());
        }
    }
}
