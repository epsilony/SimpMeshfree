/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.NodeSupportDomainSizer;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizers;
import net.epsilony.geom.Coordinate;
import no.uib.cipr.matrix.Vector;
import java.util.ArrayList;
import java.util.Random;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D.MLS;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
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
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2DTest {

    /**
     * w=(1-r^2)^2
     * wx=4*(r^2-1)(x-xn)/260^2
     */
    WeightFunction weightFunction = new WeightFunction() {

        double supportRad = 360;
        double supportRadSquare = 360 * 360;

        @Override
        public double[] values(Node node, Coordinate point, double supportRad, double[] results) {
            Coordinate coord = node.coordinate;
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
        public void setOrders(PartDiffOrd[] types) {
            
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
        NodeSupportDomainSizer supportDomainSizer=new NodeSupportDomainSizers.ConstantSizer(1000);
        
        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(1), BivariateCompletePolynomial.partialXFactory(1), BivariateCompletePolynomial.partialYFactory(1)},new BoundaryBasedCriterions2D.Visible(supportDomainSizer), new NodeSupportDomainSizer() {

            @Override
            public double getRadium(Node node) {
                return 360;
            }

            @Override
            public double getRadiumSquare(Node node) {
                return 360*360;
            }

            @Override
            public double getMaxRadium() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        mls.setOrders(new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()});
        mls.setCacheRange(8, 10);

        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206};
        double[] nodesTestValue = new double[9];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = linearTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node> nodes = new ArrayList<>(9);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node t = new Node(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        DenseVector[] results = new DenseVector[3];
        ArrayList<Node> filtedNodes=new ArrayList<>();
        mls.values(new Coordinate(50, 55), nodes, null, results,filtedNodes);
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        DenseVector vec=new DenseVector(results[0], false);
        double act = vec.dot(nodesTestValueVector);
        double exp = linearTestFun(50, 55);
        assertEquals(exp, act, 0.00001);
         vec=new DenseVector(results[1], false);
        act = vec.dot(nodesTestValueVector);
        exp = 3.2;
        assertEquals(exp, act, 0.00001);
         vec=new DenseVector(results[2], false);
        act = vec.dot(nodesTestValueVector);
        exp = 6.5;
        assertEquals(exp, act, 0.00001);

        for (int i = 0; i < testNum; i++) {
            Random rand = new Random();
            double x = rand.nextDouble() * 200;
            double y = rand.nextDouble() * 200;
            mls.values(new Coordinate(x, y), nodes, null, results,filtedNodes);
            exp = linearTestFun(x, y);
            vec=new DenseVector(results[0], false);
            act = vec.dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = 3.2;
             vec=new DenseVector(results[1], false);
            act = vec.dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = 6.5;
             vec=new DenseVector(results[2], false);
            act = vec.dot(nodesTestValueVector);
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
        NodeSupportDomainSizer supportDomainSizer=new NodeSupportDomainSizers.ConstantSizer(1000);
        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(3), BivariateCompletePolynomial.partialXFactory(3), BivariateCompletePolynomial.partialYFactory(3)},new BoundaryBasedCriterions2D.Visible(supportDomainSizer), new NodeSupportDomainSizers.ConstantSizer(360));

        mls.setOrders(new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()});
        mls.setCacheRange(8, 14);

        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206, 0, 300, 100, 300, 200, 300, 300, 300, 300, 200, 300, 100, 300, 0};
        double[] nodesTestValue = new double[nodesXYs.length / 2];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = cubicTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node> nodes = new ArrayList<>(nodesTestValue.length);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node t = new Node(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        DenseVector[] results = new DenseVector[3];
        ArrayList<Node> filteredNodes=new ArrayList<>();
        mls.values(new Coordinate(50, 55), nodes, null, results,filteredNodes);
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        double exp, act;
        System.out.println("nodesTestValueVector = " + nodesTestValueVector);
        for (int i = 0; i < testNum; i++) {
            Random rand = new Random();
            double x = rand.nextDouble() * 300;
            double y = rand.nextDouble() * 300;
            mls.values(new Coordinate(x, y), nodes, null, results,filteredNodes);
            exp = cubicTestFun(x, y);
             DenseVector vec=new DenseVector(results[0], false);
            act = vec.dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = cubicXTestFun(x, y);
            vec=new DenseVector(results[1], false);
            act = vec.dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
            exp = cubicYTestFun(x, y);
            vec=new DenseVector(results[2], false);
            act = vec.dot(nodesTestValueVector);
            assertEquals(exp, act, 0.00001);
        }

    }

    @Test
    public void testSinFun() {
        System.out.println("start sin function reproduction test");
        int testNum = 5;
       NodeSupportDomainSizer supportDomainSizer=new NodeSupportDomainSizers.ConstantSizer(1000);
        MLS mls = new ShapeFunctions2D.MLS(weightFunction,new BivariateArrayFunction[]{BivariateCompletePolynomial.factory(3), BivariateCompletePolynomial.partialXFactory(3), BivariateCompletePolynomial.partialYFactory(3)},new BoundaryBasedCriterions2D.Visible(supportDomainSizer), new NodeSupportDomainSizers.ConstantSizer(360));
        mls.setOrders(new PartDiffOrd[]{PartDiffOrd.ORI(), PartDiffOrd.X(), PartDiffOrd.Y()});
        mls.setCacheRange(8, 14);


        mls.boundaryBasedCriterion=new BoundaryBasedCriterions2D.Visible(supportDomainSizer);
 
        double[] nodesXYs = new double[]{2, 3, 98, 2, 199, 4, -5, 95, 99, 95, 202, 97, 4, 210, 101, 199, 196, 206, 0, 300, 100, 300, 200, 300, 300, 300, 300, 200, 300, 100, 300, 0};
        double[] nodesTestValue = new double[nodesXYs.length / 2];

        for (int i = 0; i < nodesTestValue.length; i++) {
            nodesTestValue[i] = sinTestFun(nodesXYs[i * 2], nodesXYs[i * 2 + 1]);
        }

        ArrayList<Node> nodes = new ArrayList<>(nodesTestValue.length);
        for (int i = 0; i < nodesXYs.length; i += 2) {
            Node t = new Node(nodesXYs[i], nodesXYs[i + 1]);
            nodes.add(t);
        }
        DenseVector[] results = new DenseVector[3];
        DenseVector nodesTestValueVector = new DenseVector(nodesTestValue, false);
        double exp, act;
        ArrayList<Node> filtedNodes=new ArrayList<>();
        for (int i = 0; i < testNum; i++) {
            for (int j = 0; j < testNum; j++) {
                double x = 300.0 / testNum * i;
                double y = 300.0 / testNum * j;
                mls.values(new Coordinate(x, y), nodes, null, results,filtedNodes);
                exp = sinTestFun(x, y);
                DenseVector vec=new DenseVector(results[0],false);
                act =  vec.dot(nodesTestValueVector);
                System.out.println("ori");
                System.out.println("exp = " + exp);
                System.out.println("act = " + act);
                exp = sinXTestFun(x, y);
                vec=new DenseVector(results[1],false);
                act =  vec.dot(nodesTestValueVector);
                System.out.println("partial x");
                System.out.println("exp = " + exp);
                System.out.println("act = " + act);
                exp = sinYTestFun(x, y);
                vec=new DenseVector(results[2],false);
                act =  vec.dot(nodesTestValueVector);
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
