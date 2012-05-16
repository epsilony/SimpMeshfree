/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.*;
import static net.epsilony.simpmeshfree.utils.CommonUtils.len2DBase;
import static net.epsilony.simpmeshfree.utils.CommonUtils.vectorMultTDoubleArrayLists;
import net.epsilony.simpmeshfree.utils.PartDiffCoordinateArrayFunction;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseCholesky;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSPDDenseMatrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2D {

    /**
     * 移动最小二乘法，没有通过白盒测试。测试样例证明了其的单位分解性和再生性。
     */
    public static class MLS implements ShapeFunction {

        private int order;
        WeightFunction weightFunction;
        PartDiffCoordinateArrayFunction baseFunction;
        BoundaryBasedCritieron criterion;
        int MAX_NODES_SIZE_ESTIMATION = 50;
        ArrayList<TDoubleArrayList> nodesWeights = new ArrayList<>();
        UpperSPDDenseMatrix A, A_x, A_y;
        ArrayList<TDoubleArrayList> B, B_x, B_y;
        DenseVector p, p_x, p_y, gamma, gamma_x, gamma_y;
        UpperSPDDenseMatrix[] As;
        ArrayList<ArrayList<TDoubleArrayList>> Bs;
        DenseVector[] ps;
        double[][] ps_arr;
        DenseVector[] gamgas;
        DenseCholesky decomper;
        private DenseVector tv;

        @Override
        public DenseVector[] values(Coordinate center, Boundary centerBnd, DenseVector[] results,ArrayList<Node> resNodes) {

            double supR = criterion.setCenter(center, centerBnd, resNodes);
            int diffDim = len2DBase(order);
            int baseDim = baseFunction.getDim();
            weightFunction.values(resNodes, supR, nodesWeights);

            for (int i = 0; i < diffDim; i++) {
                As[i].zero();
                ArrayList<TDoubleArrayList> tB = Bs.get(i);
                for (int j = 0; j < baseDim; j++) {
                    tB.get(j).fill(0, baseDim, 0);
                }
            }
            int nodeId = 0;
            for (Node nd : resNodes) {
                Coordinate coord = nd;
                baseFunction.values(coord, ps_arr);
                for (int dId = 0; dId < diffDim; dId++) {
                    double weight_d=nodesWeights.get(dId).getQuick(nodeId);
                    UpperSPDDenseMatrix A_d=As[dId];
                    ArrayList<TDoubleArrayList> B_d=Bs.get(dId);
//                    double weight = nodesWeights.get(0).getQuick(nodeId);
//                    double weight_x = nodesWeights.get(1).getQuick(nodeId);
//                    double weight_y = nodesWeights.get(2).getQuick(nodeId);
                    for (int i = 0; i < baseDim; i++) {
                        for (int j = i; j < baseDim; j++) {
                            double p_ij = p.get(i) * p.get(j);
                            A_d.add(i,j,weight_d*p_ij);
//                            A.add(i, j, weight * p_ij);
//                            A_x.add(i, j, weight_x * p_ij);
//                            A_y.add(i, j, weight_y * p_ij);
                        }
                        B_d.get(i).set(nodeId,weight_d*p.get(i)+B_d.get(i).get(nodeId));
//                        B.get(i).set(nodeId, weight * p.get(i) + B.get(i).get(nodeId));
//                        B_x.get(i).set(nodeId, weight_x * p.get(i) + B_x.get(i).get(nodeId));
//                        B_y.get(i).set(nodeId, weight_y * p.get(i) + B_y.get(i).get(nodeId));
                    }
                }
                nodeId++;
            }
            

            baseFunction.values(center, ps_arr);
            
            A.solve(p, gamma);
            vectorMultTDoubleArrayLists(gamma, B, results[0]);
            
            if(order<1){
                return results;
            }
            
            tv.zero();
            A_x.mult(gamma, tv);
            tv.scale(-1);
            tv.add(p_x);
            A.solve(tv, gamma_x);
            
            tv.zero();
            A_y.mult(gamma,tv);
            tv.scale(-1);
            tv.add(p_y);
            A.solve(tv,gamma_y);
            
            results[1].zero();
            vectorMultTDoubleArrayLists(gamma_x, B, tv);
            results[1].add(tv);
            vectorMultTDoubleArrayLists(gamma, B_x, tv);
            results[1].add(tv);

            results[2].zero();
            vectorMultTDoubleArrayLists(gamma_y, B, tv);
            results[2].add(tv);
            vectorMultTDoubleArrayLists(gamma, B_y, tv);
            results[2].add(tv);

            return results;
        }

        @Override
        public void setOrder(int order) {
            if (order < 0 || order >= 2) {
                throw new UnsupportedOperationException();
            }
            this.order = order;
            //TODO: here maybe improved
            nodesWeights.ensureCapacity(order);
            weightFunction.setOrder(order);
        }

        @Override
        public int getOrder() {
            return order;
        }

        public void setBaseFunction(PartDiffCoordinateArrayFunction baseFunction) {
            
            this.baseFunction = baseFunction;
            final int baseLen = baseFunction.getDim();
            A = new UpperSPDDenseMatrix(baseLen);
            A_x = new UpperSPDDenseMatrix(baseLen);
            A_y = new UpperSPDDenseMatrix(baseLen);

            B = new ArrayList<>(baseLen);
            B_x = new ArrayList<>(baseLen);
            B_y = new ArrayList<>(baseLen);
            Bs = new ArrayList<>(3);
            Bs.add(B);
            Bs.add(B_x);
            Bs.add(B_y);
            for (int i = 0; i < baseLen; i++) {
                B.add(new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION));
                B_x.add(new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION));
                B_y.add(new TDoubleArrayList(MAX_NODES_SIZE_ESTIMATION));
            }

            p = new DenseVector(baseLen);
            p_x = new DenseVector(baseLen);
            p_y = new DenseVector(baseLen);

            gamma = new DenseVector(baseLen);
            gamma_x = new DenseVector(baseLen);
            gamma_y = new DenseVector(baseLen);

            As = new UpperSPDDenseMatrix[]{A, A_x, A_y};
            ps = new DenseVector[]{p, p_x, p_y};
            gamgas = new DenseVector[]{gamma, gamma_x, gamma_y};

            ps_arr = new double[baseLen][];
            for (int i = 0; i < baseLen; i++) {
                ps_arr[i] = ps[i].getData();
            }
            
            tv=new DenseVector(baseLen);

        }
        
        
    }
}
