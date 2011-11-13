/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Collection;
import java.util.List;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.PartialDiffType;
import net.epsilony.simpmeshfree.model.WeightFunction;
import net.epsilony.simpmeshfree.utils.BivariateArrayFunction;
import no.uib.cipr.matrix.DenseCholesky;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSPDDenseMatrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public class ShapeFunctions2D {

    public static class MLS implements ShapeFunction<Coordinate2D> {

        public MLS(WeightFunction<Coordinate2D> weightFunction, BivariateArrayFunction[] baseFunctions) {
            setWeightFunction(weightFunction);
            setBaseFunctions(baseFunctions);
        }
        WeightFunction<Coordinate2D> weightFunction;       // [权函数 作x偏微分 作y偏微分]
        double[] weights = new double[3];                   //用于承载权函数s的返回信息
        /**
         * [0]一般是order阶完备多项式的基函数  =  [1 x y x^2 xy y^2 ...
         * [1]:偏x
         * [2]:偏y
         * [3]:偏xx
         * [4]:偏xy
         * [5]:偏yy
         * [6]:偏xxx
         * ...
         */
        BivariateArrayFunction[] baseFunctions;
        int baseValueDim;                //基函数的基数，即其返回的数组的有效长度
        Cache cache = new Cache();

        void setCacheRange(int minSize, int maxSize) {
            cache.setCacheRange(minSize, maxSize);
        }

        static class Cache {

            void setValueDimension(int dimension) {
                this.valueDim = dimension;
                matA = new UpperSPDDenseMatrix(dimension);
                matAx = new UpperSPDDenseMatrix(dimension);
                matAy = new UpperSPDDenseMatrix(dimension);
                decomp = new DenseCholesky(dimension, true);
                pArr = new double[dimension];
                pXArr = new double[dimension];
                pYArr = new double[dimension];
                pVec = new DenseVector(pArr, false);
                pXVec = new DenseVector(pXArr, false);
                pYVec = new DenseVector(pYArr, false);
                pMat = new DenseMatrix(pVec, false);
                pXMat = new DenseMatrix(pXVec, false);
                pYMat = new DenseMatrix(pYVec, false);
                tVec = new DenseVector(dimension);
            }
            int valueDim;

            void setCacheRange(int minSize, int maxSize) {
                cacheMaxSize = maxSize;
                cacheMinSize = minSize;
                int cacheSize = maxSize - minSize + 1;
                matBCache = new DenseMatrix[cacheSize];
                matBxCache = new DenseMatrix[cacheSize];
                matByCache = new DenseMatrix[cacheSize];

                resultCache = new DenseVector[cacheSize];
                resultXCache = new DenseVector[cacheSize];
                resultYCache = new DenseVector[cacheSize];
                resMatCache = new DenseMatrix[cacheSize];
                resXMatCache = new DenseMatrix[cacheSize];
                resYMatCache = new DenseMatrix[cacheSize];
                for (int i = 0; i < cacheSize; i++) {
                    int nodeSize = i + minSize;
                    matBCache[i] = new DenseMatrix(valueDim, nodeSize);
                    matBxCache[i] = new DenseMatrix(valueDim, nodeSize);
                    matByCache[i] = new DenseMatrix(valueDim, nodeSize);

                    resultCache[i] = new DenseVector(nodeSize);
                    resultXCache[i] = new DenseVector(nodeSize);
                    resultYCache[i] = new DenseVector(nodeSize);

                    resMatCache[i] = new DenseMatrix(resultCache[i], false);
                    resXMatCache[i] = new DenseMatrix(resultXCache[i], false);
                    resYMatCache[i] = new DenseMatrix(resultYCache[i], false);
                }
            }
            public double[] pArr, pXArr, pYArr;    // the backend content of pVec, pXVec, pYVec
            public DenseVector pVec, pXVec, pYVec, tVec;   //Vectors just wrap pArr,pXArr,pYArr, the backend content of pMat,pXMat,pYMat
            public DenseMatrix pMat, pXMat, pYMat, tMat;  //pMat,pXMat,pYMat just wrap pArr,pXArr,pYArr;
            public DenseCholesky decomp;
            //Cache for nodes size from cacheMaxSize(include), to cacheMinSize (include)
            public int cacheMaxSize, cacheMinSize;
            public DenseMatrix[] matBCache, matBxCache, matByCache;
            public DenseVector[] resultCache, resultXCache, resultYCache;
            public DenseMatrix[] resMatCache, resXMatCache, resYMatCache;
            public UpperSPDDenseMatrix matA, matAx, matAy;         //matA= A = P^T * P, matAx = \partial A/ \partial x, matAy = \partial A/ \partial x
        }

        private void setBaseFunctions(BivariateArrayFunction[] baseFunctions) {
            this.baseFunctions = baseFunctions;
            setValueDimension(baseFunctions[0].valueDimension());
        }

        private void setWeightFunction(WeightFunction<Coordinate2D> weightFun) {
            this.weightFunction = weightFun;
        }

        private void setValueDimension(int dimension) {
            this.baseValueDim = dimension;
            cache.setValueDimension(dimension);
//            tMat=new DenseMatrix(tVec,false);
        }
        int oriI, xI, yI = -1;

        @Override
        public Vector[] values(Coordinate2D center, List<Node<Coordinate2D>> nodes, Collection<Boundary<Coordinate2D>> boundaries, Vector[] results) {

            int opt = 0;

            if (oriI != -1) {
                opt += 1;
            }
            if (xI != -1) {
                opt += 2;
            }

            if (yI != -1) {
                opt += 4;
            }

            double[] p, pX, pY, ws;
            DenseVector pV, pXV, pYV, tV;
            DenseMatrix pM, pXM, pYM;

            UpperSPDDenseMatrix mA, mAx, mAy;
            DenseMatrix mB, mBx, mBy, resM, resXM, resYM;
            DenseVector resV, resXV, resYV;

            BivariateArrayFunction pFun, pXFun, pYFun;
            Cache cc = cache;
            DenseCholesky dcomA = cc.decomp;
            final int funDim = baseValueDim;
            final int nodesSize = nodes.size();
            switch (opt) {
                case 0:

                case 1:

                case 2:


                case 7:
                    pFun = baseFunctions[0];
                    pXFun = baseFunctions[1];
                    pYFun = baseFunctions[2];
                    p = cc.pArr;
                    pX = cc.pXArr;
                    pY = cc.pYArr;
                    pV = cc.pVec;
                    pXV = cc.pXVec;
                    pYV = cc.pYVec;
                    pM = cc.pMat;
                    pXM = cc.pXMat;
                    pYM = cc.pYMat;
                    mA = cc.matA;
                    mAx = cc.matAx;
                    mAy = cc.matAy;
                    tV = cc.tVec;
                    ws = weights;
                    if (nodesSize > cc.cacheMaxSize || nodesSize < cc.cacheMinSize) {
                        mB = new DenseMatrix(funDim, nodesSize);
                        mBx = new DenseMatrix(funDim, nodesSize);
                        mBy = new DenseMatrix(funDim, nodesSize);
                        resV = new DenseVector(nodesSize);
                        resXV = new DenseVector(nodesSize);
                        resYV = new DenseVector(nodesSize);
                        resM = new DenseMatrix(resV, false);
                        resXM = new DenseMatrix(resXV, false);
                        resYM = new DenseMatrix(resYV, false);
                    } else {
                        int cacheI = nodesSize - cc.cacheMinSize;
                        mB = cc.matBCache[cacheI];
                        mBx = cc.matBxCache[cacheI];
                        mBy = cc.matByCache[cacheI];
                        resV = cc.resultCache[cacheI];
                        resXV = cc.resultXCache[cacheI];
                        resYV = cc.resultYCache[cacheI];
                        resM = cc.resMatCache[cacheI];
                        resXM = cc.resXMatCache[cacheI];
                        resYM = cc.resYMatCache[cacheI];

                    }
                    mA.zero();
                    mAx.zero();
                    mAy.zero();
                    int colBI = 0;
                    for (Node<Coordinate2D> nd : nodes) {
                        Coordinate2D coord = nd.coordinate;
                        double x = coord.x;
                        double y = coord.y;
                        weightFunction.values(nd, center, ws);
                        pFun.value(x, y, p);

                        double weight = ws[0];
                        double weightX = ws[1];
                        double weightY = ws[2];
                        for (int i = 0; i < funDim; i++) {
                            double tpi = p[i];
                            double t = tpi * weight;
                            double tx = tpi * weightX;
                            double ty = tpi * weightY;

                            for (int j = i; j < funDim; j++) {
                                double tpj = p[j];
                                mA.add(i, j, t * tpj);
                                mAx.add(i, j, tx * tpj);
                                mAy.add(i, j, ty * tpj);
                            }

                            mB.set(i, colBI, t);
                            mBx.set(i, colBI, tx);
                            mBy.set(i, colBI, ty);
                        }
                        colBI++;
                    }
                    dcomA.factor(mA);
                    double x = center.x;
                    double y = center.y;
                    pFun.value(x, y, p);
                    pXFun.value(x, y, pX);
                    pYFun.value(x, y, pY);
                    DenseMatrix mR = dcomA.solve(pM);   //pV & PM =A^-1 p =r
                    DenseVector vR = pV;
                    mB.transAmult(mR, resM);
                    results[oriI] = resV;

                    mAx.mult(vR, tV);
                    pXV.add(-1, tV);
                    DenseMatrix mRx = dcomA.solve(pXM);
                    mB.transAmult(mRx, resXM);
                    mBx.transAmultAdd(mR, resXM);
                    results[xI] = resXV;

                    mAy.mult(vR, tV);
                    pYV.add(-1, tV);
                    DenseMatrix mRy = dcomA.solve(pYM);
                    mB.transAmult(mRy, resYM);
                    mBy.transAmultAdd(mR, resYM);
                    results[yI] = resYV;

                    break;
                default:
                    throw new IllegalStateException();
            }


            return results;
        }

        @Override
        public void setPDTypes(PartialDiffType[] types) {
            weightFunction.setPDTypes(types);
            weights = new double[types.length];

            oriI = -1;
            xI = -1;
            yI = -1;
            for (int i = 0; i < types.length; i++) {
                PartialDiffType type = types[i];
                switch (type.getSumPartialOrder()) {
                    case 0:
                        oriI = i;
                        break;
                    case 1:
                        switch (type.getPartialDimension(0)) {
                            case 0:
                                xI = i;
                                break;
                            case 1:
                                yI = i;
                                break;
                            default:
                                throw new IllegalArgumentException("Dimension Index over Range exp <=1");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Partial oder over range exp: <=1");
                }
            }
        }
    }
}
