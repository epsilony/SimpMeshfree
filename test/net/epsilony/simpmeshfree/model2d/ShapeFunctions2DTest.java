/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import no.uib.cipr.matrix.Vector;
import java.util.ArrayList;
import java.util.Random;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D.MLS;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.PartialDiffType;
import net.epsilony.simpmeshfree.model.WeightFunction;
import net.epsilony.simpmeshfree.utils.BivariateArrayFunction;
import net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial;
import no.uib.cipr.matrix.DenseVector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author epsilon
 */
public class ShapeFunctions2DTest {

    /**
     * w=(1-r^2)^2
     * wx=4*(r^2-1)(x-xn)/260^2
     */
    WeightFunction<Coordinate2D> weightFunction = new WeightFunction<Coordinate2D>() {

        double supportRad = 360;
        double supportRadSquare = 360 * 360;

        @Override
        public double[] values(Node<Coordinate2D> node, Coordinate2D point, double[] results) {
            Coordinate2D coord = node.coordinate;
            double xn = coord.x;
            double yn = coord.y;
            double x = point.x;
            double y = point.y;
            double dx = x - xn;
            double dy = y - yn;
            double rSq = dx * dx + dy * dy;
            double srs = supportRadSquare;
            if (rSq >= srs) {
                results[0] = 0;
                results[1] = 0;
                results[2] = 0;
            } else {
                double t = (rSq / srs - 1);
                results[0] = t * t;
                results[1] = -4 * t * dx / srs;
                results[2] = -4 * t * dy / srs;
            }
            return results;
        }

        @Override
        public void setPDTypes(PartialDiffType[] types) {
            
        }
    };

    public ShapeFunctions2DTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLinear() {
        System.out.println("start linear function reproduction test");
        int testNum = 1000;

        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(1), BivariateCompletePolynomial.partialXFactory(1), BivariateCompletePolynomial.partialYFactory(1)});
        mls.setPDTypes(new PartialDiffType[]{PartialDiffType.ORI(), PartialDiffType.X(), PartialDiffType.Y()});
        mls.setCacheRange(8, 10);

        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206};
        double[] nodesTestValue = new double[9];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = linearTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node<Coordinate2D>> nodes = new ArrayList<>(9);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node<Coordinate2D> t = new Node2D(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        Vector[] results = new Vector[3];
        mls.values(new Coordinate2D(50, 55), nodes, null, results);
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        double act = results[0].dot(nodesTestValueVector);
        double exp = linearTestFun(50, 55);
        assertEquals(exp, act, 0.00001);
        act = results[1].dot(nodesTestValueVector);
        exp = 3.2;
        assertEquals(exp, act, 0.00001);
        act = results[2].dot(nodesTestValueVector);
        exp = 6.5;
        assertEquals(exp, act, 0.00001);

        for (int i = 0; i < testNum; i++) {
            Random rand = new Random();
            double x = rand.nextDouble() * 200;
            double y = rand.nextDouble() * 200;
            mls.values(new Coordinate2D(x, y), nodes, null, results);
            exp = linearTestFun(x, y);
            act = results[0].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = 3.2;
            act = results[1].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = 6.5;
            act = results[2].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
        }
    }

    double linearTestFun(double x, double y) {
        return 4.2 + 3.2 * x + 6.5 * y;
    }

    @Test
    public void testCubic() {
        System.out.println("start cubic function reproduction test");
        int testNum = 1000;

        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(3), BivariateCompletePolynomial.partialXFactory(3), BivariateCompletePolynomial.partialYFactory(3)});
        mls.weightFunction = weightFunction;
        mls.setPDTypes(new PartialDiffType[]{PartialDiffType.ORI(), PartialDiffType.X(), PartialDiffType.Y()});
        mls.setCacheRange(8, 14);

        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206, 0, 300, 100, 300, 200, 300, 300, 300, 300, 200, 300, 100, 300, 0};
        double[] nodesTestValue = new double[nodesXYs.length / 2];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = cubicTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node<Coordinate2D>> nodes = new ArrayList<>(nodesTestValue.length);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node<Coordinate2D> t = new Node2D(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        Vector[] results = new Vector[3];
        mls.values(new Coordinate2D(50, 55), nodes, null, results);
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        double exp, act;

        for (int i = 0; i < testNum; i++) {
            Random rand = new Random();
            double x = rand.nextDouble() * 300;
            double y = rand.nextDouble() * 300;
            mls.values(new Coordinate2D(x, y), nodes, null, results);
            exp = cubicTestFun(x, y);
            act = results[0].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = cubicXTestFun(x, y);
            act = results[1].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = cubicYTestFun(x, y);
            act = results[2].dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
        }
    }

    @Test
    public void testSinFun() {
        System.out.println("start sin function reproduction test");
        int testNum = 5;

        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(3), BivariateCompletePolynomial.partialXFactory(3), BivariateCompletePolynomial.partialYFactory(3)});
        mls.setPDTypes(new PartialDiffType[]{PartialDiffType.ORI(), PartialDiffType.X(), PartialDiffType.Y()});
        mls.setCacheRange(8, 14);

        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206, 0, 300, 100, 300, 200, 300, 300, 300, 300, 200, 300, 100, 300, 0};
        double[] nodesTestValue = new double[nodesXYs.length / 2];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = sinTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node<Coordinate2D>> nodes = new ArrayList<>(nodesTestValue.length);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node<Coordinate2D> t = new Node2D(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        Vector[] results = new Vector[3];
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        double exp, act;
        for (int i = 0; i < testNum; i++) {
            for (int j = 0; j < testNum; j++) {
                double x = 300.0 / testNum * i;
                double y = 300.0 / testNum * j;
                mls.values(new Coordinate2D(x, y), nodes, null, results);
                exp = sinTestFun(x, y);
                act = results[0].dot(nodesTestValueVector);
                System.out.println("ori");
                System.out.println("exp = " + exp);
                System.out.println("act = " + act);
                exp = sinXTestFun(x, y);
                act = results[1].dot(nodesTestValueVector);
                System.out.println("partial x");
                System.out.println("exp = " + exp);
                System.out.println("act = " + act);
                exp = sinYTestFun(x, y);
                act = results[2].dot(nodesTestValueVector);
                System.out.println("partial y");
                System.out.println("exp = " + exp);
                System.out.println("act = " + act);
            }
        }
    }

    double cubicTestFun(double x, double y) {
        return 3.5 + 4.2 * x + 6.3 * y + 2.2 * x * x + 1.7 * x * y + 3.9 * y * y + 7.2 * x * x * x + 0.7 * x * x * y + 0.2 * x * y * y + y * y * y;
    }

    double cubicXTestFun(double x, double y) {
        return 4.2 + 4.4 * x + 1.7 * y + 21.6 * x * x + 1.4 * x * y + 0.2 * y * y;
    }

    double cubicYTestFun(double x, double y) {
        return 6.3 + 1.7 * x + 7.8 * y + 0.7 * x * x + 0.4 * x * y + 3 * y * y;
    }

    double sinTestFun(double x, double y) {
        return Math.sin(x/300) *Math.sin(y/300);
    }

    double sinXTestFun(double x, double y) {
        return Math.cos(x/300) * Math.sin(y/300)/300;
    }

    double sinYTestFun(double x, double y) {
        return Math.sin(x/300 ) * Math.cos(y/300)/300;
    }
}
