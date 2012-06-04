/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 * @version 20120528
 * @author epsilonyuan@gmail.com
 */
public class WeakformAssembliers2D {

    public static class SimpAssemblier implements WeakformAssemblier {

        DenseMatrix constitutiveLaw;
        FlexCompRowMatrix mainMatrix;
        DenseVector mainVector;
        int[] nodesIds;
        double neumannPenalty;

        public SimpAssemblier(DenseMatrix constitutiveLaw, double neumannPenalty, int nodesSize) {
            this.constitutiveLaw = constitutiveLaw;
            this.neumannPenalty = neumannPenalty;
            mainMatrix = new FlexCompRowMatrix(nodesSize * 2, nodesSize * 2);
            mainVector = new DenseVector(nodesSize * 2);
        }

        @Override
        public void asmBalance(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc) {
            TDoubleArrayList v, vx, vy;
            v = shapeFunVals[0];
            vx = shapeFunVals[1];
            vy = shapeFunVals[2];

            double weight = qp.weight;
            int nodesSize = nodes.size();
            if (nodesIds == null || nodesIds.length < nodesSize) {
                nodesIds = new int[nodesSize];
            }
            int[] nIds = nodesIds;
            int i = 0;
            for (Node nd : nodes) {
                nIds[i] = nd.id;
                i++;
            }
            DenseMatrix cLaw = constitutiveLaw;
            double d00 = cLaw.get(0, 0) * weight,
                    d01 = cLaw.get(0, 1) * weight,
                    d02 = cLaw.get(0, 2) * weight,
                    d11 = cLaw.get(1, 1) * weight,
                    d12 = cLaw.get(1, 2) * weight,
                    d22 = cLaw.get(2, 2) * weight;
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

            for (i = 0; i < nodesSize; i++) {
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


                for (int j = i + 1; j < nodesSize; j++) {
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
                vec.add(matIndexI, vi * values[0] * factor);
                vec.add(matIndexI + 1, vi * values[1] * factor);

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
        public DenseMatrix getConstitutiveLaw(Coordinate pos) {
            return constitutiveLaw;
        }

        @Override
        public Matrix getEquationMatrix() {
            return mainMatrix;
        }

        @Override
        public DenseVector getEquationVector() {
            return mainVector;
        }
        LinkedList<SimpAssemblier> avators = new LinkedList<>();

        @Override
        synchronized public WeakformAssemblier avatorInstance() {
            SimpAssemblier avator = new SimpAssemblier(constitutiveLaw, neumannPenalty, mainVector.size() / 2);
            avators.add(avator);
            return avator;
        }
      
        @Override
        public void uniteAvators() {
            for (SimpAssemblier avator : avators) {
                for (MatrixEntry me : avator.mainMatrix) {
                    mainMatrix.add(me.row(), me.column(), me.get());
                }
                for (VectorEntry ve : avator.mainVector) {
                    mainVector.add(ve.index(), ve.get());
                }
            }
            avators.clear();
        }
    }
//    public static class ApacheSpareSimpAssemblier implements WeakformAssemblier {
//
//        DenseMatrix constitutiveLaw;
//        SparseRealMatrix mainMatrix;
//        DenseVector mainVector;
//        int[] nodesIds;
//        double neumannPenalty;
//
//        public ApacheSpareSimpAssemblier(DenseMatrix constitutiveLaw, double neumannPenalty, int nodesSize) {
//            this.constitutiveLaw = constitutiveLaw;
//            this.neumannPenalty = neumannPenalty;
//            mainMatrix = new OpenMapRealMatrix(nodesSize * 2, nodesSize * 2);
//            mainVector = new DenseVector(nodesSize * 2);
//        }
//
//        @Override
//        public void asmBalance(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions, VolumeCondition volBc) {
//            TDoubleArrayList v, vx, vy;
//
//            if (null == volBc) {
//                v = null;
//                vx = shapeFunctions[0];
//                vy = shapeFunctions[1];
//            } else {
//                v = shapeFunctions[0];
//                vx = shapeFunctions[1];
//                vy = shapeFunctions[2];
//            }
//            double weight = qp.weight;
//            int nodesSize = nodes.size();
//            if (nodesIds == null || nodesIds.length < nodesSize) {
//                nodesIds = new int[nodesSize];
//            }
//            int[] nIds = nodesIds;
//            int i = 0;
//            for (Node nd : nodes) {
//                nIds[i] = nd.id;
//                i++;
//            }
//            DenseMatrix tmat = constitutiveLaw;
//            double d00 = tmat.get(0, 0) * weight,
//                    d01 = tmat.get(0, 1) * weight,
//                    d02 = tmat.get(0, 2) * weight,
//                    d11 = tmat.get(1, 1) * weight,
//                    d12 = tmat.get(1, 2) * weight,
//                    d22 = tmat.get(2, 2) * weight;
//            SparseRealMatrix mat = mainMatrix;
//            double[] bcValue = new double[3];
//            double bcAndWeightX = 0, bcAndWeightY = 0;
//            if (null != volBc) {
//                volBc.value(qp.coordinate,bcValue);
//                bcAndWeightX = bcValue[0] * weight;
//                bcAndWeightY = bcValue[1] * weight;
//                if (bcAndWeightX == 0 && bcAndWeightY == 0) {
//                    bcValue = null;
//                }
//            }
//
//            if (null == bcValue) {
//                for (i = 0; i < nodesSize; i++) {
//                    int matIndexI = nIds[i] * 2;
//                    double ix = vx.getQuick(i), iy = vy.getQuick(i);
//                    {
//                        double xx = ix * ix,
//                                xy = ix * iy,
//                                yx = iy * ix,
//                                yy = iy * iy;
//                        double xyyx = xy + yx;
//
//
//                        mat.addToEntry(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
//                        mat.addToEntry(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                        mat.addToEntry(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                    }
//                    for (int j = i + 1; j < nodesSize; j++) {
//                        int matIndexJ = nIds[j] * 2;
//                        double jx = vx.getQuick(j), jy = vy.getQuick(j);
//                        double xx = ix * jx,
//                                xy = ix * jy,
//                                yx = iy * jx,
//                                yy = iy * jy;
//                        double xyyx = xy + yx;
//                        if (matIndexI < matIndexJ) {
//                            mat.addToEntry(matIndexI, matIndexJ, d00 * xx + d02 * xyyx + d22 * yy);
//                            mat.addToEntry(matIndexI + 1, matIndexJ, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
//                            mat.addToEntry(matIndexI, matIndexJ + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                            mat.addToEntry(matIndexI + 1, matIndexJ + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                        } else {
//                            mat.addToEntry(matIndexJ, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
//                            mat.addToEntry(matIndexJ, matIndexI + 1, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
//                            mat.addToEntry(matIndexJ + 1, matIndexI, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                            mat.addToEntry(matIndexJ + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                        }
//                    }
//                }
//            } else {
//                DenseVector vec = mainVector;
//
//                for (i = 0; i < nodesSize; i++) {
//                    int matIndexI = nIds[i] * 2;
//                    double ix = vx.getQuick(i), iy = vy.getQuick(i);
//                    double dv = v.getQuick(i);
//                    vec.add(matIndexI, bcAndWeightX * dv);
//                    vec.add(matIndexI + 1, bcAndWeightY * dv);
//
//                    {
//                        double xx = ix * ix,
//                                xy = ix * iy,
//                                yx = iy * ix,
//                                yy = iy * iy;
//                        double xyyx = xy + yx;
//
//
//                        mat.addToEntry(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
//                        mat.addToEntry(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                        mat.addToEntry(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                    }
//
//                    for (int j = i + 1; j < nodesSize; j++) {
//                        int matIndexJ = nIds[j] * 2;
//                        double jx = vx.getQuick(j), jy = vy.getQuick(j);
//                        double xx = ix * jx,
//                                xy = ix * jy,
//                                yx = iy * jx,
//                                yy = iy * jy;
//                        double xyyx = xy + yx;
//
//                        if (matIndexI < matIndexJ) {
//                            mat.addToEntry(matIndexI, matIndexJ, d00 * xx + d02 * xyyx + d22 * yy);
//                            mat.addToEntry(matIndexI + 1, matIndexJ, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
//                            mat.addToEntry(matIndexI, matIndexJ + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                            mat.addToEntry(matIndexI + 1, matIndexJ + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                        } else {
//                            mat.addToEntry(matIndexJ, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
//                            mat.addToEntry(matIndexJ, matIndexI + 1, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
//                            mat.addToEntry(matIndexJ + 1, matIndexI, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
//                            mat.addToEntry(matIndexJ + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
//                        }
//
//                    }
//                }
//            }
//
//        }
//
//        @Override
//        public void asmNeumann(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals) {
//
//
//            DenseVector vec = mainVector;
//            double weight = qp.weight;
//
//            double[] bcValue = qp.values;
//            boolean[] avis = qp.validities;
//            int opt = 0;
//            if (avis[0]) {
//                opt += 1;
//            }
//            if (avis[1]) {
//                opt += 2;
//            }
//            if (0 == opt) {
//                return;
//            }
//
//            double valueX = bcValue[0] * weight;
//            double valueY = bcValue[1] * weight;
//            TDoubleArrayList vs = shapeFunVals[0];
//
//
//            switch (opt) {
//                case 1: {
//                    int i = 0;
//                    for (Node nd : nodes) {
//                        int vecIndex = nd.id * 2;
//                        double v = vs.getQuick(i);
//                        vec.add(vecIndex, valueX * v);
//                        i++;
//                    }
//                }
//                case 2: {
//                    int i = 0;
//                    for (Node nd : nodes) {
//                        int vecIndex = nd.id * 2;
//                        double v = vs.getQuick(i);
//                        vec.add(vecIndex + 1, valueY * v);
//                        i++;
//                    }
//                }
//                break;
//                case 3: {
//                    int i = 0;
//                    for (Node nd : nodes) {
//                        int vecIndex = nd.id * 2;
//                        double v = vs.getQuick(i);
//                        vec.add(vecIndex, valueX * v);
//                        vec.add(vecIndex + 1, valueY * v);
//                        i++;
//                    }
//                }
//                break;
//                default:
//                    throw new UnsupportedOperationException();
//            }
//
//        }
//
//        @Override
//        public void asmDirichlet(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions) {
//            double factor = qp.weight * neumannPenalty;
//            SparseRealMatrix mat = mainMatrix;
//            DenseVector vec = mainVector;
//            TDoubleArrayList vs = shapeFunctions[0];
//
//            double[] values = qp.values;
//            boolean[] avis = qp.validities;
//            int opt = 0;
//            if (avis[0]) {
//                opt += 1;
//            }
//            if (avis[1]) {
//                opt += 2;
//            }
//            if (0 == opt) {
//                return;
//            }
//            int[] ids = new int[nodes.size()];
//            int i = 0;
//            for (Node nd : nodes) {
//                ids[i++] = nd.id;
//            }
//
//            switch (opt) {
//                case 1:
//                    i = 0;
//                    for (Node nd : nodes) {
//                        int matIndexI = nd.id * 2;
//                        double vi = vs.getQuick(i);
//                        vec.add(matIndexI, vi * values[0] * factor);
//
//                        for (int j = i; j < nodes.size(); j++) {
//                            int matIndexJ = ids[j] * 2;
//                            double vij = factor * vi * vs.getQuick(j);
//
//                            int indexI, indexJ;
//                            if (matIndexI <= matIndexJ) {
//                                indexI = matIndexI;
//                                indexJ = matIndexJ;
//                            } else {
//                                indexJ = matIndexI;
//                                indexI = matIndexJ;
//                            }
//                            mat.addToEntry(indexI, indexJ, vij);
//                        }
//                        i++;
//                    }
//                    break;
//                case 2:
//                    i = 0;
//                    for (Node nd : nodes) {
//                        int matIndexI = nd.id * 2;
//                        double vi = vs.getQuick(i);
//                        vec.add(matIndexI + 1, vi * values[1] * factor);
//
//                        for (int j = i; j < nodes.size(); j++) {
//                            int matIndexJ = ids[j] * 2;
//                            double vij = factor * vi * vs.getQuick(j);
//
//                            int indexI, indexJ;
//                            if (matIndexI <= matIndexJ) {
//                                indexI = matIndexI;
//                                indexJ = matIndexJ;
//                            } else {
//                                indexJ = matIndexI;
//                                indexI = matIndexJ;
//                            }
//                            mat.addToEntry(indexI + 1, indexJ + 1, vij);
//                        }
//                        i++;
//                    }
//                    break;
//                case 3:
//                    i = 0;
//                    for (Node nd : nodes) {
//                        int matIndexI = nd.id * 2;
//                        double vi = vs.getQuick(i);
//                        vec.add(matIndexI, vi * values[0] * factor);
//                        vec.add(matIndexI + 1, vi * values[1] * factor);
//
//                        for (int j = i; j < nodes.size(); j++) {
//                            int matIndexJ = ids[j] * 2;
//                            double vij = factor * vi * vs.getQuick(j);
//
//                            int indexI, indexJ;
//                            if (matIndexI <= matIndexJ) {
//                                indexI = matIndexI;
//                                indexJ = matIndexJ;
//                            } else {
//                                indexJ = matIndexI;
//                                indexI = matIndexJ;
//                            }
//                            mat.addToEntry(indexI, indexJ, vij);
//                            mat.addToEntry(indexI + 1, indexJ + 1, vij);
//                        }
//                        i++;
//                    }
//                    break;
//                default:
//                    throw new UnsupportedOperationException();
//            }
//
//
//        }
//
//        @Override
//        public DenseMatrix getConstitutiveLaw(Coordinate pos) {
//            return constitutiveLaw;
//        }
//
//        @Override
//        public Matrix getEquationMatrix() {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public DenseVector getEquationVector() {
//            return mainVector;
//        }
//        LinkedList<ApacheSpareSimpAssemblier> avators = new LinkedList<>();
//
//        @Override
//        synchronized public WeakformAssemblier avatorInstance() {
//            ApacheSpareSimpAssemblier avator = new ApacheSpareSimpAssemblier(constitutiveLaw, neumannPenalty, mainVector.size() / 2);
//            avators.add(avator);
//            return avator;
//        }
//
//        @Override
//        public void uniteAvators() {
//            for (ApacheSpareSimpAssemblier avator : avators) {
//                for (int i = 0; i < avator.mainMatrix.getColumnDimension(); i++) {
//                    Iterator<RealVector.Entry> iter = avator.mainMatrix.getRowVector(i).sparseIterator();
//                    while (iter.hasNext()) {
//                        RealVector.Entry ve = iter.next();
//                        mainMatrix.addToEntry(i, ve.getIndex(), ve.getValue());
//                    }
//
//                }
//                for (VectorEntry ve : avator.mainVector) {
//                    mainVector.add(ve.index(), ve.get());
//                }
//            }
//            avators.clear();
//        }
//    }
}
