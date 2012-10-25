/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun.wcores;

import net.epsilony.simpmeshfree.sfun.DistanceSquareFunction;
import net.epsilony.simpmeshfree.sfun.DistanceSquareFunctions;
import net.epsilony.simpmeshfree.sfun.WeightFunction;
import net.epsilony.simpmeshfree.sfun.WeightFunctionCore;
import net.epsilony.simpmeshfree.sfun.WeightFunctions;
import net.epsilony.utils.geom.Coordinate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author epsilon
 */
public class WendlandWuTest {

    public WendlandWuTest() {
    }

    @Test
    public void UnitTest() {
        WeightFunctionCore[] cores = new WeightFunctionCore[]{new Wendland(Wendland.C2), new Wendland(Wendland.C4), new Wendland(Wendland.C6), new Wu(Wu.C2), new Wu(Wu.C4), new Wu(Wu.C6)};
        double rad = 2.2;
        int testDiffOrder=1;
        double[] testPoints = new double[]{1.1, 0.8, -0.6, 1.4, 2.1, 1.0};
        double[][][] exps = new double[][][]{{
                {0.0737609320357593, -0.252882648555459, -0.183914653494880},
                {0.0337703960333242, 0.0721998418475414, -0.168466297644265},
                {0, 0, 0}},
            {
                {0.0851373136039566, -0.422175971151926, -0.307037069928682},
                {0.0273390072163675, 0.0853747966665850, -0.199207858888773},
                {0, 0, 0}},
            {
                {0.0104034140502232, -0.0676068519562061, -0.0491686196045121},
                {0.00233912265030511,  0.00961697665872130, -0.0224396122036401},
                {0, 0, 0}},
            {
                {0.467944809447935,-1.85739035582176,-1.35082934968855},
                {0.187838138180060,0.473039635126158,-1.10375914862770},
                {0,0,0}
            }
        };

        for (int i = 0; i < exps.length; i++) {
            WeightFunctionCore core = cores[i];
            WeightFunction wFun = WeightFunctions.weightFunction(core);
            wFun.setDiffOrder(testDiffOrder);
            for (int j = 0; j < testPoints.length / 2; j++) {
                double x = testPoints[j * 2];
                double y = testPoints[j * 2 + 1];
                double[] distSqs = new double[]{x * x + y * y, 2 * x, 2 * y};
                double[] value = wFun.value(distSqs, rad, null);
                System.out.println(i + " " + j);
                assertArrayEquals(exps[i][j], value, 1e-10);
            }
        }
    }
}
