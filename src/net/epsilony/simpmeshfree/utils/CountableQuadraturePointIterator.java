/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilon
 */
public interface CountableQuadraturePointIterator extends QuadraturePointIterator{
    int sunNum();
    int dispatchedNum();
}
