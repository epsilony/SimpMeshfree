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
     * @param t
     * @param output
     * @return &X!=0x00:X方向上值被设置
     */
    public byte getValues(double t,double[] output);

    public List<double[]> getConNaturalValues(List<double[]> output);
}
