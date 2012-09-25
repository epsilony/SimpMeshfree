/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author epsilon
 */
public interface AdaptiveFilter {
     boolean isNeedRefine(QuadPixcell px);
    boolean isNeedMerge(QuadPixcell px);
}
