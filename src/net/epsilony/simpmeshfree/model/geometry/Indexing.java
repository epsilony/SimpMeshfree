/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.Comparator;

/**
 *
 * @author epsilon
 */
public interface Indexing {

    public static final Comparator<Indexing> indexComparator=new Comparator<Indexing>() {

        @Override
        public int compare(Indexing o1, Indexing o2) {
            return o1.getIndex()-o2.getIndex();
        }
    };
    

    int getIndex();

    void setIndex(int index);


}
