/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.simpmeshfree.model.WeakformProcessor;
import net.epsilony.simpmeshfree.model2d.WeakformAssembliers2D.SimpAssemblier;

/**
 * TODO translate to English
 * 该接口用于并行计算，类似于factory + 实例合并。一个实现的例子见{@link SimpAssemblier}，
 * 应用的例子见{@link WeakformProcessor#process(int) }的原代码
 * @param <A> 
 * @author epsilon
 */
public interface Avatarable <A> {
    /**
     * 获取一个分身，这个分身的所有操作必须是独立的，以便并行计算。该方法的实现必须是synchronzied
     * @return 
     */
    A avatorInstance();

}
