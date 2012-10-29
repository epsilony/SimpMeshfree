/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.List;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.ejml.data.DenseMatrix64F;

/**
 * @version 20120528
 * @author epsilonyuan@gmail.com
 */
public class WeakformAssembliers2D {

    public static class Simp implements WeakformAssemblier {

        DenseMatrix64F constitutiveLaw;
        FlexCompRowMatrix mainMatrix;
        DenseVector mainVector;
        int[] nodesIds;
        double neumannPenalty;
        protected int modelNdsSize;

        public Simp(DenseMatrix64F constitutiveLaw, double neumannPenalty, int nodesSize) {
            init(constitutiveLaw, neumannPenalty, nodesSize);
        }

        public Simp(DenseMatrix constitutiveLaw, double neumannPenalty, int nodesSize) {
            init(CommonUtils.toDenseMatrix64F(constitutiveLaw), neumannPenalty, nodesSize);
        }

        @Override
        public void asmBalance(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc) {
            TDoubleArrayList v, vx, vy;
            v = shapeFunVals[0];
            vx = shapeFunVals[1];
            vy = shapeFunVals[2];
            int sfNdsSz=nodes.size();
            double weight = qp.weight;
            if (nodesIds == null || nodesIds.length < sfNdsSz) {
                nodesIds = new int[sfNdsSz];
            }
            int[] nIds = nodesIds;
            int i = 0;
            for (Node nd : nodes) {
                nIds[i] = nd.id;
                i++;
            }
            DenseMatrix64F cLaw = constitutiveLaw;
            double d00 = cLaw.unsafe_get(0, 0) * weight,
                    d01 = cLaw.unsafe_get(0, 1) * weight,
                    d02 = cLaw.unsafe_get(0, 2) * weight,
                    d11 = cLaw.unsafe_get(1, 1) * weight,
                    d12 = cLaw.unsafe_get(1, 2) * weight,
                    d22 = cLaw.unsafe_get(2, 2) * weight;
            FlexCompRowMatrix mat = mainMatrix;
            double[] bcValue = new double[3];
            double bcAndWeightX = 0, bcAndWeightY = 0;
            if (null != volBc) {
                volBc.value(qp.coordinate, bcValue);
                bcAndWeightX = bcValue[0] * weight;
                bcAndWeightY = bcValue[1] * weight;
                if (bcAndWeightX == 0 && bcAndWeightY == 0) {
                    bcValue = null;
                }
            }

            DenseVector vec = mainVector;

            for (i = 0; i < sfNdsSz; i++) {
                int matIndexI = nIds[i] * 2;


                if (null != bcValue) {
                    double dv = v.getQuick(i);
                    vec.add(matIndexI, bcAndWeightX * dv);
                    vec.add(matIndexI + 1, bcAndWeightY * dv);
                }

                double ix = vx.getQuick(i), iy = vy.getQuick(i);
                double xx = ix * ix,
                        xy = ix * iy,
                        yx = iy * ix,
                        yy = iy * iy;
                double xyyx = xy + yx;


                mat.add(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                mat.add(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                mat.add(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);


                for (int j = i + 1; j < sfNdsSz; j++) {
                    int matIndexJ = nIds[j] * 2;
                    double jx = vx.getQuick(j), jy = vy.getQuick(j);
                    xx = ix * jx;
                    xy = ix * jy;
                    yx = iy * jx;
                    yy = iy * jy;
                    xyyx = xy + yx;

                    if (matIndexI < matIndexJ) {
                        mat.add(matIndexI, matIndexJ, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.add(matIndexI + 1, matIndexJ, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                        mat.add(matIndexI, matIndexJ + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.add(matIndexI + 1, matIndexJ + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    } else {
                        mat.add(matIndexJ, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.add(matIndexJ, matIndexI + 1, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                        mat.add(matIndexJ + 1, matIndexI, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.add(matIndexJ + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    }
                }
            }
        }

        @Override
        public void asmNeumann(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions) {

            DenseVector vec = mainVector;
            double weight = qp.weight;
            double[] bcValue = qp.values;
            boolean[] bcValidities = qp.validities;

            double valueX = bcValue[0] * weight;
            double valueY = bcValue[1] * weight;
            TDoubleArrayList vs = shapeFunctions[0];

            int i = 0;
            final boolean vali1 = bcValidities[0];
            final boolean vali2 = bcValidities[1];
            for (Node nd : nodes) {
                int vecIndex = nd.id * 2;
                double v = vs.getQuick(i);
                if (vali1) {
                    vec.add(vecIndex, valueX * v);
                }
                if (vali2) {
                    vec.add(vecIndex + 1, valueY * v);
                }
                i++;
            }

        }

        @Override
        public void asmDirichlet(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions) {
            double factor = qp.weight * neumannPenalty;
            FlexCompRowMatrix mat = mainMatrix;
            DenseVector vec = mainVector;
            TDoubleArrayList vs = shapeFunctions[0];

            double[] values = qp.values;
            boolean[] avis = qp.validities;

            int[] ids = new int[nodes.size()];
            int i = 0;
            for (Node nd : nodes) {
                ids[i++] = nd.id;
            }

            i = 0;
            final boolean vb1 = avis[0];
            final boolean vb2 = avis[1];
            for (Node nd : nodes) {
                int matIndexI = nd.id * 2;
                double vi = vs.getQuick(i);
                if (vb1) {
                    vec.add(matIndexI, vi * values[0] * factor);
                }
                if (vb2) {
                    vec.add(matIndexI + 1, vi * values[1] * factor);
                }
                for (int j = i; j < nodes.size(); j++) {
                    int matIndexJ = ids[j] * 2;
                    double vij = factor * vi * vs.getQuick(j);

                    int indexI, indexJ;
                    if (matIndexI <= matIndexJ) {
                        indexI = matIndexI;
                        indexJ = matIndexJ;
                    } else {
                        indexJ = matIndexI;
                        indexI = matIndexJ;
                    }
                    if (vb1) {
                        mat.add(indexI, indexJ, vij);
                    }
                    if (vb2) {
                        mat.add(indexI + 1, indexJ + 1, vij);
                    }
                }
                i++;
            }
        }

        @Override
        public Matrix getEquationMatrix() {
            return mainMatrix;
        }

        @Override
        public DenseVector getEquationVector() {
            return mainVector;
        }

        @Override
        synchronized public WeakformAssemblier produce() {
            Simp avator = new Simp(constitutiveLaw, neumannPenalty, modelNdsSize);
            return avator;
        }

        @Override
        public void uniteIn(WeakformAssemblier w) {
            Simp avator = (Simp) w;
            mainMatrix.add(avator.mainMatrix);
            mainVector.add(avator.mainVector);
        }

        private void init(DenseMatrix64F constitutiveLaw, double neumannPenalty, int nodesSize) {
            this.constitutiveLaw = new DenseMatrix64F(constitutiveLaw);
            this.neumannPenalty = neumannPenalty;
            mainMatrix = new FlexCompRowMatrix(nodesSize * 2, nodesSize * 2);
            mainVector = new DenseVector(nodesSize * 2);
            this.modelNdsSize=nodesSize;
        }
    }

    public static class Lagrange extends Simp {

        FlexCompColMatrix g;
        DenseVector q;
        TIntIntHashMap indexMap;
        private int dirichletNdsSize;

        public Lagrange(DenseMatrix64F constitutiveLaw, int nodesSize, int dirichletNdsSize) {
            super(constitutiveLaw, 0, nodesSize);
            init(dirichletNdsSize);
        }

        public Lagrange(DenseMatrix constitutiveLaw, int nodesSize, int dirichletNdsSize) {
            super(constitutiveLaw, 0, nodesSize);
            init(dirichletNdsSize);
        }

        private void init(int dirichletNdsSize) {
            this.dirichletNdsSize = dirichletNdsSize;
            g = new FlexCompColMatrix(mainMatrix.numRows(), dirichletNdsSize * 2);
            q = new DenseVector(dirichletNdsSize * 2);
            indexMap = new TIntIntHashMap(dirichletNdsSize);
        }

        @Override
        public synchronized WeakformAssemblier produce() {
            Lagrange res = new Lagrange(constitutiveLaw, modelNdsSize, dirichletNdsSize);
            return res;
        }

        @Override
        public Matrix getEquationMatrix() {
            if(mainMatrix.numRows()==modelNdsSize*2){
                asmKGQ();
            }
            return mainMatrix;
        }

        @Override
        public DenseVector getEquationVector() {
            if(mainMatrix.numRows()==modelNdsSize*2){
                asmKGQ();
            }
            return mainVector;
        }

        @Override
        public void asmDirichlet(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions) {
            LineBoundary bnd = (LineBoundary) qp.boundary;
            Node n0 = bnd.start;
            Node n1 = bnd.end;
            double[] ns = linearLagrange(n0, n1, qp.coordinate);
            TDoubleArrayList phis = shapeFunctions[0];
            double n_0 = ns[0];
            double n_1 = ns[1];

            int col_index_0;
            int col_index_1;
            if (indexMap.containsKey(n0.id)) {
                int id = indexMap.get(n0.id);
                col_index_0 = id * 2;
            } else {
                int id = indexMap.size();
                indexMap.put(n0.id, id);
                col_index_0 = id * 2;
            }
            if (indexMap.containsKey(n1.id)) {
                int id = indexMap.get(n1.id);
                col_index_1 = id * 2;
            } else {
                int id = indexMap.size();
                indexMap.put(n1.id, id);
                col_index_1 = id * 2;
            }
            int i = 0;
            double weight = qp.weight;
            for (Node nd : nodes) {
                double phi_i = phis.getQuick(i);
                double g_i0 = -weight * phi_i * n_0;
                double g_i1 = -weight * phi_i * n_1;
                int row_index = nd.id * 2;

                if (qp.validities[0]) {
                    g.add(row_index, col_index_0, g_i0);
                    g.add(row_index, col_index_1, g_i1);
                }

                if (qp.validities[1]) {
                    g.add(row_index + 1, col_index_0 + 1, g_i0);
                    g.add(row_index + 1, col_index_1 + 1, g_i1);
                }
                i++;
            }
            double q_i0 = -weight * n_0;
            double q_i1 = -weight * n_1;
            q.add(col_index_0, q_i0 * qp.values[0]);
            q.add(col_index_1, q_i1 * qp.values[0]);
            q.add(col_index_0 + 1, q_i0 * qp.values[1]);
            q.add(col_index_1 + 1, q_i1 * qp.values[1]);
        }

        @Override
        public void uniteIn(WeakformAssemblier w) {
            Lagrange l = (Lagrange) w;
            g.add(l.g);
            q.add(l.q);
            super.uniteIn(w);
        }

        private void asmKGQ() {
            int sum = 0;
            for (int j = 0; j < q.size(); j++) {
                SparseVector col = g.getColumn(j);
                if (col.getUsed() > 0) {
                    sum++;
                }
            }

            FlexCompRowMatrix oldMainMat = mainMatrix;
            DenseVector oldMainVec = mainVector;
            mainMatrix = new FlexCompRowMatrix(oldMainMat.numRows() + sum, oldMainMat.numColumns() + sum);
            mainVector = new DenseVector(oldMainVec.size() + sum);
            for (MatrixEntry me : oldMainMat) {
                mainMatrix.set(me.row(), me.column(), me.get());
            }
            for (VectorEntry ve : oldMainVec) {
                mainVector.set(ve.index(), ve.get());
            }
            int new_index = oldMainVec.size();
            for (int j = 0; j < q.size(); j++) {
                SparseVector col = g.getColumn(j);
                if (col.getUsed() > 0) {
                    for (VectorEntry ve : col) {
                        mainMatrix.set(ve.index(), new_index, ve.get());
                    }
                    mainVector.set(new_index, q.get(j));
                    new_index++;
                }
            }
        }

        public static double[] linearLagrange(Node n0, Node n1, Coordinate coord) {
            double d0 = GeometryMath.distance(n0, coord);
            double d1 = GeometryMath.distance(n1, coord);
            double l = GeometryMath.distance(n0, n1);
            return new double[]{d1 / l, d0 / l};
        }
    }
}
