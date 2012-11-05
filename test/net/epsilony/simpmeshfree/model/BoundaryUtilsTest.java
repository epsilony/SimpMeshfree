/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.epsilony.simpmeshfree.model.test.BoundaryUtilsTestUtils;
import net.epsilony.utils.geom.Coordinate;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import static net.epsilony.simpmeshfree.model.test.BoundaryUtilsTestUtils.*;

/**
 *
 * @author epsilon
 */
public class BoundaryUtilsTest {

    public BoundaryUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of isLineBoundarySphereIntersect method, of class BoundaryUtils.
     */
    @Test
    public void testIsBoundaryIntersect_line2D() {
        Object[] sampleData = BoundaryUtilsTestUtils.genLineIntersection2DTestSample();
        boolean[] expResults = (boolean[]) sampleData[0];
        ArrayList<double[]> lines = (ArrayList<double[]>) sampleData[1];
        Coordinate center = (Coordinate) sampleData[2];
        Double rad = (Double) sampleData[3];

        for (int i = 0; i < expResults.length; i++) {
            boolean exp = expResults[i];
            double[] linePt = lines.get(i);
            LineBoundary line = new LineBoundary(new Node(linePt[0], linePt[1]), new Node(linePt[2], linePt[3]));
            boolean act = BoundaryUtils.isLineBoundarySphereIntersect(line, center, rad);
            try {
                assertEquals(exp, act);
            } catch (AssertionError e) {
                System.out.println("i=" + i);
                throw e;
            }
        }
    }

    @Test
    public void testIsBoundaryIntersect_line3D() {
        int testNum = 10;
        for (int testId = 0; testId < testNum; testId++) {
            Object[] samples = genLineIntersection3DTestSample();
            boolean[] expResults = (boolean[]) samples[0];
            List<LineBoundary> lines = (List<LineBoundary>) samples[1];
            Coordinate center = (Coordinate) samples[2];
            Double rad = (Double) samples[3];
            int i = 0;
            for (LineBoundary line : lines) {
                boolean exp = expResults[i];
                boolean act = BoundaryUtils.isLineBoundarySphereIntersect(line, center, rad);
                try {
                    assertEquals(exp,act);
                } catch (AssertionError e) {
                    System.out.println("failed:"+i+" "+line+" "+"center:"+center+" rad:"+rad);
                    throw e;
                }

                i++;
            }
        }
    }
}
