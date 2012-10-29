/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.epsilony.simpmeshfree.sfun.DistanceSquareFunction;
import net.epsilony.simpmeshfree.sfun.DistanceSquareFunctions;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.sfun.WeightFunction;
import net.epsilony.simpmeshfree.sfun.WeightFunctionCore;
import net.epsilony.simpmeshfree.sfun.WeightFunctions;
import net.epsilony.simpmeshfree.sfun.wcores.TriSpline;
import net.epsilony.simpmeshfree.utils.BasesFunction;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.simpmeshfree.utils.Complete2DPolynomialBases;
import net.epsilony.simpmeshfree.utils.SomeFactory;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * 移动最小二乘法，没有通过白盒测试。测试样例证明了其的单位分解性和再生性。
 */
public class MLS implements ShapeFunction {

    private DenseMatrix64F A_bak;
    private LinearSolverLu luSolver;

    public MLS(WeightFunction weightFunction, BasesFunction basesFunction) {
        init(weightFunction, basesFunction);
    }
    private int diffOrder;
    WeightFunction weightFunction;
    BasesFunction basesFunction;
    TDoubleArrayList[] nodesWeightsCache;
    TDoubleArrayList[] distSquaresCache;
    DenseMatrix64F A;
    DenseMatrix64F A_x;
    DenseMatrix64F A_y;
    ArrayList<TDoubleArrayList> B;
    ArrayList<TDoubleArrayList> B_x;
    ArrayList<TDoubleArrayList> B_y;
    DenseMatrix64F p;
    DenseMatrix64F p_x;
    DenseMatrix64F p_y;
    DenseMatrix64F gamma;
    DenseMatrix64F gamma_x;
    DenseMatrix64F gamma_y;
    DenseMatrix64F[] As;
    List<ArrayList<TDoubleArrayList>> Bs;
    double[][] ps_arr;
    private DenseMatrix64F tv;
    public boolean areBasesRelative = true; // Commonly, for complete nth order polynomial,
    // Commonly, for complete nth order polynomial,
    DistanceSquareFunction euclideanDistSqFun = DistanceSquareFunctions.common(2);
    //just a mark variable to mark that relative base coordinate are using.

    @Override
    public void setDiffOrder(int partDiffOrder) {
        if (partDiffOrder < 0 || partDiffOrder >= 2) {
            throw new UnsupportedOperationException();
        }
        this.diffOrder = partDiffOrder;
        weightFunction.setDiffOrder(partDiffOrder);
        basesFunction.setDiffOrder(partDiffOrder);
        euclideanDistSqFun.setDiffOrder(partDiffOrder);
        nodesWeightsCache = new TDoubleArrayList[CommonUtils.len2DBase(partDiffOrder)];
        distSquaresCache = new TDoubleArrayList[nodesWeightsCache.length];
        for (int i = 0; i < nodesWeightsCache.length; i++) {
            nodesWeightsCache[i] = new TDoubleArrayList(ShapeFunctions2D.MAX_NODES_SIZE_GUESS);
            distSquaresCache[i] = new TDoubleArrayList(ShapeFunctions2D.MAX_NODES_SIZE_GUESS);
        }
    }

    @Override
    public int getDiffOrder() {
        return diffOrder;
    }

    private void init(WeightFunction weightFunction, BasesFunction baseFunction) {
        this.weightFunction = weightFunction;
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
                tB.add(new TDoubleArrayList(ShapeFunctions2D.MAX_NODES_SIZE_GUESS));
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

    @Override
    public TDoubleArrayList[] values(Coordinate pos, List<Node> nodes, TDoubleArrayList[] distSquares, TDoubleArrayList infRads, TDoubleArrayList[] results) {
        if (null == distSquares) {
            distSquares = this.distSquaresCache;
            euclideanDistSqFun.setPosition(pos);
            euclideanDistSqFun.sqValues(nodes, distSquares);
        }
        int ndsNum = nodes.size();
        weightFunction.values(distSquares, infRads, nodesWeightsCache);
        results = ShapeFunctions2D.init4Output(results, diffOrder, ndsNum);
        int diffDim = CommonUtils.len2DBase(diffOrder);
        int baseDim = basesFunction.getDim();
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
            TDoubleArrayList weights_d = nodesWeightsCache[diffDimIdx];
            DenseMatrix64F A_d = As[diffDimIdx];
            ArrayList<TDoubleArrayList> B_d = Bs.get(diffDimIdx);
            double[] tp = ps_arr[0];
            int ndIdx = 0;
            for (Coordinate nd : nodes) {
                if (areBasesRelative) {
                    GeometryMath.minus(nd, pos, radCoord);
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
            basesFunction.values(pos, ps_arr);
        }
        A_bak.set(A);
        luSolver.setA(A_bak);
        tv.set(p);
        luSolver.solve(tv, gamma);
        //            CommonOps.solve(A, p, gamma);
        ShapeFunctions2D.multAddTo(gamma, B, results[0]);
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
        ShapeFunctions2D.multAddTo(gamma_x, B, results[1]);
        ShapeFunctions2D.multAddTo(gamma, B_x, results[1]);
        ShapeFunctions2D.multAddTo(gamma_y, B, results[2]);
        ShapeFunctions2D.multAddTo(gamma, B_y, results[2]);
        return results;
    }

    public static SomeFactory<ShapeFunction> genFactory(SomeFactory<WeightFunctionCore> weightFunctionCoreFactory, int baseOrder) {
        SomeFactory<BasesFunction> basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(baseOrder);
        return genFactory(weightFunctionCoreFactory, basesFunctionFactory);
    }

    public static SomeFactory<ShapeFunction> genFactory(SomeFactory<WeightFunctionCore> weightFunctionCoreFactory, SomeFactory<BasesFunction> basesFunctionFactory) {
        return new MLSFactory(weightFunctionCoreFactory, basesFunctionFactory);
    }

    public static SomeFactory<ShapeFunction> genFactory(int baseOrder) {
        SomeFactory<WeightFunctionCore> weightFunctionCoreFactory = TriSpline.genFactory();
        SomeFactory<BasesFunction> basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(baseOrder);
        return genFactory(weightFunctionCoreFactory, basesFunctionFactory);
    }

    public static SomeFactory<ShapeFunction> genFactory() {
        final int DEFAULT_BASE_ORDER = 2;
        SomeFactory<WeightFunctionCore> weightFunctionCoreFactory = TriSpline.genFactory();
        SomeFactory<BasesFunction> basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(DEFAULT_BASE_ORDER);
        return genFactory(weightFunctionCoreFactory, basesFunctionFactory);
    }

    public static class MLSFactory implements SomeFactory<ShapeFunction> {

        SomeFactory<WeightFunctionCore> weightFunctionCoreFactory;
        SomeFactory<WeightFunction> weightFunctionFactory;
        SomeFactory<BasesFunction> basesFunctionFactory;

        public MLSFactory(SomeFactory<WeightFunctionCore> weightFunctionCoreFactory, SomeFactory<BasesFunction> basesFunctionFactory) {
            this.weightFunctionCoreFactory = weightFunctionCoreFactory;
            this.basesFunctionFactory = basesFunctionFactory;
            weightFunctionFactory = WeightFunctions.factory(weightFunctionCoreFactory);

        }

        public void setWeightFunctionCoreFactory(SomeFactory<WeightFunctionCore> weightFunctionCoreFactory) {
            this.weightFunctionCoreFactory = weightFunctionCoreFactory;
            weightFunctionFactory = WeightFunctions.factory(weightFunctionCoreFactory);
        }

        public void setComplete2DPolynomialBasesFactory(int baseOrder) {
            basesFunctionFactory = Complete2DPolynomialBases.basesFunctionFactory(baseOrder);
        }

        @Override
        public MLS produce() {
            return new MLS(weightFunctionFactory.produce(), basesFunctionFactory.produce());
        }
    }
}
