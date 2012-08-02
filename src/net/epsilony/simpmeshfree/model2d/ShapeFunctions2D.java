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
import net.epsilony.simpmeshfree.utils.Complete2DPolynomialBases;
import net.epsilony.simpmeshfree.utils.SomeFactory;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
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

    public static MLSFactory createMLSFactory(final GeomUtils gu, int minNodesNumOfInfluenceDomain, double influenceDomainInitSearchRadiu) {
        final InfluenceDomainSizer infSizer = new InfluenceDomainSizers.Array(gu.allNodes, gu.new NearestKVisibleDomainSizer(minNodesNumOfInfluenceDomain, influenceDomainInitSearchRadiu));
        SomeFactory<SupportDomainCritierion> critierionFactory = new SomeFactory<SupportDomainCritierion>() {
            @Override
            public SupportDomainCritierion produce() {
                return gu.new VisibleCritieron(infSizer);
            }
        };
        return new MLSFactory(infSizer, critierionFactory);
    }

    public static class MLSFactory implements SomeFactory<ShapeFunction> {

        SomeFactory<WeightFunctionCore> weightFunctionCoreFactory;
        SomeFactory<WeightFunction> weightFunctionFactory;
        SomeFactory<BasesFunction> basesFunctionFactory;
        InfluenceDomainSizer influenceDomainSizer;
        SomeFactory<SupportDomainCritierion> supportDomainCritierionFactory;
        public static final int DEFAULT_BASE_ORDER = 2;

        public MLSFactory(InfluenceDomainSizer influenceDomainSizer, SomeFactory<SupportDomainCritierion> supportDomainCritierionFactory) {

            weightFunctionCoreFactory = WeightFunctionCores.triSplineFactory();
            weightFunctionFactory = WeightFunctions.factory(weightFunctionCoreFactory);
            basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(DEFAULT_BASE_ORDER);
            this.influenceDomainSizer = influenceDomainSizer;
            this.supportDomainCritierionFactory = supportDomainCritierionFactory;

        }

        public void setWeightFunctionCoreFactory(SomeFactory<WeightFunctionCore> weightFunctionCoreFactory) {
            this.weightFunctionCoreFactory = weightFunctionCoreFactory;
            weightFunctionFactory = WeightFunctions.factory(weightFunctionCoreFactory);
        }

        public void setComplete2DPolynomialBasesFactory(int baseOrder) {
            basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(baseOrder);
        }

        public void setInfluenceDomainSizer(InfluenceDomainSizer influenceDomainSizer) {
            this.influenceDomainSizer = influenceDomainSizer;
        }

        public void setSupportDomainCritierionFactory(SomeFactory<SupportDomainCritierion> supportDomainCritierionFactory) {
            this.supportDomainCritierionFactory = supportDomainCritierionFactory;
        }

        @Override
        public MLS produce() {
            return new MLS(weightFunctionFactory.produce(), basesFunctionFactory.produce(), influenceDomainSizer, supportDomainCritierionFactory.produce());
        }
    }

    /**
     * 移动最小二乘法，没有通过白盒测试。测试样例证明了其的单位分解性和再生性。
     */
    public static class MLS implements ShapeFunction {

        private DenseMatrix64F A_bak;
        private LinearSolverLu luSolver;
        private InfluenceDomainSizer infSizer;
        private TDoubleArrayList infRads;

        public MLS(WeightFunction weightFunction, BasesFunction basesFunction, InfluenceDomainSizer infSizer, SupportDomainCritierion criterion) {
            init(weightFunction, basesFunction, infSizer, criterion);
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
        public boolean areBasesRelative = true;  // Commonly, for complete nth order polynomial,
        //just a mark variable to mark that relative base coordinate are using.

        @Override
        public TDoubleArrayList[] values(Coordinate center, Boundary centerBnd, TDoubleArrayList[] shapeFunVals, ArrayList<Node> resNodes) {

            TDoubleArrayList[] distSqs = nodesWeights;
            criterion.getSupports(center, centerBnd, resNodes, distSqs);
            int ndsNum = resNodes.size();
            TDoubleArrayList[] results = initOutputResult(shapeFunVals, diffOrder, ndsNum);
            int diffDim = len2DBase(diffOrder);
            int baseDim = basesFunction.getDim();

            InfluenceDomainSizers.getInfRadius(resNodes, infSizer, infRads);
            weightFunction.values(distSqs, infRads, nodesWeights);
            Coordinate zero = new Coordinate(0, 0, 0);
            Coordinate radCoord = new Coordinate(0, 0, 0);

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
                    if (areBasesRelative) {
                        GeometryMath.minus(nd, center, radCoord);
                        basesFunction.values(radCoord, ps_arr);
                    } else {
                        basesFunction.values(nd, ps_arr);
                    }
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
            if (areBasesRelative) {
                basesFunction.values(zero, ps_arr);
            } else {
                basesFunction.values(center, ps_arr);
            }

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
            luSolver.solve(tv, gamma_y);
//            CommonOps.solve(A, tv, gamma_y);

            multAddTo(gamma_x, B, results[1]);
            multAddTo(gamma, B_x, results[1]);

            multAddTo(gamma_y, B, results[2]);
            multAddTo(gamma, B_y, results[2]);

            return results;
        }

        @Override
        public void setDiffOrder(int partDiffOrder) {
            if (partDiffOrder < 0 || partDiffOrder >= 2) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = partDiffOrder;
            weightFunction.setDiffOrder(partDiffOrder);
            basesFunction.setDiffOrder(partDiffOrder);
            criterion.setDiffOrder(partDiffOrder);
            nodesWeights = new TDoubleArrayList[len2DBase(partDiffOrder)];
            for (int i = 0; i < nodesWeights.length; i++) {
                nodesWeights[i] = new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION);
            }
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        private void init(WeightFunction weightFunction, BasesFunction baseFunction, InfluenceDomainSizer infSizer, SupportDomainCritierion criterion) {
            this.weightFunction = weightFunction;
            this.criterion = criterion;
            this.basesFunction = baseFunction;
            this.infSizer = infSizer;
            infRads = new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION);
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
            luSolver = new LinearSolverLu(new LUDecompositionAlt());
            A_bak = new DenseMatrix64F(baseDim, baseDim);
        }
    }
}
