/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.shapefun;

import java.util.List;
import net.epsilony.simpmeshfree.model.geometry.Node;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface ShapeFunction {

    /**
     * <br>计算点(x,y)的形函数偏导数向量</br>
     * <br> &part;&phi;<sub>1</sub>/&part;x,&part;&phi;<sub>2</sub>/&part;x,...,&part;&phi;<sub>2</sub>/&part;x</br>
     * <br> &part;&phi;<sub>1</sub>/&part;y,&part;&phi;<sub>2</sub>/&part;y,...,&part;&phi;<sub>2</sub>/&part;y</br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return <br>形函数向量组成的数组其值为：
     * <br>{&part;&phi;<sub>1</sub>/&part;x,&part;&phi;<sub>2</sub>/&part;x,...,&part;&phi;<sub>2</sub>/&part;x;</br>
     * <br> &part;&phi;<sub>1</sub>/&part;y,&part;&phi;<sub>2</sub>/&part;y,...,&part;&phi;<sub>2</sub>/&part;y}</br>
     */
    Vector[] shapePartialValues(List<Node> nodes, double x, double y);

    /**
     * <br>计算点(x,y)的形函数二阶偏导数向量</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;x&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&sup2;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&part;y,&part;&sup2;&phi;<sub>2</sub>/&part;x&part;y,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&part;y</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;y&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;y&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;y&sup2;</br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return <br>形函数向量组成的数组,其值为：</br>
     * <br> {&part;&sup2;&phi;<sub>1</sub>/&part;x&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;x&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&sup2;;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&part;y,&part;&sup2;&phi;<sub>2</sub>/&part;x&part;y,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&part;y;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;y&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;y&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;y&sup2;}</br>
     */
    Vector[] shapeQuadPartialValues(List<Node> nodes, double x, double y);

    /**
     * <br>计算点(x,y)的形函数向量:</br>
     * <br>&phi;<sub>1</sub>,&phi;<sub>2</sub>,...,&phi;<sub>k</sub></br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return 形函数向量其值为：{&phi;<sub>1</sub>,&phi;<sub>2</sub>,...,&phi;<sub>k</sub>}
     */
    Vector shapeValues(List<Node> nodes, double x, double y);

  
}
