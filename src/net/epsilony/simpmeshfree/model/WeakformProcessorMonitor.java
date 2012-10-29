/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import net.epsilony.simpmeshfree.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;

/**
 *
 * @author epsilon
 */
public interface WeakformProcessorMonitor {

    void avatorInited(WeakformAssemblier asm, ShapeFunction shapeFun, int asmId);

    void balanceAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc, int asmId);

    void dirichletAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId);

    void neumannAsmed(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, int asmId);
    
    void beforeProcess(ExecutorService executor,int coreNum);
    
    void processStarted(ExecutorService executor);
    
    void avatorUnited(WeakformAssemblier asm);
    
    void beforeEquationSolve();
    
    void equationSolved();
    
    WeakformProcessor getProcessor();
    
    void setProcessor(WeakformProcessor processor);
}
