/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import gnu.trove.list.array.TDoubleArrayList;
import net.epsilony.utils.PartDiffOrdered;

/**
 * 
 * @author epsilonyuan@gmail.com
 */
public interface WeightFunction extends PartDiffOrdered{
    
    /**
     * 计算相对于node的权函数值或偏导数序列，该序列取决于
     * @param nodes 结点，权函数一般从结点开始最大，越远离结点其值越小
     * @param supportRad 支持域半径
     * @param results 
     * @return 如果results为null返回一个new double[]否则返回results
     */
    TDoubleArrayList[] values(TDoubleArrayList[] DistsSqs,TDoubleArrayList rads, TDoubleArrayList[] results);
    
    double[] value(double[] distSq,double rad,double[] result);
    
}
