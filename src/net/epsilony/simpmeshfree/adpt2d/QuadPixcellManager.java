/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author epsilon
 */
public class QuadPixcellManager {
    List<QuadPixcell> pxes;
    AdaptiveFilter af;
    PriorityQueue<QuadPixcell> levelBigTile,levelSmallTile;

    public QuadPixcellManager(Collection<QuadPixcell> pxes, AdaptiveFilter af) {
        this.pxes = new LinkedList<>(pxes);
        this.af = af;
        levelBigTile=new PriorityQueue<>(pxes.size(), QuadPixcellTools.LEVEL_BIGGER_POSITIVE);
        levelSmallTile=new PriorityQueue<>(pxes.size(), QuadPixcellTools.LEVEL_SMALLER_POSITIVE);
    }
    
    
    public List<QuadPixcell> refine(boolean recursive){
        return QuadPixcellTools.refine(pxes, af, recursive, levelBigTile);
    }
    
    public List<QuadPixcell> merge(boolean recursive){
        return QuadPixcellTools.merge(pxes, af, recursive,levelSmallTile);
    }
    
    public void adapt(boolean recursive){
        refine(recursive);
        merge(recursive);
    }
}
