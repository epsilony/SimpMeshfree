/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.io.Serializable;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author epsilon
 */
public interface ModelCore extends Serializable{
    /**
     * 施加本质边界条件的内核
     * @param values 本质边界条件的数值，根据flag参数会有{ux uy} {u NaN} {NaN u} 等
     * @param index 该本质边界条件所对应的结点在矩阵中的排号，对于2维的位移边界，与本质边界条件有关的是第index*2，index*2+1 行与列，以及常数向量b的第index*2，index*2+1行
     * @param flag 参见{@link net.epsilony.simpmeshfree.model.geometry.BoundaryCondition} 的常量NOT，X，Y，XY
     * @param matrix 对称矩阵，对于平面力学就是“刚阵”K（KU＝b别告诉我看不懂）啦只需设置矩阵的上三角值
     * @param vector KU＝b的b啦
     */
    void essentialBoundaryConditionCore(double[] values, int index, byte flag, FlexCompRowMatrix matrix,DenseVector vector);
    
    /**
     * 对一个2维Gause积分域中的一个Gause上迭加刚阵，说白了就是有了形函数，和系数，要求把形函数和系数乘后加进刚阵中
     * @param mIndex 刚阵中有关的行的编号，对2维来说，有mIndex*2, mIndex*2+1 
     * @param dphim_dx
     * @param dphim_dy
     * @param nIndex 刚阵中有关列的编号，对于2维来说，具体的行有nIndex*2,nIndex*2+1;
     * @param dphin_dx
     * @param dphin_dy
     * @param coefs Gauss积分的系数，w<sub>ij{</sub>
     * @param matrix 刚阵啦
     */
    void quadrateCore(int mIndex, double dphim_dx, double dphim_dy, int nIndex, double dphin_dx, double dphin_dy, double coefs, FlexCompRowMatrix matrix);
    /**
     * 集中的自然边界条件加载核心
     * @param values
     * @param index
     * @param phi
     * @param vector
     */
    void natureConBoundaryQuadrateCore(double[] values, int index, double phi, DenseVector vector);
    void natureBoundaryQudarateCore(double[] values, int index, double phi, double coef, DenseVector vector);
     FlexCompRowMatrix initialKMatrix(int nodesSize);
}
