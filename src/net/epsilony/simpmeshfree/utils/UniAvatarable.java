/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilon
 */
public interface UniAvatarable<A> extends Avatarable<A> {

    /**
     * 合并所有曾经产生的分身到本实例
     */
    void uniteAvators();
}