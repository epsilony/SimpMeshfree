/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.LineBoundary;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.utils.*;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakformAssembliers2DTest {
//
//    public WeakformAssembliers2DTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//
//    /**
//     * 该函数中的任何常数都不便更改。
//     */
//    @Test
//    public void testAsmBalance() {
//        WeakFormAssemblier assemblier = new WeakFormAssembliers2D.SimpAssemblier(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
//        QuadraturePoint qp = new QuadraturePoint();
//        qp.coordinate.x = 33;
//        qp.coordinate.y = 34;
//        qp.weight = 3;
//        double supRad = 14;
//
//
//        ShapeFunction shapeFun = sampleShapeFunction(supRad);
//        shapeFun.setOrders(new PartDiffOrd[]{PartDiffOrd.X(), PartDiffOrd.Y(), PartDiffOrd.ORI()});
//        DenseVector[] shapeFunVals = new DenseVector[3];
//        LinkedList<Node> filteredNodes = new LinkedList<>();
//        shapeFun.values(qp.coordinate, sampleNoes(), null, shapeFunVals, filteredNodes);
//        System.out.println("shapeFunVals:");
//        System.out.println(shapeFunVals[0]);
//        System.out.println(shapeFunVals[1]);
//        System.out.println("");
//        assemblier.asmBalance(qp, filteredNodes, shapeFunVals, null);
//        FlexCompRowMatrix mat = (FlexCompRowMatrix) assemblier.getEquationMatrix();
//        DenseVector vec = assemblier.getEquationVector();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 50; i++) {
//            sb.append(String.format("% 16d ", i));
//        }
//        for (int i = 0; i < 50; i++) {
//            for (int j = 0; j < 50; j++) {
//                sb.append(String.format("% 16f ", mat.get(i, j)));
//            }
//            sb.append(String.format("% 16d\n", i));
//        }
//        System.out.println(sb);
//        System.out.println("");
//
//        System.out.println(shapeFunVals[2]);
//
//        /**
//         * 这里假设形函数是正确的，则expMatrix=B^T*D*B，由Python Numpy得到
//         * vec1=[8.317302260936e-03,          -6.942923211543e-02,          -4.720537240644e-02,           6.111192985449e-02,           4.720537240644e-02]
//         * vec2=[2.553057920770e-03,-6.795689914363e-02,6.285078330209e-02,-3.459615877714e-02,3.714921669790e-02]
//         * matD=mat([[11,12,13],[12,22,23],[13,23,33]])
//         * matB=mat([[vec1[0],0,vec1[1],0,vec1[2],0,vec1[3],0,vec1[4],0],[0,vec2[0],0,vec2[1],0,vec2[2],0,vec2[3],0,vec2[4]],[vec2[0],vec1[0],vec2[1],vec1[1],vec2[2],vec1[2],vec2[3],vec1[3],vec2[4],vec1[4]]])
//         * expMatrix=matB.transpose*matD*matB
//         */
//        
//        double[][] expMatrix = new double[][]{{4.58444567e-03, 6.01433723e-03, -6.51891230e-02,
//                -7.23887263e-02, 1.86162083e-02, 2.64729964e-03,
//                2.89192512e-03, 1.88160013e-02, 3.90965439e-02,
//                4.49110882e-02},
//            {6.01433723e-03, 1.02091376e-02, -9.68302832e-02,
//                -1.19850558e-01, 4.31731214e-02, -5.25109228e-04,
//                -9.14144608e-03, 3.54018525e-02, 5.67842706e-02,
//                7.47646771e-02},
//            {-6.51891230e-02, -9.68302832e-02, 9.84288912e-01,
//                1.14360435e+00, -3.59762294e-01, -6.39647801e-03,
//                2.44475175e-02, -3.27926228e-01, -5.83785013e-01,
//                -7.12451362e-01},
//            {-7.23887263e-02, -1.19850558e-01, 1.14360435e+00,
//                1.43312961e+00, -4.83408056e-01, -3.71764950e-02,
//                8.50363772e-02, -3.85701819e-01, -6.72843946e-01,
//                -8.90400741e-01},
//            {1.86162083e-02, 4.31731214e-02, -3.59762294e-01,
//                -4.83408056e-01, 2.33189551e-01, -4.10599888e-02,
//                -9.69757170e-02, 1.76503871e-01, 2.04932251e-01,
//                3.04791053e-01},
//            {2.64729964e-03, -5.25109228e-04, -6.39647801e-03,
//                -3.71764950e-02, -4.10599888e-02, 7.18894955e-02,
//                3.74119604e-02, -5.13964960e-02, 7.39720672e-03,
//                1.72086047e-02},
//            {2.89192512e-03, -9.14144608e-03, 2.44475175e-02,
//                8.50363772e-02, -9.69757170e-02, 3.74119604e-02,
//                7.68260028e-02, -5.71843830e-02, -7.18972842e-03,
//                -5.61225085e-02},
//            {1.88160013e-02, 3.54018525e-02, -3.27926228e-01,
//                -3.85701819e-01, 1.76503871e-01, -5.13964960e-02,
//                -5.71843830e-02, 1.56962299e-01, 1.89790739e-01,
//                2.44734164e-01},
//            {3.90965439e-02, 5.67842706e-02, -5.83785013e-01,
//                -6.72843946e-01, 2.04932251e-01, 7.39720672e-03,
//                -7.18972842e-03, 1.89790739e-01, 3.46945946e-01,
//                4.18871729e-01},
//            {4.49110882e-02, 7.47646771e-02, -7.12451362e-01,
//                -8.90400741e-01, 3.04791053e-01, 1.72086047e-02,
//                -5.61225085e-02, 2.44734164e-01, 4.18871729e-01,
//                5.53693295e-01}};
//
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                int indexI = filteredNodes.get(i).id * 2;
//                int indexJ = filteredNodes.get(j).id * 2;
//                if (indexI > indexJ) {
//                    continue;
//                }
//                double exp = expMatrix[i * 2][j * 2];
//                double act = mat.get(indexI, indexJ);
//                assertEquals(exp, act, 1e-6);
//
//                exp = expMatrix[i * 2][j * 2 + 1];
//                act = mat.get(indexI, indexJ + 1);
//                assertEquals(exp, act, 1e-6);
//
//                exp = expMatrix[i * 2 + 1][j * 2 + 1];
//                act = mat.get(indexI + 1, indexJ + 1);
//                assertEquals(exp, act, 1e-6);
//
//                if (i != j) {
//                    exp = expMatrix[i * 2 + 1][j * 2];
//                    act = mat.get(indexI + 1, indexJ);
//                    assertEquals(exp, act, 1e-6);
//                }
//            }
//        }
//
//    }
//    
////    @Test
//    public void testAsmNeumann(){
//        WeakFormAssemblier assemblier = new WeakFormAssembliers2D.SimpAssemblier(new DenseMatrix(new double[][]{{11, 12, 13}, {00, 22, 23}, {00, 00, 33}}), 1e8, 25);
//        BCQuadraturePoint qp=new BCQuadraturePoint();
//        qp.coordinate.x = 33;
//        qp.coordinate.y = 34;
//        qp.weight = 3;
//        double supRad = 14;
//        final LineBoundary line=new LineBoundary(new Coordinate(0,0), new Coordinate(0,50));
//        qp.boundaryCondition=new BoundaryCondition() {
//
//            @Override
//            public Boundary getBoundary() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public boolean[] valueByParameter(Coordinate parameter, double[] results) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public boolean[] valueByCoordinate(Coordinate coord, double[] results) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        };
//
//
//        ShapeFunction shapeFun = sampleShapeFunction(supRad);
//        shapeFun.setOrders(new PartDiffOrd[]{PartDiffOrd.ORI(),PartDiffOrd.X(), PartDiffOrd.Y()});
//        DenseVector[] shapeFunVals = new DenseVector[3];
//        LinkedList<Node> filteredNodes = new LinkedList<>();
//        shapeFun.values(qp.coordinate, sampleNoes(), null, shapeFunVals, filteredNodes);
//        System.out.println("shapeFunVals:");
//        System.out.println(shapeFunVals[0]);
//        System.out.println("");
////        assemblier.asmDirichlet();
//        FlexCompRowMatrix mat = (FlexCompRowMatrix) assemblier.getEquationMatrix();
//        DenseVector vec = assemblier.getEquationVector();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 50; i++) {
//            sb.append(String.format("% 16d ", i));
//        }
//        for (int i = 0; i < 50; i++) {
//            for (int j = 0; j < 50; j++) {
//                sb.append(String.format("% 16f ", mat.get(i, j)));
//            }
//            sb.append(String.format("% 16d\n", i));
//        }
//        System.out.println(sb);
//        System.out.println("");
//
//    }
//
//    public ShapeFunction sampleShapeFunction(double supRad) {
//        ShapeFunction shapeFun = new ShapeFunctions2D.MLS(new WeakFormProcessor2DTest.TempWeightFunction(supRad), new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(1), BivariateCompletePolynomial.partialXFactory(1), BivariateCompletePolynomial.partialYFactory(1)}, new BoundaryBasedCriterions2D.Visible(new NodeSupportDomainSizers.ConstantSizer(supRad)), null);
//        return shapeFun;
//    }
//
//    public List<Node> sampleNoes() {
//        LinkedList<Node> result = new LinkedList<>();
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                result.add(new Node(i * 10, j * 10));
//            }
//        }
//        int[] indes = new int[]{12, 22, 24, 10, 23, 11, 13, 4, 9, 5, 16, 18, 7, 20, 1, 3, 21, 0, 15, 14, 2, 6, 8, 17, 19};
//        int i = 0;
//        for (Node nd : result) {
//            nd.id = indes[i++];
//        }
//        return result;
//    }
}
