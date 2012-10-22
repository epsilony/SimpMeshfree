/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.*;
import net.epsilony.simpmeshfree.model.DistanceSquareFunctions;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.SupportDomainCritierion;
import net.epsilony.simpmeshfree.model.SupportDomainUtils;
import net.epsilony.simpmeshfree.model.sfun.ShapeFunction;
import net.epsilony.simpmeshfree.model.sfun.WeightFunctionCore;
import net.epsilony.simpmeshfree.model.sfun.wcores.SimpPower;
import net.epsilony.simpmeshfree.model.sfun.wcores.TriSpline;
import static net.epsilony.simpmeshfree.model2d.test.WeightFunctionTestUtils.*;
import net.epsilony.simpmeshfree.model2d.test.WeightFunctionTestUtils.ValueFun;
import net.epsilony.utils.geom.Coordinate;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2DTest {

    @Test
    public void testFitness() {
        System.out.println("start function fitness reproduction test");
        int testNum = 1000;
        int nodesNum = 80;
        double xMax = 1, yMax = 1, rad = 0.5;
        List<Node> nds = genNodes(0, 0, xMax, yMax, nodesNum, false);
        List<? extends Coordinate> centers = genNodes(0 + 0.1, 0 + 0.1, xMax - 0.1, yMax - 0.1, testNum, false);
        int[] baseOrders = new int[]{3, 4};
        WeightFunctionCore[] coreFuns = new WeightFunctionCore[]{new TriSpline(), new SimpPower(1), new SimpPower(2)};
        for (int i = 0; i < baseOrders.length; i++) {
            int baseOrder = baseOrders[i];
            System.out.println("baseOrder=" + baseOrder);
            for (int j = 0; j < coreFuns.length; j++) {
                WeightFunctionCore coreFun = coreFuns[j];
                System.out.println("core J=" + j);
                ShapeFunction shapeFun = genShapeFunction(baseOrder, coreFun);
                shapeFun.setDiffOrder(1);
                ValueFun expFun = genExpFun("c");
                LinkedList<double[]> actResults = new LinkedList<>();
                evalCase(rad,shapeFun, expFun, nds, centers, actResults);
                assertResults(centers, actResults, expFun, 1);
            }
        }

    }

    public static void assertResults(List<? extends Coordinate> centers, List<double[]> actResults, ValueFun expFun, double err) {
        Iterator<? extends Coordinate> cenIter = centers.iterator();
        Iterator<double[]> actIter = actResults.iterator();
        int i = 0;
        while (cenIter.hasNext()) {
            Coordinate center = cenIter.next();
            double[] actRes = actIter.next();
            double[] expRes = expFun.value(center);
            try {
                assertArrayEquals(expRes, actRes, 1e-6);
            } catch (AssertionError e) {
                System.out.println("i = " + i);
                System.out.println(center + " expRes = " + Arrays.toString(expRes) + " actRes = " + Arrays.toString(actRes));
                throw e;
            }
            i++;
        }
    }

    @Test
    public void testPartitionOfUnity() {
        int nodesNum = 110;
        int testNum = 1000;
        double xMax = 20, yMax = 20, rad = 30;
        List<Node> nds = genNodes(0, 0, xMax, yMax, Math.sqrt(nodesNum * 1.0), false);
        List<? extends Coordinate> centers = genNodes(0, 0, xMax, yMax, Math.sqrt(testNum * 1.0), false);
        int[] baseOrders = new int[]{1, 2, 3};
        WeightFunctionCore[] coreFuns = new WeightFunctionCore[]{new TriSpline(), new SimpPower(1), new SimpPower(2)};
        ArrayList<Node> resNodes = new ArrayList<>(nodesNum);
        double maxErr = 0;
        SupportDomainCritierion simpCriterion = SupportDomainUtils.simpCriterion(rad, nds);
        TDoubleArrayList rads=new TDoubleArrayList();
        rads.add(rad);
        TDoubleArrayList[] distSqs=DistanceSquareFunctions.initDistSqsContainer(2, 1);
        for (int i = 0; i < baseOrders.length; i++) {
            int baseOrder = baseOrders[i];
            for (int j = 0; j < coreFuns.length; j++) {
                WeightFunctionCore coreFun = coreFuns[j];
                ShapeFunction shapeFun = genShapeFunction(baseOrder, coreFun);
                shapeFun.setDiffOrder(1);
                simpCriterion.setDiffOrder(1);
                for (Coordinate center : centers) {
                    simpCriterion.getSupports(center, null, resNodes, distSqs);
                    TDoubleArrayList[] funVals = shapeFun.values(center,resNodes, distSqs,rads,null);
                    double[] funValsSum = new double[3];
                    for (int k = 0; k < 3; k++) {
                        funValsSum[k] = funVals[k].sum();
                    }
                    double[] expFunValsSum = new double[]{1, 0, 0};
                    try {
                        assertArrayEquals(expFunValsSum, funValsSum, 1e-10);
                    } catch (AssertionError e) {
                        throw e;
                    }
                }
            }
        }
    }
}
