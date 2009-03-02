/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
abstract public class Segment extends ModelElement{

    static ModelElementIndexManager segmentIM=new ModelElementIndexManager();
    @Override
    public ModelElementIndexManager getIndexManager() {
        return segmentIM;
    }
    protected Segment(){
        index=segmentIM.getNewIndex();
    }

}
