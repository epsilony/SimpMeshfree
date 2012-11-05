/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.ArrayList;
import java.util.Arrays;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.utils.geom.Node;
import net.epsilony.simpmeshfree.model2d.RectangleModel;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import static net.epsilony.utils.geom.GeometryMath.*;
import net.epsilony.utils.geom.Triangle;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author epsilon
 */
public class QuadraturePointIteratorsTest {

    public QuadraturePointIteratorsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public ArrayList<LineBoundary> sampleLines() {
        double[][] linePoints = new double[][]{
            {0.225234515939, 0.969885142122, 0.227100754109},
            {0.147692907405, 0.936153998508, 0.65765424313},
            {0.376300988085, 0.932642244385, 0.539442390686},
            {0.0411711385213, 0.836491084811, 0.622549035592}};
        ArrayList<LineBoundary> lines = new ArrayList<>();
        for (int i = 0; i < linePoints.length; i++) {
            double[] startDs = linePoints[i];
            double[] endDs = linePoints[(i + 1) % linePoints.length];
            Node start = new Node(new Coordinate(startDs));
            Node end = new Node(new Coordinate(endDs));
            LineBoundary line = new LineBoundary(start, end);
            line.id = i + 1;
            lines.add(line);
        }
        return lines;
    }

    /**
     * Test of compoundIterators method, of class QuadraturePointIterators.
     */
    @Test
    public void testCompoundIterators() {
    }

    /**
     * Test of fromLineBoundaries method, of class QuadraturePointIterators.
     */
    @Test
    public void testFromLineBoundaries() {
        ArrayList<LineBoundary> lines = sampleLines();
        int[] powers = new int[]{1, 2, 3, 4};
        for (int power : powers) {
            System.out.println("power = " + power);

            QuadraturePointIterator qpIter = QuadraturePointIterators.fromLineBoundaries(power, lines);
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            int num = 0;
            while (qpIter.next(qp)) {
                num++;
                act += qp.weight;
            }
            System.out.println("num = " + num);
            double expSumLen = 0;
            for (LineBoundary line : lines) {
                expSumLen += GeometryMath.distance(line.start, line.end);
            }
            assertEquals(expSumLen, act, 1e-6);
        }
    }

//    public static double sampleFunction(Coordinate c) {
//        double[] coefs = new double[]{0.23752921, 0.69521027, 0.26294195, 0.55050963, 0.82327928,
//            0.2169608, 0.21277663, 0.10688252, 0.49089585, 0.39184109};
//        double x = c.x, y = c.y, z = c.z;
//        double[] ori = new double[]{1, x, y, z, x * x, y * y, z * z, x * y, y * z, z * x};
//        double result = 0;
//        for (int i = 0; i < coefs.length; i++) {
//            result += coefs[i] * ori[i];
//        }
//        return result;
//    }
    /**
     * Test of fromLineBoundariesAndBC method, of class
     * QuadraturePointIterators.
     */
    @Test
    public void testFromLineBoundariesAndBC() {
        ArrayList<LineBoundary> lines = sampleLines();


        double exp = 0;
        for (LineBoundary line : lines) {
            exp += GeometryMath.distance(line.start, line.end) * Math.sqrt(line.id);
        }
        int[] powers = new int[]{2, 3, 4};

        for (int power : powers) {
            BoundaryCondition bc = sampleBC();
            QuadraturePoint qp = new QuadraturePoint();
            QuadraturePointIterator bcIter = QuadraturePointIterators.fromLineBoundariesAndBC(power, lines, bc);
            double act = 0;
            while (bcIter.next(qp)) {
                int i = 0;
                for (boolean bl : qp.validities) {
                    if (bl) {
                        act += qp.weight * qp.values[i];
                    }
                    i++;
                }
            }
            assertEquals(exp, act, 1e-6);
        }

        exp = 0;
        double a = 1.021, b = -3.42, c = 2.07;

        for (LineBoundary line : lines) {
            Node start = line.start;
            Node end = line.end;
            exp += dot(a, b, c, (scale(add(end, start), 0.5))) * distance(end, start);
        }

        for (int power : powers) {
            BoundaryCondition bc = sampleBC2(a, b, c);
            QuadraturePoint qp = new QuadraturePoint();
            QuadraturePointIterator bcIter = QuadraturePointIterators.fromLineBoundariesAndBC(power, lines, bc);
            double act = 0;
            while (bcIter.next(qp)) {
                int i = 0;
                for (boolean bl : qp.validities) {
                    if (bl) {
                        assertEquals(true, i < 1);
                        act += qp.weight * qp.values[i];
                    }
                    i++;
                }
            }
            assertEquals(exp, act, 1e-6);
        }

    }

    BoundaryCondition sampleBC() {
        return new BoundaryCondition() {
            Boundary bnd = null;

            @Override
            public boolean setBoundary(Boundary bnd) {
                this.bnd = bnd;
                return true;
            }

            @Override
            public void values(Coordinate input, double[] results, boolean[] validities) {
                results[0] = Math.sqrt(bnd.getId());
                validities[0] = true;
                for (int i = 1; i < validities.length; i++) {
                    validities[i] = false;
                }
            }
        };
    }

    BoundaryCondition sampleBC2(final double a, final double b, final double c) {
        return new BoundaryCondition() {
            double[] abc = new double[]{a, b, c};
            Boundary bnd = null;

            @Override
            public boolean setBoundary(Boundary bnd) {
                this.bnd = bnd;
                return true;
            }

            @Override
            public void values(Coordinate input, double[] results, boolean[] validities) {
                Arrays.fill(validities, false);
                results[0] = 0;
                for (int i = 0; i < 3; i++) {
                    results[0] += input.getDim(i) * abc[i];
                }
                validities[0] = true;
            }
        };
    }

    /**
     * Test of fromQuadrangles method, of class QuadraturePointIterators.
     */
    @Test
    public void testFromQuadrangles() {
        double width = 105;
        double height = 33;
        double lineSize = 5;
        double spaceNdsDis = 3;
        double a = 1.1, b = -2.35;
        RectangleModel rm = new RectangleModel(width, height, lineSize, spaceNdsDis);
        double exp = width * height;
        int[] powers = new int[]{2, 3, 4, 5};
        for (int power : powers) {
            QuadraturePointIterator quadIter = QuadraturePointIterators.fromQuadrangles(power, rm.quadrangles());
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (quadIter.next(qp)) {
                act += qp.weight;
            }
            assertEquals(exp, act, 1e-6);
        }

        exp = 0.5 * a * width * width * height;
        for (int power : powers) {
            QuadraturePointIterator quadIter = QuadraturePointIterators.fromQuadrangles(power, rm.quadrangles());
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (quadIter.next(qp)) {
                act += qp.weight * (a * qp.coordinate.x + b * qp.coordinate.y);
            }
            assertEquals(exp, act, 1e-6);
        }
    }

    /**
     * Test of fromTriangles method, of class QuadraturePointIterators.
     */
    @Test
    public void testFromTriangles() {
        double width = 105;
        double height = 33;
        double lineSize = 2;
        double spaceNdsDis = 3;
        double a = 1.1, b = -2.35;
        RectangleModel rm = new RectangleModel(width, height, lineSize, spaceNdsDis);
        double exp = width * height;
        int[] powers = new int[]{2, 3, 4, 5};
        double area = 0;
        for (Triangle tri : rm.triangles()) {
            area += GeometryMath.triangleArea2D(tri.c1.x, tri.c1.y, tri.c2.x, tri.c2.y, tri.c3.x, tri.c3.y);
        }
        assertEquals(exp, area, 1e-6);
        for (int power : powers) {
            QuadraturePointIterator triIter = QuadraturePointIterators.fromTriangles(power, rm.triangles());
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (triIter.next(qp)) {
                act += qp.weight;
            }
            assertEquals(exp, act, 1e-6);
        }

        exp = 0.5 * a * width * width * height;
        for (int power : powers) {
            QuadraturePointIterator triIter = QuadraturePointIterators.fromTriangles(power, rm.triangles());
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (triIter.next(qp)) {
                act += qp.weight * (a * qp.coordinate.x + b * qp.coordinate.y);
            }
            assertEquals(exp, act, 1e-6);
        }
        
                
    }

    @Test
    public void testQuadratureDomainBased() {
        double width = 105;
        double height = 33;
        double lineSize = 2;
        double spaceNdsDis = 3;
        double a = 1.1, b = -2.35;
        RectangleModel rm = new RectangleModel(width, height, lineSize, spaceNdsDis);
        
        int[] powers = new int[]{2, 3, 4, 5};
        double area = 0;
        for (Triangle tri : rm.triangles()) {
            area += GeometryMath.triangleArea2D(tri.c1.x, tri.c1.y, tri.c2.x, tri.c2.y, tri.c3.x, tri.c3.y);
        }
        double exp = width * height;
        assertEquals(exp, area, 1e-6);
        for (int power : powers) {
            QuadraturePointIterator triIter=QuadraturePointIterators.fromDomains(QuadratureDomains.fromTriangles(rm.triangles()),power);
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (triIter.next(qp)) {
                act += qp.weight;
            }
            assertEquals(exp, act, 1e-6);
        }

        exp = 0.5 * a * width * width * height;
        for (int power : powers) {
            QuadraturePointIterator triIter=QuadraturePointIterators.fromDomains(QuadratureDomains.fromTriangles(rm.triangles()),power);
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (triIter.next(qp)) {
                act += qp.weight * (a * qp.coordinate.x + b * qp.coordinate.y);
            }
            assertEquals(exp, act, 1e-6);
        }
        
        exp = width * height;
        for (int power : powers) {
            QuadraturePointIterator quadIter=QuadraturePointIterators.fromDomains(QuadratureDomains.fromQuadrangles(rm.quadrangles()),power);
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (quadIter.next(qp)) {
                act += qp.weight;
            }
            assertEquals(exp, act, 1e-6);
        }

        exp = 0.5 * a * width * width * height;
        for (int power : powers) {
            QuadraturePointIterator quadIter=QuadraturePointIterators.fromDomains(QuadratureDomains.fromQuadrangles(rm.quadrangles()),power);
            QuadraturePoint qp = new QuadraturePoint();
            double act = 0;
            while (quadIter.next(qp)) {
                act += qp.weight * (a * qp.coordinate.x + b * qp.coordinate.y);
            }
            assertEquals(exp, act, 1e-6);
        }
    }
}
