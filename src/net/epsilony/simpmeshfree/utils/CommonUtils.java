/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import gnu.trove.list.array.TDoubleArrayList;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import org.apache.commons.math.util.MathUtils;
import org.ejml.data.DenseMatrix64F;

/**
 *
 * @author epsilon
 */
public class CommonUtils {

    public static int len2DBase(int order) {
        return (order + 2) * (order + 1) / 2;
    }

    public static int len3DBase(int order) {
        int result = 0;
        for (int i = 0; i <= order; i++) {
            result += MathUtils.binomialCoefficient(2 + i, 2);
        }
        return result;
    }

    public static int lenBase(int dim, int order) {
        switch (dim) {
            case 2:
                return len2DBase(order);
            case 3:
                return len3DBase(order);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static DenseMatrix64F toDenseMatrix64F(Matrix mat) {
        DenseMatrix64F result = new DenseMatrix64F(mat.numRows(), mat.numColumns());
        for (MatrixEntry me : mat) {
            result.set(me.row(), me.column(), me.get());
        }
        return result;
    }

    public static TDoubleArrayList[] copyTDoubleArrayListArray(TDoubleArrayList[] ori) {
        TDoubleArrayList[] res = new TDoubleArrayList[ori.length];
        for (int i = 0; i < ori.length; i++) {
            TDoubleArrayList val = ori[i];
            if (val == null) {
                res[i] = null;
            } else {
                res[i] = new TDoubleArrayList(val);
            }
        }
        return res;
    }
}
