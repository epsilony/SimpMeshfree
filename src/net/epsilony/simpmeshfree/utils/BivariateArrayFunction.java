/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface BivariateArrayFunction {
    double[] value(double x,double y,double[] result);
    int valueDimension();
}
