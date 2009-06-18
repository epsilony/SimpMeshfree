/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.geometry;

import java.util.List;

/**
 *
 * @author epsilon
 */
public interface BoundaryCondition {
    public enum BoundaryConditionType{
        Natural,Essential,ConNatural;
    }

    public final byte X=0x01;
    public final byte Y=0x02;
    public final byte XY=0x03;
    public final byte NOT=0x00;

    public BoundaryConditionType getType();

    /**
     *
     * @param t 参数曲线段上的参数
     * @param output double[2] 可以是{u<sub>x</sub>,u<sub>y</sub>},也可以是{t<sub>x</sub>,t<sub>y</sub>}
     * @return &X!=0x00:X方向上值被设置
     */
    public byte getValues(double t,double[] output);

    /**
     * 设置集中力载荷
     * @param output {[parm,tx,ty],... 其中parm为参数曲线段上的参数，tx,ty为集中力分量
     * @return
     */
    public List<double[]> getConNaturalValues(List<double[]> output);


}