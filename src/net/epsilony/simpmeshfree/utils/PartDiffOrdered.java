/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.simpmeshfree.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.sfun.WeightFunction;

/**
 * 用来设置将来将要计算返回的类型的接口。</br>
 * 通常的应用可见{@link WeightFunction}与{@link ShapeFunction}</br>。
 * @author epsilonyuan@gmail.com
 */
public interface PartDiffOrdered {

    /**
     * <p>设定此后相关输出的序列值为某函数的一系列偏微分</br>
     * </ul></p>
     * @param order 
     */
    void setDiffOrder(int order);
    
    int getDiffOrder();
}
