/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author epsilon
 */
public class QuadPixcellTools {

    public static List<QuadPixcell> refine(Collection<QuadPixcell> pxes, AdaptiveFilter af, boolean recursive) {
        return refine(pxes, af, recursive, null);
    }

    public static List<QuadPixcell> refine(Collection<QuadPixcell> pxes, AdaptiveFilter af, boolean recursive, PriorityQueue<QuadPixcell> pqCache) {
        PriorityQueue<QuadPixcell> pq;
        if (null == pqCache) {
            pq = new PriorityQueue<>(pxes.size(), LEVEL_BIGGER_POSITIVE);
        } else {
            pq = pqCache;
            pq.clear();
        }
        for (QuadPixcell px : pxes) {
            if (af.isNeedRefine(px)) {
                pq.add(px);
            }
        }

        LinkedList<QuadPixcell> newPxes = new LinkedList<>();
        while (!pq.isEmpty()) {
            QuadPixcell px = pq.peek();

            //Check weather some neighbours should refine firstly.
            boolean needContinue = false;
            for (int i = 0; i < px.neighbours.length; i++) {
                QuadPixcell nb = px.neighbours[i];
                if (nb != null && nb.level < px.level) {
                    pq.add(nb);
                    needContinue = true;
                }
            }
            if (needContinue) {
                continue;
            }
            QuadPixcell[] refined = px.refine();
            pq.remove();

            for (int i = 0; i < refined.length; i++) {
                newPxes.add(refined[i]);
            }

            for (int i = 1; i < refined.length; i++) {
                pxes.add(refined[i]);
            }

            if (recursive) {
                for (int i = 0; i < refined.length; i++) {
                    QuadPixcell refedPx = refined[i];
                    if (af.isNeedRefine(refedPx)) {
                        pq.add(refedPx);
                    }
                }
            }
        }

        return newPxes;
    }

    public static List<QuadPixcell> merge(Collection<QuadPixcell> pxes, AdaptiveFilter af, boolean recursive) {
        return merge(pxes, af, recursive, null);
    }

    public static List<QuadPixcell> merge(Collection<QuadPixcell> pxes, AdaptiveFilter af, boolean recursive, PriorityQueue<QuadPixcell> pqCache) {
        PriorityQueue<QuadPixcell> pq;
        if (null == pqCache) {
            pq = new PriorityQueue<>(pxes.size(), LEVEL_SMALLER_POSITIVE);
        } else {
            pq = pqCache;
            pq.clear();
        }
        for (QuadPixcell px : pxes) {
            if (px.branchDepth > 0 && af.isNeedMerge(px)) {
                pq.add(px);
            }
        }

        LinkedList<QuadPixcell> mergedPxes = new LinkedList<>();

        while (!pq.isEmpty()) {
            QuadPixcell px = pq.poll();
            boolean canMerge = true;
            QuadPixcell[] slibings = px.slibings();
            for (int i = 0; i < slibings.length; i++) {
                if (!af.isNeedMerge(slibings[i])) {
                    canMerge = false;
                    break;
                }
                if (!recursive && mergedPxes.contains(slibings[i])) {
                    canMerge = false;
                    break;
                }
                QuadPixcell slibNb = slibings[i].neighbours[i];
                if (slibNb != null) {
                    if (slibNb.level > px.level) {
                        canMerge = false;
                        break;
                    }
                    QuadPixcell slibNb_left=slibNb.neighbours[(i+1)%4];
                    if(slibNb_left!=null&&slibNb_left.level>px.level){
                        canMerge =false;
                        break;
                    }
                }
            }
            if (!canMerge) {
                continue;
            }

            QuadPixcell[] merged = px.merge();

            for (int i = 0; i < merged.length; i++) {
                mergedPxes.add(merged[i]);
            }

            for (int i = 1; i < merged.length; i++) {
                pxes.remove(merged[i]);
            }


            if (recursive && px.branchDepth > 0 && af.isNeedMerge(px)) {
                pq.add(px);
            }
        }

        return mergedPxes;
    }

    public static Comparator<QuadPixcell> levelValueBiggerPositive() {
        return new Comparator<QuadPixcell>() {
            @Override
            public int compare(QuadPixcell o1, QuadPixcell o2) {
                if (o1.level > o2.level) {
                    return 1;
                } else if (o1.level == o2.level) {
                    return 0;
                } else {
                    return -1;
                }
            }
        };
    }

    public static Comparator<QuadPixcell> levelValueSmallerPositive() {
        return new Comparator<QuadPixcell>() {
            @Override
            public int compare(QuadPixcell o1, QuadPixcell o2) {
                if (o1.level < o2.level) {
                    return 1;
                } else if (o1.level == o2.level) {
                    return 0;
                } else {
                    return -1;
                }
            }
        };
    }
    public static final Comparator<QuadPixcell> LEVEL_SMALLER_POSITIVE = levelValueSmallerPositive();
    public static final Comparator<QuadPixcell> LEVEL_BIGGER_POSITIVE = levelValueBiggerPositive();
}
