/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.WeightFunction;

/**
 * 用来设置将来将要计算返回的类型的接口。</br>
 * 通常的应用可见{@link WeightFunction}与{@link ShapeFunction}</br>。
 * {@link PartDiffOrd}可以有效的设定任意阶的偏微分阶数</br>
 * 常用的二维函数有关的偏微分计算对应的{@link PartDiffOrd}见{@link PartDiffOrd#ORI()}、{@link PartDiffOrd#X()}等
 * @see PartDiffOrd
 * @author epsilonyuan@gmail.com
 */
public interface PartDiffOrdSettable {

    /**
     * <p>设定此后相关输出的序列值为某函数的一系列偏微分</br>
     * </p>
     * <p><strong>注意</strong>
     * <ul>
     * <li>orders中元素的顺序决定了此后的相关输出的顺序</li>
     * <li>orders中不能有重复的元素</li>
     * </ul></p>
     * @param orders 
     */
    void setOrders(PartDiffOrd[] orders);
    
}
