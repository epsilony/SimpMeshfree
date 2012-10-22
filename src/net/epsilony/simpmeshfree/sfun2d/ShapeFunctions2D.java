/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import org.ejml.data.DenseMatrix64F;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2D {

    public static int MAX_NODES_SIZE_GUESS = 50;

    public static TDoubleArrayList[] init4Output(int partDiffOrder) {
        return init4Output(null, partDiffOrder, MAX_NODES_SIZE_GUESS);
    }

    public static TDoubleArrayList[] init4Output(TDoubleArrayList[] result, int partDiffOrder, int ndsNum) {
        int partDim;
        switch (partDiffOrder) {
            case 0:
                partDim = 1;
                break;
            case 1:
                partDim = 3;
                break;
            default:
                throw new IllegalArgumentException("partDiffOrder must be 0 or 1 here, other hasn't been supported yet!");
        }
        if (null == result) {
            result = new TDoubleArrayList[partDim];
            for (int i = 0; i < result.length; i++) {
                result[i] = new TDoubleArrayList(ndsNum);
                result[i].fill(0, ndsNum, 0);
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i].resetQuick();
                result[i].ensureCapacity(ndsNum);
                result[i].fill(0, ndsNum, 0);
            }
        }
        return result;
    }

    public static void multAddTo(DenseMatrix64F gamma, ArrayList<TDoubleArrayList> B, TDoubleArrayList aim) {
        int dimI = B.get(0).size(), dimJ = gamma.numRows;
        for (int i = 0; i < dimI; i++) {
            double t = aim.getQuick(i);
            for (int j = 0; j < dimJ; j++) {
                t += gamma.unsafe_get(j, 0) * B.get(j).getQuick(i);
            }
            aim.setQuick(i, t);
        }
    }
}
