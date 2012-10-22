/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.DistanceSquareFunctions;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.SupportDomainCritierion;
import net.epsilony.simpmeshfree.model.SupportDomainUtils;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.model.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.model.sfun.wcores.TriSpline;
import net.epsilony.simpmeshfree.model2d.test.WeightFunctionTestUtils;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @version 20120528
 * @author epsilonyuan@gmail.com
 */
public class SimpTest {

    public SimpTest() {
    }

    /**
     * 该函数中的任何常数都不便更改。
     */
    @Test
    public void testSimpAsmBalance() {
        WeakformAssemblier assemblier = new WeakformAssembliers2D.Simp(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        double supRad = 14;
        List<Node> nds = sampleNodes();

        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(3, new TriSpline());
        shapeFun.setDiffOrder(1);

        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList supRads = new TDoubleArrayList();
        supRads.add(supRad);
        SupportDomainCritierion simpCriterion = SupportDomainUtils.simpCriterion(supRad, nds);
        simpCriterion.setDiffOrder(1);
        TDoubleArrayList[] distSqs = DistanceSquareFunctions.initDistSqsContainer(2, 1);
        simpCriterion.getSupports(qp.coordinate, null, resNds, distSqs);
//        resNds.clear();
//        resNds.addAll(nds);
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, resNds, distSqs, supRads, null);
        assemblier.asmBalance(qp, resNds, shapeFunVals, null);

        FlexCompRowMatrix mat = (FlexCompRowMatrix) assemblier.getEquationMatrix();

        System.out.println(shapeFunVals[0]);
        System.out.println(shapeFunVals[1]);
        System.out.println(shapeFunVals[2]);

        /*
         * Here, we assume that the shape function values are right, then
         * expMatrix=B^T*D*B，and it can be calculate by Python Numpy the
         * simpmeshfree_gui project has a helper tool to print array for java
         * code: simpmeshfree_gui.tools.print_np_array the python code is below:
         * vec1=... # copy and paste shapeFunVals[1] vec2=... # copy and paste
         * shapeFunVals[2] weight=3 # according to this test
         * matD=np.mat([[11,12,13],[12,22,23],[13,23,33]])
         * matB=np.mat([[vec1[0],0,vec1[1],0,vec1[2],0,vec1[3],0,vec1[4],0],[0,vec2[0],0,vec2[1],0,vec2[2],0,vec2[3],0,vec2[4]],[vec2[0],vec1[0],vec2[1],vec1[1],vec2[2],vec1[2],vec2[3],vec1[3],vec2[4],vec1[4]]])
         * expMatrix=matB.transpose()*matD*matB*weight
         * print_np_array(np.array(expMatrix))
         */

        /* the assumed shape function is
         * vec0=[-1.310791015625, -64.9375, 7.625, -41.3125, -11.875]
         vec1=[-0.12890625, -5.625, -3.25, -2.21875, 0.25]
         vec2=[0.1240234375, 4.0, 2.0, 1.0, 1.75]
         */
        double[][] expMatrix = new double[][]{
            {71.5243712068, 90.9661468863, 3494.74580383, 4463.84120178, -160.542938232, -304.83480835, 2136.44936371, 2763.58277893, 557.395294189, 744.584747314},
            {90.9661468863, 194.513589084, 4400.98924255, 9561.02549744, 25.4729919434, -735.708892822, 2611.30410004, 5948.05927277, 627.501800537, 1621.88314819},
            {3494.74580383, 4400.98924255, 170780.753906, 215953.394531, -7971.9609375, -14697.8671875, 104447.724609, 133680.421875, 27280.1015625, 36005.6015625},
            {4463.84120178, 9561.02549744, 215953.394531, 469967.027344, 1300.1953125, -36210.3515625, 128117.349609, 292389.966797, 30774.6328125, 79738.1484375},
            {-160.542938232, 25.4729919434, -7971.9609375, 1300.1953125, 1031.390625, -349.171875, -5104.81640625, 895.5, -1488.984375, 302.015625},
            {-304.83480835, -735.708892822, -14697.8671875, -36210.3515625, -349.171875, 3033.234375, -8629.06640625, -22612.8632812, -2009.296875, -6223.265625},
            {2136.44936371, 2611.30410004, 104447.724609, 128117.349609, -5104.81640625, -8629.06640625, 63958.8544922, 79276.2451172, 16759.1835938, 21331.1835938},
            {2763.58277893, 5948.05927277, 133680.421875, 292389.966797, 895.5, -22612.8632812, 79276.2451172, 181939.822266, 19020.609375, 49636.7460938},
            {557.395294189, 627.501800537, 27280.1015625, 30774.6328125, -1488.984375, -2009.296875, 16759.1835938, 19020.609375, 4428.140625, 5103.140625},
            {744.584747314, 1621.88314819, 36005.6015625, 79738.1484375, 302.015625, -6223.265625, 21331.1835938, 49636.7460938, 5103.140625, 13554.984375}};


        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int indexI = resNds.get(i).id * 2;
                int indexJ = resNds.get(j).id * 2;
                if (indexI > indexJ) {
                    continue;
                }
                double exp = expMatrix[i * 2][j * 2];
                double act = mat.get(indexI, indexJ);
                assertEquals(exp, act, 1e-6);

                exp = expMatrix[i * 2][j * 2 + 1];
                act = mat.get(indexI, indexJ + 1);
                assertEquals(exp, act, 1e-6);

                exp = expMatrix[i * 2 + 1][j * 2 + 1];
                act = mat.get(indexI + 1, indexJ + 1);
                assertEquals(exp, act, 1e-6);

                if (i != j) {
                    exp = expMatrix[i * 2 + 1][j * 2];
                    act = mat.get(indexI + 1, indexJ);
                    assertEquals(exp, act, 1e-6);
                }
            }
        }

    }

    @Test
    public void testSimpAsmNeumann() {
        WeakformAssemblier assemblier = new WeakformAssembliers2D.Simp(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        qp.values = new double[]{1.23, 3.42};
        qp.validities = new boolean[]{true, true};
        double supRad = 14;
        List<Node> nds = sampleNodes();

        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(3, new TriSpline());
        shapeFun.setDiffOrder(0);
        SupportDomainCritierion simpCriterion = SupportDomainUtils.simpCriterion(supRad, nds);
        simpCriterion.setDiffOrder(0);
        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList[] distSqs = DistanceSquareFunctions.initDistSqsContainer(2, 0);
        simpCriterion.getSupports(qp.coordinate, null, resNds, distSqs);
        TDoubleArrayList rads = new TDoubleArrayList();
        rads.add(supRad);
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, resNds, distSqs, rads, null);
        assemblier.asmNeumann(qp, resNds, shapeFunVals);
        DenseVector vec = assemblier.getEquationVector();

        System.out.println(shapeFunVals[0]);

        /* the assumed shape function is
         * vec0=[-1.310791015625, -64.9375, 7.625, -41.3125, -11.875]
         vec1=[-0.12890625, -5.625, -3.25, -2.21875, 0.25]
         vec2=[0.1240234375, 4.0, 2.0, 1.0, 1.75]
         */
        /*
         * Here, we assume that the shape function values are right
         */
        double[] exps = new double[]{0.45764648, 1.27248047, 14.76, 41.04,
            7.38, 20.52, 3.69, 10.26,
            6.4575, 17.955};


        for (int i = 0; i < 5; i++) {

            int indexI = resNds.get(i).id * 2;

            double exp = exps[i * 2];
            double act = vec.get(indexI);
            assertEquals(exp, act, 1e-6);

            exp = exps[i * 2 + 1];
            act = vec.get(indexI + 1);
            assertEquals(exp, act, 1e-6);
        }
    }

    @Test
    public void testSimpAssemblyDirichlet() {
        double penalty = 1e8;
        WeakformAssemblier assemblier = new WeakformAssembliers2D.Simp(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), penalty, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        qp.values = new double[]{1.23, 3.42};
        qp.validities = new boolean[]{true, true};
        double supRad = 14;
        List<Node> nds = sampleNodes();
        SupportDomainCritierion simpCriterion = SupportDomainUtils.simpCriterion(supRad, nds);
        simpCriterion.setDiffOrder(0);
        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(3, new TriSpline());
        shapeFun.setDiffOrder(0);

        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList[] distSqs = DistanceSquareFunctions.initDistSqsContainer(2, 0);
        simpCriterion.getSupports(qp.coordinate, null, resNds, distSqs);
        TDoubleArrayList rads = new TDoubleArrayList();
        rads.add(supRad);
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, resNds, distSqs, rads, null);
        assemblier.asmDirichlet(qp, resNds, shapeFunVals);

        System.out.println(shapeFunVals[0]);


        /*
         * Here, we assume that the shape function values are right, then
         * v=... # copy and paste shapeFunVals[0] weight=3 # according to this
         * test values=np.array([1.23,3.42]) #according to this test
         * print_np_array(np.array(expMatrix))
         */
        
        /* the assumed shape function is
         * vec0=[-1.310791015625, -64.9375, 7.625, -41.3125, -11.875]
         vec1=[-0.12890625, -5.625, -3.25, -2.21875, 0.25]
         vec2=[0.1240234375, 4.0, 2.0, 1.0, 1.75]
         */

        double[][] matExps = new double[][]{
            {4614543.91479, 0.0, 148828125.0, 0.0, 74414062.5, 0.0, 37207031.25, 0.0, 65112304.6875, 0.0},
            {0.0, 4614543.91479, 0.0, 148828125.0, 0.0, 74414062.5, 0.0, 37207031.25, 0.0, 65112304.6875},
            {148828125.0, 0.0, 4800000000.0, 0.0, 2400000000.0, 0.0, 1200000000.0, 0.0, 2100000000.0, 0.0},
            {0.0, 148828125.0, 0.0, 4800000000.0, 0.0, 2400000000.0, 0.0, 1200000000.0, 0.0, 2100000000.0},
            {74414062.5, 0.0, 2400000000.0, 0.0, 1200000000.0, 0.0, 600000000.0, 0.0, 1050000000.0, 0.0},
            {0.0, 74414062.5, 0.0, 2400000000.0, 0.0, 1200000000.0, 0.0, 600000000.0, 0.0, 1050000000.0},
            {37207031.25, 0.0, 1200000000.0, 0.0, 600000000.0, 0.0, 300000000.0, 0.0, 525000000.0, 0.0},
            {0.0, 37207031.25, 0.0, 1200000000.0, 0.0, 600000000.0, 0.0, 300000000.0, 0.0, 525000000.0},
            {65112304.6875, 0.0, 2100000000.0, 0.0, 1050000000.0, 0.0, 525000000.0, 0.0, 918750000.0, 0.0},
            {0.0, 65112304.6875, 0.0, 2100000000.0, 0.0, 1050000000.0, 0.0, 525000000.0, 0.0, 918750000.0}};
        double[] exps = new double[]{4.57646484e+07, 1.27248047e+08, 1.47600000e+09,
            4.10400000e+09, 7.38000000e+08, 2.05200000e+09,
            3.69000000e+08, 1.02600000e+09, 6.45750000e+08,
            1.79550000e+09};

        Matrix mat = assemblier.getEquationMatrix();
        Vector vec = assemblier.getEquationVector();
        for (int i = 0; i < 5; i++) {
            int indexI = resNds.get(i).id * 2;
            double exp = exps[i * 2];
            double act = vec.get(indexI);
            assertEquals(exp, act, Math.abs(1e-6 * act));
            exp = exps[i * 2 + 1];
            act = vec.get(indexI + 1);
            assertEquals(exp, act, Math.abs(1e-6 * act));
            for (int j = 0; j < 5; j++) {

                int indexJ = resNds.get(j).id * 2;
                if (indexI > indexJ) {
                    continue;
                }
                exp = matExps[i * 2][j * 2];
                act = mat.get(indexI, indexJ);
                try {
                    assertEquals(exp, act, Math.abs(1e-6 * act));
                } catch (AssertionError e) {
                    assertEquals(exp, act, Math.abs(1e-6 * act));
                }
                exp = matExps[i * 2][j * 2 + 1];
                act = mat.get(indexI, indexJ + 1);
                assertEquals(exp, act, Math.abs(1e-6 * act));

                exp = matExps[i * 2 + 1][j * 2 + 1];
                act = mat.get(indexI + 1, indexJ + 1);
                assertEquals(exp, act, Math.abs(1e-6 * act));

                if (i != j) {
                    exp = matExps[i * 2 + 1][j * 2];
                    act = mat.get(indexI + 1, indexJ);
                    assertEquals(exp, act, Math.abs(1e-6 * act));
                }
            }
        }
    }

    public static List<Node> sampleNodes() {
        LinkedList<Node> result = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                result.add(new Node(i * 10, j * 10));
            }
        }
        int[] indes = new int[]{12, 22, 24, 10, 23, 11, 13, 4, 9, 5, 16, 18, 7, 20, 1, 3, 21, 0, 15, 14, 2, 6, 8, 17, 19};
        int i = 0;
        for (Node nd : result) {
            nd.id = indes[i++];
        }
        return result;
    }
}
