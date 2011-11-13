/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public interface WeightFunction<COORD> {

    void setPDTypes(PartialDiffType[] types);

    double[] values(Node<COORD> node, COORD point, double[] results);
}
