/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.epsilony.simpmeshfree.model.*;
import static net.epsilony.simpmeshfree.utils.CommonUtils.len2DBase;
import net.epsilony.simpmeshfree.utils.CoordinatePartDiffArrayFunction;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmDenseMatrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2D {

    public static int MAX_NODES_SIZE_ESTIMATION = 50;

    public static TDoubleArrayList[] initOutputResult(int partDiffOrder) {
        return initOutputResult(null, MAX_NODES_SIZE_ESTIMATION, partDiffOrder);
    }

    public static TDoubleArrayList[] initOutputResult(TDoubleArrayList[] result, int partDiffOrder, int ndsNum) {
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

    public static void multAddTo(DenseVector gamma, ArrayList<TDoubleArrayList> B, TDoubleArrayList aim) {
        int dimI = B.get(0).size(), dimJ = gamma.size();
        for (int i = 0; i < dimI; i++) {
            double t = aim.getQuick(i);
            for (int j = 0; j < dimJ; j++) {
                t += gamma.get(j) * B.get(j).getQuick(i);
            }
            aim.setQuick(i, t);
        }
    }

    /**
     * 移动最小二乘法，没有通过白盒测试。测试样例证明了其的单位分解性和再生性。
     */
    public static class MLS implements ShapeFunction {

        public MLS(WeightFunction weightFunction, CoordinatePartDiffArrayFunction baseFunction, SupportDomainCritierion criterion) {
            init(weightFunction, baseFunction, criterion);
        }
        private int diffOrder;
        WeightFunction weightFunction;
        CoordinatePartDiffArrayFunction baseFunction;
        SupportDomainCritierion criterion;
        TDoubleArrayList[] nodesWeights = new TDoubleArrayList[3];
        UpperSymmDenseMatrix A, A_x, A_y;
        ArrayList<TDoubleArrayList> B, B_x, B_y;
        DenseVector p, p_x, p_y, gamma, gamma_x, gamma_y;
        UpperSymmDenseMatrix[] As;
        List<ArrayList<TDoubleArrayList>> Bs;
        double[][] ps_arr;
        private DenseVector tv;

        @Override
        public TDoubleArrayList[] values(Coordinate center, Boundary centerBnd, TDoubleArrayList[] resultCache, ArrayList<Node> resNodes) {

            double supR = criterion.setCenter(center, centerBnd, resNodes);
            int ndsNum = resNodes.size();
            TDoubleArrayList[] results = initOutputResult(resultCache, diffOrder, ndsNum);
            int diffDim = len2DBase(diffOrder);
            int baseDim = baseFunction.getDim();

            weightFunction.values(resNodes, supR, nodesWeights);

            for (int i = 0; i < diffDim; i++) {
                As[i].zero();
                ArrayList<TDoubleArrayList> tB = Bs.get(i);
                for (int j = 0; j < baseDim; j++) {
                    TDoubleArrayList v = tB.get(j);
                    v.resetQuick();
                    v.ensureCapacity(ndsNum);
                    v.fill(0, ndsNum, 0);
                }
            }
            
            baseFunction.setDiffOrder(0);
            for (int diffDimIdx = 0; diffDimIdx < diffDim; diffDimIdx++) {
                TDoubleArrayList weights_d = nodesWeights[diffDimIdx];
                UpperSymmDenseMatrix A_d = As[diffDimIdx];
                ArrayList<TDoubleArrayList> B_d = Bs.get(diffDimIdx);
                double[] tp = ps_arr[0];
                int ndIdx = 0;
                for (Node nd : resNodes) {
                    baseFunction.values(nd, ps_arr);
                    double weight_d=weights_d.getQuick(ndIdx);
                    for (int i = 0; i < baseDim; i++) {
                        double p_i = tp[i];
                        for (int j = i; j < baseDim; j++) {
                            double p_ij = p_i * tp[j];
                            A_d.add(i, j, weight_d * p_ij);
                        }
                        B_d.get(i).set(ndIdx, weight_d * p_i);
                    }
                    ndIdx++;
                }

            }
            baseFunction.setDiffOrder(diffOrder);
            baseFunction.values(center, ps_arr);

            A.solve(p, gamma);
            multAddTo(gamma, B, results[0]);

            if (diffOrder < 1) {
                return results;
            }

            tv.zero();
            A_x.mult(gamma, tv);
            tv.scale(-1);
            tv.add(p_x);
            A.solve(tv, gamma_x);

            tv.zero();
            A_y.mult(gamma, tv);
            tv.scale(-1);
            tv.add(p_y);
            A.solve(tv, gamma_y);

            multAddTo(gamma_x, B, results[1]);
            multAddTo(gamma, B_x, results[1]);

            multAddTo(gamma_y, B, results[2]);
            multAddTo(gamma, B_y, results[2]);

            return results;
        }

        @Override
        public void setDiffOrder(int partDimOrder) {
            if (partDimOrder < 0 || partDimOrder >= 2) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = partDimOrder;
            weightFunction.setDiffOrder(partDimOrder);
            baseFunction.setDiffOrder(partDimOrder);
            nodesWeights = new TDoubleArrayList[len2DBase(partDimOrder)];
            for (int i = 0; i < nodesWeights.length; i++) {
                nodesWeights[i] = new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION);
            }
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        private void init(WeightFunction weightFunction, CoordinatePartDiffArrayFunction baseFunction, SupportDomainCritierion criterion) {
            this.weightFunction = weightFunction;
            this.criterion = criterion;
            this.baseFunction = baseFunction;
            final int baseDim = baseFunction.getDim();
            A = new UpperSymmDenseMatrix(baseDim);
            A_x = new UpperSymmDenseMatrix(baseDim);
            A_y = new UpperSymmDenseMatrix(baseDim);
            As = new UpperSymmDenseMatrix[]{A, A_x, A_y};

            B = new ArrayList<>(baseDim);
            B_x = new ArrayList<>(baseDim);
            B_y = new ArrayList<>(baseDim);
            Bs = Arrays.asList(B, B_x, B_y);

            for (ArrayList<TDoubleArrayList> tB : Bs) {
                for (int i = 0; i < baseDim; i++) {
                    tB.add(new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION));
                }
            }

            gamma = new DenseVector(baseDim);
            gamma_x = new DenseVector(baseDim);
            gamma_y = new DenseVector(baseDim);

            ps_arr = new double[3][baseDim];

            p = new DenseVector(ps_arr[0], false);
            p_x = new DenseVector(ps_arr[1], false);
            p_y = new DenseVector(ps_arr[2], false);

            tv = new DenseVector(baseDim);
        }
    }
}
