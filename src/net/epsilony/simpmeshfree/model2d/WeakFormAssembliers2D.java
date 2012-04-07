/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakFormAssemblier;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SparseRealMatrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakFormAssembliers2D {
    

    public static class SimpAssemblier implements WeakFormAssemblier {

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
        public void asmBalance(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions, VolumeCondition volBc) {
            double[] v, vx, vy;

            if (null == volBc) {
                v = null;
                vx = shapeFunctions[0].getData();
                vy = shapeFunctions[1].getData();
            } else {
                v = shapeFunctions[0].getData();
                vx = shapeFunctions[1].getData();
                vy = shapeFunctions[2].getData();
            }
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
            DenseMatrix tmat = constitutiveLaw;
            double d00 = tmat.get(0, 0) * weight,
                    d01 = tmat.get(0, 1) * weight,
                    d02 = tmat.get(0, 2) * weight,
                    d11 = tmat.get(1, 1) * weight,
                    d12 = tmat.get(1, 2) * weight,
                    d22 = tmat.get(2, 2) * weight;
            FlexCompRowMatrix mat = mainMatrix;
            double[] bcValue = null;
            double bcAndWeightX = 0, bcAndWeightY = 0;
            if (null != volBc) {
                bcValue = volBc.value(qp.coordinate);
                bcAndWeightX = bcValue[0] * weight;
                bcAndWeightY = bcValue[1] * weight;
                if (bcAndWeightX == 0 && bcAndWeightY == 0) {
                    bcValue = null;
                }
            }

            if (null == bcValue) {
                for (i = 0; i < nodesSize; i++) {
                    int matIndexI = nIds[i] * 2;
                    double ix = vx[i], iy = vy[i];
                    {
                        double xx = ix * ix,
                                xy = ix * iy,
                                yx = iy * ix,
                                yy = iy * iy;
                        double xyyx = xy + yx;


                        mat.add(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.add(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.add(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    }
                    for (int j = i + 1; j < nodesSize; j++) {
                        int matIndexJ = nIds[j] * 2;
                        double jx = vx[j], jy = vy[j];
                        double xx = ix * jx,
                                xy = ix * jy,
                                yx = iy * jx,
                                yy = iy * jy;
                        double xyyx = xy + yx;
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
            } else {
                DenseVector vec = mainVector;

                for (i = 0; i < nodesSize; i++) {
                    int matIndexI = nIds[i] * 2;
                    double ix = vx[i], iy = vy[i];
                    double dv = v[i];
                    vec.add(matIndexI, bcAndWeightX * dv);
                    vec.add(matIndexI + 1, bcAndWeightY * dv);

                    {
                        double xx = ix * ix,
                                xy = ix * iy,
                                yx = iy * ix,
                                yy = iy * iy;
                        double xyyx = xy + yx;


                        mat.add(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.add(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.add(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    }

                    for (int j = i + 1; j < nodesSize; j++) {
                        int matIndexJ = nIds[j] * 2;
                        double jx = vx[j], jy = vy[j];
                        double xx = ix * jx,
                                xy = ix * jy,
                                yx = iy * jx,
                                yy = iy * jy;
                        double xyyx = xy + yx;

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

        }

        @Override
        public void asmNeumann(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions) {


            DenseVector vec = mainVector;
            double weight = qp.weight;
            BoundaryCondition boundBC = qp.boundaryCondition;
            double[] bcValue = new double[2];
            boolean[] avis = boundBC.valueByParameter(qp.parameter, bcValue);
            int opt = 0;
            if (avis[0]) {
                opt += 1;
            }
            if (avis[1]) {
                opt += 2;
            }
            if (0 == opt) {
                return;
            }

            double valueX = bcValue[0] * weight;
            double valueY = bcValue[1] * weight;
            double[] vs = shapeFunctions[0].getData();


            switch (opt) {
                case 1: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex, valueX * v);
                        i++;
                    }
                }
                case 2: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex + 1, valueY * v);
                        i++;
                    }
                }
                break;
                case 3: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex, valueX * v);
                        vec.add(vecIndex + 1, valueY * v);
                        i++;
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException();
            }

        }

        @Override
        public void asmDirichlet(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions) {
            double factor = qp.weight * neumannPenalty;
            FlexCompRowMatrix mat = mainMatrix;
            DenseVector vec = mainVector;
            double[] vs = shapeFunctions[0].getData();
            BoundaryCondition bc = qp.boundaryCondition;
            double[] values = new double[2];
            boolean[] avis = bc.valueByCoordinate(qp.coordinate, values);
            int opt = 0;
            if (avis[0]) {
                opt += 1;
            }
            if (avis[1]) {
                opt += 2;
            }
            if (0 == opt) {
                return;
            }
            int[] ids = new int[nodes.size()];
            int i = 0;
            for (Node nd : nodes) {
                ids[i++] = nd.id;
            }

            switch (opt) {
                case 1:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI, vi * values[0] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.add(indexI, indexJ, vij);
                        }
                        i++;
                    }
                    break;
                case 2:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI + 1, vi * values[1] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.add(indexI + 1, indexJ + 1, vij);
                        }
                        i++;
                    }
                    break;
                case 3:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI, vi * values[0] * factor);
                        vec.add(matIndexI + 1, vi * values[1] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.add(indexI, indexJ, vij);
                            mat.add(indexI + 1, indexJ + 1, vij);
                        }
                        i++;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
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

        LinkedList<SimpAssemblier> avators=new LinkedList<>();
        @Override
        synchronized public WeakFormAssemblier divisionInstance() {
            SimpAssemblier avator=new SimpAssemblier(constitutiveLaw, neumannPenalty, mainVector.size()/2);
            avators.add(avator);
            return avator;
        }

        @Override
        public void uniteAvators() {
            for(SimpAssemblier avator:avators){
                for(MatrixEntry me:avator.mainMatrix){
                    mainMatrix.add(me.row(), me.column(), me.get());
                }
                for(VectorEntry ve:avator.mainVector){
                    mainVector.add(ve.index(),ve.get());
                }
            }
            avators.clear();  
        }
    }
    
    public static class ApacheSpareSimpAssemblier implements WeakFormAssemblier {

        DenseMatrix constitutiveLaw;
        SparseRealMatrix mainMatrix;
        DenseVector mainVector;
        int[] nodesIds;
        double neumannPenalty;

        public ApacheSpareSimpAssemblier(DenseMatrix constitutiveLaw, double neumannPenalty, int nodesSize) {
            this.constitutiveLaw = constitutiveLaw;
            this.neumannPenalty = neumannPenalty;
            mainMatrix = new OpenMapRealMatrix(nodesSize * 2, nodesSize * 2);
            mainVector = new DenseVector(nodesSize * 2);
        }

        @Override
        public void asmBalance(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions, VolumeCondition volBc) {
            double[] v, vx, vy;

            if (null == volBc) {
                v = null;
                vx = shapeFunctions[0].getData();
                vy = shapeFunctions[1].getData();
            } else {
                v = shapeFunctions[0].getData();
                vx = shapeFunctions[1].getData();
                vy = shapeFunctions[2].getData();
            }
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
            DenseMatrix tmat = constitutiveLaw;
            double d00 = tmat.get(0, 0) * weight,
                    d01 = tmat.get(0, 1) * weight,
                    d02 = tmat.get(0, 2) * weight,
                    d11 = tmat.get(1, 1) * weight,
                    d12 = tmat.get(1, 2) * weight,
                    d22 = tmat.get(2, 2) * weight;
            SparseRealMatrix mat = mainMatrix;
            double[] bcValue = null;
            double bcAndWeightX = 0, bcAndWeightY = 0;
            if (null != volBc) {
                bcValue = volBc.value(qp.coordinate);
                bcAndWeightX = bcValue[0] * weight;
                bcAndWeightY = bcValue[1] * weight;
                if (bcAndWeightX == 0 && bcAndWeightY == 0) {
                    bcValue = null;
                }
            }

            if (null == bcValue) {
                for (i = 0; i < nodesSize; i++) {
                    int matIndexI = nIds[i] * 2;
                    double ix = vx[i], iy = vy[i];
                    {
                        double xx = ix * ix,
                                xy = ix * iy,
                                yx = iy * ix,
                                yy = iy * iy;
                        double xyyx = xy + yx;


                        mat.addToEntry(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.addToEntry(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.addToEntry(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    }
                    for (int j = i + 1; j < nodesSize; j++) {
                        int matIndexJ = nIds[j] * 2;
                        double jx = vx[j], jy = vy[j];
                        double xx = ix * jx,
                                xy = ix * jy,
                                yx = iy * jx,
                                yy = iy * jy;
                        double xyyx = xy + yx;
                        if (matIndexI < matIndexJ) {
                            mat.addToEntry(matIndexI, matIndexJ, d00 * xx + d02 * xyyx + d22 * yy);
                            mat.addToEntry(matIndexI + 1, matIndexJ, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                            mat.addToEntry(matIndexI, matIndexJ + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                            mat.addToEntry(matIndexI + 1, matIndexJ + 1, d11 * yy + d12 * xyyx + d22 * xx);
                        } else {
                            mat.addToEntry(matIndexJ, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                            mat.addToEntry(matIndexJ, matIndexI + 1, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                            mat.addToEntry(matIndexJ + 1, matIndexI, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                            mat.addToEntry(matIndexJ + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                        }
                    }
                }
            } else {
                DenseVector vec = mainVector;

                for (i = 0; i < nodesSize; i++) {
                    int matIndexI = nIds[i] * 2;
                    double ix = vx[i], iy = vy[i];
                    double dv = v[i];
                    vec.add(matIndexI, bcAndWeightX * dv);
                    vec.add(matIndexI + 1, bcAndWeightY * dv);

                    {
                        double xx = ix * ix,
                                xy = ix * iy,
                                yx = iy * ix,
                                yy = iy * iy;
                        double xyyx = xy + yx;


                        mat.addToEntry(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                        mat.addToEntry(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                        mat.addToEntry(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                    }

                    for (int j = i + 1; j < nodesSize; j++) {
                        int matIndexJ = nIds[j] * 2;
                        double jx = vx[j], jy = vy[j];
                        double xx = ix * jx,
                                xy = ix * jy,
                                yx = iy * jx,
                                yy = iy * jy;
                        double xyyx = xy + yx;

                        if (matIndexI < matIndexJ) {
                            mat.addToEntry(matIndexI, matIndexJ, d00 * xx + d02 * xyyx + d22 * yy);
                            mat.addToEntry(matIndexI + 1, matIndexJ, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                            mat.addToEntry(matIndexI, matIndexJ + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                            mat.addToEntry(matIndexI + 1, matIndexJ + 1, d11 * yy + d12 * xyyx + d22 * xx);
                        } else {
                            mat.addToEntry(matIndexJ, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
                            mat.addToEntry(matIndexJ, matIndexI + 1, d01 * yx + d02 * xx + d12 * yy + d22 * xy);
                            mat.addToEntry(matIndexJ + 1, matIndexI, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
                            mat.addToEntry(matIndexJ + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
                        }

                    }
                }
            }

        }

        @Override
        public void asmNeumann(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions) {


            DenseVector vec = mainVector;
            double weight = qp.weight;
            BoundaryCondition boundBC = qp.boundaryCondition;
            double[] bcValue = new double[2];
            boolean[] avis = boundBC.valueByParameter(qp.parameter, bcValue);
            int opt = 0;
            if (avis[0]) {
                opt += 1;
            }
            if (avis[1]) {
                opt += 2;
            }
            if (0 == opt) {
                return;
            }

            double valueX = bcValue[0] * weight;
            double valueY = bcValue[1] * weight;
            double[] vs = shapeFunctions[0].getData();


            switch (opt) {
                case 1: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex, valueX * v);
                        i++;
                    }
                }
                case 2: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex + 1, valueY * v);
                        i++;
                    }
                }
                break;
                case 3: {
                    int i = 0;
                    for (Node nd : nodes) {
                        int vecIndex = nd.id * 2;
                        double v = vs[i];
                        vec.add(vecIndex, valueX * v);
                        vec.add(vecIndex + 1, valueY * v);
                        i++;
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException();
            }

        }

        @Override
        public void asmDirichlet(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions) {
            double factor = qp.weight * neumannPenalty;
            SparseRealMatrix mat = mainMatrix;
            DenseVector vec = mainVector;
            double[] vs = shapeFunctions[0].getData();
            BoundaryCondition bc = qp.boundaryCondition;
            double[] values = new double[2];
            boolean[] avis = bc.valueByCoordinate(qp.coordinate, values);
            int opt = 0;
            if (avis[0]) {
                opt += 1;
            }
            if (avis[1]) {
                opt += 2;
            }
            if (0 == opt) {
                return;
            }
            int[] ids = new int[nodes.size()];
            int i = 0;
            for (Node nd : nodes) {
                ids[i++] = nd.id;
            }

            switch (opt) {
                case 1:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI, vi * values[0] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.addToEntry(indexI, indexJ, vij);
                        }
                        i++;
                    }
                    break;
                case 2:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI + 1, vi * values[1] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.addToEntry(indexI + 1, indexJ + 1, vij);
                        }
                        i++;
                    }
                    break;
                case 3:
                    i = 0;
                    for (Node nd : nodes) {
                        int matIndexI = nd.id * 2;
                        double vi = vs[i];
                        vec.add(matIndexI, vi * values[0] * factor);
                        vec.add(matIndexI + 1, vi * values[1] * factor);

                        for (int j = i; j < nodes.size(); j++) {
                            int matIndexJ = ids[j] * 2;
                            double vij = factor * vi * vs[j];

                            int indexI, indexJ;
                            if (matIndexI <= matIndexJ) {
                                indexI = matIndexI;
                                indexJ = matIndexJ;
                            } else {
                                indexJ = matIndexI;
                                indexI = matIndexJ;
                            }
                            mat.addToEntry(indexI, indexJ, vij);
                            mat.addToEntry(indexI + 1, indexJ + 1, vij);
                        }
                        i++;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }


        }

        @Override
        public DenseMatrix getConstitutiveLaw(Coordinate pos) {
            return constitutiveLaw;
        }

        @Override
        public Matrix getEquationMatrix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DenseVector getEquationVector() {
            return mainVector;
        }

        LinkedList<ApacheSpareSimpAssemblier> avators=new LinkedList<>();
        @Override
        synchronized public WeakFormAssemblier divisionInstance() {
            ApacheSpareSimpAssemblier avator=new ApacheSpareSimpAssemblier(constitutiveLaw, neumannPenalty, mainVector.size()/2);
            avators.add(avator);
            return avator;
        }

        @Override
        public void uniteAvators() {
            for(ApacheSpareSimpAssemblier avator:avators){
                for(int i=0;i<avator.mainMatrix.getColumnDimension();i++){
                    Iterator<RealVector.Entry> iter=avator.mainMatrix.getRowVector(i).sparseIterator();
                    while(iter.hasNext()) {
                        RealVector.Entry ve=iter.next();
                        mainMatrix.addToEntry(i, ve.getIndex(), ve.getValue());
                    }
                    
                }
                for(VectorEntry ve:avator.mainVector){
                    mainVector.add(ve.index(),ve.get());
                }
            }
            avators.clear();  
        }
    }
}
