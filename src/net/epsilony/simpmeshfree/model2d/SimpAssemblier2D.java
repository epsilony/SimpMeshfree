/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.utils.geom.Node;
import net.epsilony.simpmeshfree.model.VolumeCondition;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.spfun.CommonUtils;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.ejml.data.DenseMatrix64F;

/**
 *
 * @author epsilon
 */
public class SimpAssemblier2D implements WeakformAssemblier {
    DenseMatrix64F constitutiveLaw;
    FlexCompRowMatrix mainMatrix;
    DenseVector mainVector;
    int[] nodesIds;
    double neumannPenalty;
    protected int modelNdsSize;

    public SimpAssemblier2D(DenseMatrix64F constitutiveLaw, double neumannPenalty, int nodesSize) {
        init(constitutiveLaw, neumannPenalty, nodesSize);
    }

    public SimpAssemblier2D(DenseMatrix constitutiveLaw, double neumannPenalty, int nodesSize) {
        init(CommonUtils.toDenseMatrix64F(constitutiveLaw), neumannPenalty, nodesSize);
    }

    @Override
    public void asmBalance(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc) {
        TDoubleArrayList v;
        TDoubleArrayList vx;
        TDoubleArrayList vy;
        v = shapeFunVals[0];
        vx = shapeFunVals[1];
        vy = shapeFunVals[2];
        int sfNdsSz = nodes.size();
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
        double d00 = cLaw.unsafe_get(0, 0) * weight;
        double d01 = cLaw.unsafe_get(0, 1) * weight;
        double d02 = cLaw.unsafe_get(0, 2) * weight;
        double d11 = cLaw.unsafe_get(1, 1) * weight;
        double d12 = cLaw.unsafe_get(1, 2) * weight;
        double d22 = cLaw.unsafe_get(2, 2) * weight;
        FlexCompRowMatrix mat = mainMatrix;
        double[] bcValue = new double[3];
        double bcAndWeightX = 0;
        double bcAndWeightY = 0;
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
            double ix = vx.getQuick(i);
            double iy = vy.getQuick(i);
            double xx = ix * ix;
            double xy = ix * iy;
            double yx = iy * ix;
            double yy = iy * iy;
            double xyyx = xy + yx;
            mat.add(matIndexI, matIndexI, d00 * xx + d02 * xyyx + d22 * yy);
            mat.add(matIndexI, matIndexI + 1, d01 * xy + d12 * yy + d02 * xx + d22 * yx);
            mat.add(matIndexI + 1, matIndexI + 1, d11 * yy + d12 * xyyx + d22 * xx);
            for (int j = i + 1; j < sfNdsSz; j++) {
                int matIndexJ = nIds[j] * 2;
                double jx = vx.getQuick(j);
                double jy = vy.getQuick(j);
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
                int indexI;
                int indexJ;
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
    public synchronized WeakformAssemblier produce() {
        SimpAssemblier2D avator = new SimpAssemblier2D(constitutiveLaw, neumannPenalty, modelNdsSize);
        return avator;
    }

    @Override
    public void uniteIn(WeakformAssemblier w) {
        SimpAssemblier2D avator = (SimpAssemblier2D) w;
        mainMatrix.add(avator.mainMatrix);
        mainVector.add(avator.mainVector);
    }

    private void init(DenseMatrix64F constitutiveLaw, double neumannPenalty, int nodesSize) {
        this.constitutiveLaw = new DenseMatrix64F(constitutiveLaw);
        this.neumannPenalty = neumannPenalty;
        mainMatrix = new FlexCompRowMatrix(nodesSize * 2, nodesSize * 2);
        mainVector = new DenseVector(nodesSize * 2);
        this.modelNdsSize = nodesSize;
    }
    
}
