/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface FromToCalculator<COORD> {
    void calculate(COORD point,COORD from,COORD to);
    COORD coordinateFactory();
}
