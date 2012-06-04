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
import net.epsilony.simpmeshfree.utils.BasesFunction;
import static net.epsilony.simpmeshfree.utils.CommonUtils.len2DBase;
import net.epsilony.utils.geom.Coordinate;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2D {

    public static int MAX_NODES_SIZE_ESTIMATION = 50;

    public static TDoubleArrayList[] initOutputResult(int partDiffOrder) {
        return initOutputResult(null, partDiffOrder, MAX_NODES_SIZE_ESTIMATION);
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

    /**
     * 移动最小二乘法，没有通过白盒测试。测试样例证明了其的单位分解性和再生性。
     */
    public static class MLS implements ShapeFunction {
        private DenseMatrix64F A_bak;
        private LinearSolverLu luSolver;

        public MLS(WeightFunction weightFunction, BasesFunction basesFunction, SupportDomainCritierion criterion) {
            init(weightFunction, basesFunction, criterion);
        }
        private int diffOrder;
        WeightFunction weightFunction;
        BasesFunction basesFunction;
        SupportDomainCritierion criterion;
        TDoubleArrayList[] nodesWeights = new TDoubleArrayList[3];
        DenseMatrix64F A, A_x, A_y;
        ArrayList<TDoubleArrayList> B, B_x, B_y;
        DenseMatrix64F p, p_x, p_y, gamma, gamma_x, gamma_y;
        DenseMatrix64F[] As;
        List<ArrayList<TDoubleArrayList>> Bs;
        double[][] ps_arr;
        private DenseMatrix64F tv;

        @Override
        public TDoubleArrayList[] values(Coordinate center, Boundary centerBnd, TDoubleArrayList[] shapeFunVals, ArrayList<Node> resNodes) {

            double supR = criterion.setCenter(center, centerBnd, resNodes);
            int ndsNum = resNodes.size();
            TDoubleArrayList[] results = initOutputResult(shapeFunVals, diffOrder, ndsNum);
            int diffDim = len2DBase(diffOrder);
            int baseDim = basesFunction.getDim();

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

            basesFunction.setDiffOrder(0);
            for (int diffDimIdx = 0; diffDimIdx < diffDim; diffDimIdx++) {
                TDoubleArrayList weights_d = nodesWeights[diffDimIdx];
                DenseMatrix64F A_d = As[diffDimIdx];
                ArrayList<TDoubleArrayList> B_d = Bs.get(diffDimIdx);
                double[] tp = ps_arr[0];
                int ndIdx = 0;
                for (Node nd : resNodes) {
                    basesFunction.values(nd, ps_arr);
                    double weight_d = weights_d.getQuick(ndIdx);
                    for (int i = 0; i < baseDim; i++) {
                        double p_i = tp[i];
                        for (int j = 0; j < baseDim; j++) {
                            double p_ij = p_i * tp[j];
                            A_d.add(i, j, weight_d * p_ij);
                        }
                        B_d.get(i).set(ndIdx, weight_d * p_i);
                    }
                    ndIdx++;
                }

            }
            basesFunction.setDiffOrder(diffOrder);
            basesFunction.values(center, ps_arr);
        
            A_bak.set(A);
            luSolver.setA(A_bak);

            tv.set(p);
            luSolver.solve(tv, gamma);

//            CommonOps.solve(A, p, gamma);

            multAddTo(gamma, B, results[0]);

            if (diffOrder < 1) {
                return results;
            }

            tv.zero();
            CommonOps.mult(-1, A_x, gamma, tv);
            CommonOps.add(p_x, tv, tv);
            luSolver.solve(tv, gamma_x);
//            CommonOps.solve(A, tv, gamma_x);
            tv.zero();
            CommonOps.mult(-1, A_y, gamma, tv);
            CommonOps.add(p_y, tv, tv);
            luSolver.solve(tv,gamma_y);
//            CommonOps.solve(A, tv, gamma_y);

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
            basesFunction.setDiffOrder(partDimOrder);
            nodesWeights = new TDoubleArrayList[len2DBase(partDimOrder)];
            for (int i = 0; i < nodesWeights.length; i++) {
                nodesWeights[i] = new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION);
            }
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        private void init(WeightFunction weightFunction, BasesFunction baseFunction, SupportDomainCritierion criterion) {
            this.weightFunction = weightFunction;
            this.criterion = criterion;
            this.basesFunction = baseFunction;
            final int baseDim = baseFunction.getDim();
            A = new DenseMatrix64F(baseDim, baseDim);
            A_x = new DenseMatrix64F(baseDim, baseDim);
            A_y = new DenseMatrix64F(baseDim, baseDim);
            As = new DenseMatrix64F[]{A, A_x, A_y};

            B = new ArrayList<>(baseDim);
            B_x = new ArrayList<>(baseDim);
            B_y = new ArrayList<>(baseDim);
            Bs = Arrays.asList(B, B_x, B_y);

            for (ArrayList<TDoubleArrayList> tB : Bs) {
                for (int i = 0; i < baseDim; i++) {
                    tB.add(new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION));
                }
            }

            gamma = new DenseMatrix64F(baseDim, 1);
            gamma_x = new DenseMatrix64F(baseDim, 1);
            gamma_y = new DenseMatrix64F(baseDim, 1);

            ps_arr = new double[3][baseDim];

            p = DenseMatrix64F.wrap(ps_arr[0].length, 1, ps_arr[0]);
            p_x = DenseMatrix64F.wrap(ps_arr[1].length, 1, ps_arr[1]);
            p_y = DenseMatrix64F.wrap(ps_arr[2].length, 1, ps_arr[2]);

            tv = new DenseMatrix64F(baseDim, 1);
            luSolver=new LinearSolverLu(new LUDecompositionAlt());
            A_bak= new DenseMatrix64F(baseDim,baseDim);
        }
    }
}
