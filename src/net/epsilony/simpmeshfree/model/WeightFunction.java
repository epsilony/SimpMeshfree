/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.simpmeshfree.utils.PartDiffOrdSettable;
import net.epsilony.utils.geom.Coordinate;

/**
 * 权函数接口，由{@link PartDiffOrdSettable#setOrders(net.epsilony.simpmeshfree.utils.PartDiffOrd[]) }指定{@link #values(net.epsilony.simpmeshfree.model.Node, net.epsilony.geom.Coordinate, double[]) }的返回值
 * @see PartDiffOrdSettable
 * @see PartDiffOrd
 * @author epsilonyuan@gmail.com
 */
public interface WeightFunction extends PartDiffOrdSettable{
    
    /**
     * 计算相对于node的权函数值或偏导数序列，该序列取决于{@link #setOrders(net.epsilony.simpmeshfree.utils.PartDiffOrd[]) }
     * @param node 结点，权函数一般从结点开始最大，越远离结点其值越小
     * @param point 计算点坐标
     * @param results 存放结果的数组，若其非null则其长度不得小于{@link #setOrders(net.epsilony.simpmeshfree.utils.PartDiffOrd[])  setOrders}的输入数组的长度
     * @return 如果results为null返回一个new double[]否则返回results
     */
    double[] values(Node node, Coordinate point, double supportRad, double[] results);
}
