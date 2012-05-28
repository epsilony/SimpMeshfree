/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.model.WeightFunctionCores;
import net.epsilony.simpmeshfree.model2d.test.WeightFunctionTestUtils;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version 20120528
 * @author epsilonyuan@gmail.com
 */
public class WeakformAssembliers2DTest {

    public WeakformAssembliers2DTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * 该函数中的任何常数都不便更改。
     */
    @Test
    public void testSimpAsmBalance() {
        WeakformAssemblier assemblier = new WeakformAssembliers2D.SimpAssemblier(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        double supRad = 14;
        List<Node> nds = sampleNodes();

        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(supRad, nds, 3, new WeightFunctionCores.TriSpline());
        shapeFun.setDiffOrder(1);

        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, null, null, resNds);
        assemblier.asmBalance(qp, resNds, shapeFunVals, null);
        FlexCompRowMatrix mat = (FlexCompRowMatrix) assemblier.getEquationMatrix();

//        System.out.println(shapeFunVals[0]);
//        System.out.println(shapeFunVals[1]);
//        System.out.println(shapeFunVals[2]);

        /*
         * Here, we assume that the shape function values are right, then
         * expMatrix=B^T*D*B，and it can be calculate by Python Numpy the
         * simpmeshfree_gui project has a helper tool to print array for java
         * code: simpmeshfree_gui.tools.print_np_array the python code is below:
         * vec1=... # copy and paste shapeFunVals[1] vec2=... # copy and paste
         * shapeFunVals[2] weight=3 # according to this test
         * matD=np.mat([[11,12,13],[12,22,23],[13,23,33]])
         * matB=np.mat([[vec1[0],0,vec1[1],0,vec1[2],0,vec1[3],0,vec1[4],0],[0,vec2[0],0,vec2[1],0,vec2[2],0,vec2[3],0,vec2[4]],[vec2[0],vec1[0],vec2[1],vec1[1],vec2[2],vec1[2],vec2[3],vec1[3],vec2[4],vec1[4]]])
         * expMatrix=matB.transpose*matD*matB*weight
         * print_np_array(np.array(expMatrix))
         */
        double[][] expMatrix = new double[][]{
            {0.012668090686, 0.0158045499093, -0.0371837778948, -0.0417660470121, -0.0143302339129, -0.0557888592593, -0.0533247707644, -0.031586267869, 0.104591227602, 0.124884133693},
            {0.0158045499093, 0.0221583355033, -0.0479789357632, -0.0584803414531, -0.00485105719417, -0.0788452508859, -0.0785340346629, -0.0437058415264, 0.132411891595, 0.174997563008},
            {-0.0371837778948, -0.0479789357632, 0.110483407974, 0.126254796982, 0.0310728549957, 0.173766374588, 0.166649401188, 0.0918291807175, -0.308623552322, -0.37846827507},
            {-0.0417660470121, -0.0584803414531, 0.126254796982, 0.155119657516, 0.01722407341, 0.201708555222, 0.203479349613, 0.12122887373, -0.34926867485, -0.462796926498},
            {-0.0143302339129, -0.00485105719417, 0.0310728549957, 0.01722407341, 0.106312036514, -0.0189864635468, -0.0227224230766, 0.0429768562317, -0.104998826981, -0.0436685085297},
            {-0.0557888592593, -0.0788452508859, 0.173766374588, 0.201708555222, -0.0189864635468, 0.332860708237, 0.310500919819, 0.107306063175, -0.472740411758, -0.61495757103},
            {-0.0533247707644, -0.0785340346629, 0.166649401188, 0.203479349613, -0.0227224230766, 0.310500919819, 0.301002904773, 0.126279875636, -0.452536404133, -0.615640461445},
            {-0.031586267869, -0.0437058415264, 0.0918291807175, 0.12122887373, 0.0429768562317, 0.107306063175, 0.126279875636, 0.130641371012, -0.259714007378, -0.352295815945},
            {0.104591227602, 0.132411891595, -0.308623552322, -0.34926867485, -0.104998826981, -0.472740411758, -0.452536404133, -0.259714007378, 0.865501642227, 1.04550147057},
            {0.124884133693, 0.174997563008, -0.37846827507, -0.462796926498, -0.0436685085297, -0.61495757103, -0.615640461445, -0.352295815945, 1.04550147057, 1.38320231438}};



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
        WeakformAssemblier assemblier = new WeakformAssembliers2D.SimpAssemblier(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        qp.values = new double[]{1.23, 3.42};
        qp.validities = new boolean[]{true, true};
        double supRad = 14;
        List<Node> nds = sampleNodes();

        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(supRad, nds, 3, new WeightFunctionCores.TriSpline());
        shapeFun.setDiffOrder(0);

        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, null, null, resNds);
        assemblier.asmNeumann(qp, resNds, shapeFunVals);
        DenseVector vec = assemblier.getEquationVector();

        System.out.println(shapeFunVals[0]);


        /*
         * Here, we assume that the shape function values are right, then
         * expMatrix=B^T*D*B，and it can be calculate by Python Numpy the
         * simpmeshfree_gui project has a helper tool to print array for java
         * code: simpmeshfree_gui.tools.print_np_array the python code is below:
         * v=... # copy and paste shapeFunVals[0] weight=3 # according to this
         * test values=np.array([1.23,3.42]) #according to this test
         * print_np_array(np.array(expMatrix))
         */
        double[] exps = new double[]{0.1605148, 0.44630945, 2.67380859, 7.43449219, 0.01441406,
            0.04007812, -0.19819336, -0.55107422, 1.08105469, 3.00585938};


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
        WeakformAssemblier assemblier = new WeakformAssembliers2D.SimpAssemblier(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), penalty, 25);
        QuadraturePoint qp = new QuadraturePoint();
        qp.coordinate.x = 33;
        qp.coordinate.y = 34;
        qp.weight = 3;
        qp.values = new double[]{1.23, 3.42};
        qp.validities = new boolean[]{true, true};
        double supRad = 14;
        List<Node> nds = sampleNodes();

        ShapeFunction shapeFun = WeightFunctionTestUtils.genShapeFunction(supRad, nds, 3, new WeightFunctionCores.TriSpline());
        shapeFun.setDiffOrder(0);

        ArrayList<Node> resNds = new ArrayList<>();
        TDoubleArrayList[] shapeFunVals = shapeFun.values(qp.coordinate, null, null, resNds);
        assemblier.asmDirichlet(qp, resNds, shapeFunVals);

        System.out.println(shapeFunVals[0]);


        /*
         * Here, we assume that the shape function values are right, then
         * expMatrix=B^T*D*B，and it can be calculate by Python Numpy the
         * simpmeshfree_gui project has a helper tool to print array for java
         * code: simpmeshfree_gui.tools.print_np_array the python code is below:
         * v=... # copy and paste shapeFunVals[0] weight=3 # according to this
         * test values=np.array([1.23,3.42]) #according to this test
         * print_np_array(np.array(expMatrix))
         */

        double[][] matExps = new double[][]{
            {567673.60611, 0.0, 9456140.73426, 0.0, 50976.4999151, 0.0, -700926.873833, 0.0, 3823237.49363, 0.0},
            {0.0, 567673.60611, 0.0, 9456140.73426, 0.0, 50976.4999151, 0.0, -700926.873833, 0.0, 3823237.49363},
            {9456140.73426, 0.0, 157517623.901, 0.0, 849151.611328, 0.0, -11675834.6558, 0.0, 63686370.8496, 0.0},
            {0.0, 9456140.73426, 0.0, 157517623.901, 0.0, 849151.611328, 0.0, -11675834.6558, 0.0, 63686370.8496},
            {50976.4999151, 0.0, 849151.611328, 0.0, 4577.63671875, 0.0, -62942.5048828, 0.0, 343322.753906, 0.0},
            {0.0, 50976.4999151, 0.0, 849151.611328, 0.0, 4577.63671875, 0.0, -62942.5048828, 0.0, 343322.753906},
            {-700926.873833, 0.0, -11675834.6558, 0.0, -62942.5048828, 0.0, 865459.442139, 0.0, -4720687.86621, 0.0},
            {0.0, -700926.873833, 0.0, -11675834.6558, 0.0, -62942.5048828, 0.0, 865459.442139, 0.0, -4720687.86621},
            {3823237.49363, 0.0, 63686370.8496, 0.0, 343322.753906, 0.0, -4720687.86621, 0.0, 25749206.543, 0.0},
            {0.0, 3823237.49363, 0.0, 63686370.8496, 0.0, 343322.753906, 0.0, -4720687.86621, 0.0, 25749206.543}};
        double[] exps = new double[]{ 1.60514803e+07,   4.46309452e+07,   2.67380859e+08,
         7.43449219e+08,   1.44140625e+06,   4.00781250e+06,
        -1.98193359e+07,  -5.51074219e+07,   1.08105469e+08,
         3.00585938e+08};

        Matrix mat=assemblier.getEquationMatrix();
        Vector vec=assemblier.getEquationVector();
        for (int i = 0; i < 5; i++) {
            int indexI = resNds.get(i).id * 2;
            double exp=exps[i*2];
            double act=vec.get(indexI);
            assertEquals(exp, act, Math.abs(1e-6*act));
            exp=exps[i*2+1];
            act=vec.get(indexI+1);
            assertEquals(exp, act, Math.abs(1e-6*act));
            for (int j = 0; j < 5; j++) {
                
                int indexJ = resNds.get(j).id * 2;
                if (indexI > indexJ) {
                    continue;
                }
                exp = matExps[i * 2][j * 2];
                act = mat.get(indexI, indexJ);
                try{
                assertEquals(exp, act, Math.abs(1e-6*act));
                }catch(AssertionError e){
                    assertEquals(exp, act, Math.abs(1e-6*act));
                }
                exp = matExps[i * 2][j * 2 + 1];
                act = mat.get(indexI, indexJ + 1);
                assertEquals(exp, act, Math.abs(1e-6*act));

                exp = matExps[i * 2 + 1][j * 2 + 1];
                act = mat.get(indexI + 1, indexJ + 1);
                assertEquals(exp, act, Math.abs(1e-6*act));

                if (i != j) {
                    exp = matExps[i * 2 + 1][j * 2];
                    act = mat.get(indexI + 1, indexJ);
                    assertEquals(exp, act, Math.abs(1e-6*act));
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
