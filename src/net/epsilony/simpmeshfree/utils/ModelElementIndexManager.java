/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilon
 */
public class ModelElementIndexManager {

    int max=0;

    int start=0;

    public int getNewIndex() {
        return ++max;
    }

    public int getMax(){
        return max;
    }

}
