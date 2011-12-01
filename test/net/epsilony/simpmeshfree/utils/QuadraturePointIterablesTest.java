/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.geom.Quadrangle;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterables.TriangleArrayIterable;
import net.epsilony.geom.Triangle;
import java.util.Arrays;
import net.epsilony.util.TriangleJna;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class QuadraturePointIterablesTest {

    public QuadraturePointIterablesTest() {
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

    @Test
    public void testTriangleIterableMethod() {
        System.out.println("Test TriangleIterable");
        double[] rectCorners = new double[]{1, 2, 103, 2, 103, 52, 1, 52};
        int[] pointMarkerlist = new int[]{1, 2, 3, 4};
        int[] segmentlist = new int[]{0, 1, 1, 2, 2, 3, 3, 0};
        int[] segmentmarkerlist = new int[]{1, 2, 3, 4};

        TriangleJna.triangulateio in = TriangleJna.triangulateio.instanceWithoutAttributes(4, rectCorners, pointMarkerlist, 4, segmentlist, segmentmarkerlist, 0, null);
        TriangleJna.triangulateio out = new TriangleJna.triangulateio();
        TriangleJna.LibTriangleJna libTriangleJna = TriangleJna.LibTriangleJna.INSTANCE;
        String s = "zpq20nQ";
        libTriangleJna.triangulate(s, in, out, null);
        double[] points = out.pointlist.getDoubleArray(0, out.numberofpoints * 2);
        int[] triangles = out.trianglelist.getIntArray(0, out.numberoftriangles * 3);
        System.out.println("out.numberoftriangles = " + out.numberoftriangles);
        System.out.println(Arrays.toString(points));
        System.out.println(Arrays.toString(triangles));
        Triangle[] tris = new Triangle[out.numberoftriangles];
        int triCount = 0;
        for (int i = 0; i < triangles.length; i += 3) {
            int index = triangles[i] * 2;
            double x1 = points[index],
                    y1 = points[index + 1];
            index = triangles[i + 1] * 2;
            double x2 = points[index],
                    y2 = points[index + 1];
            index = triangles[i + 2] * 2;
            double x3 = points[index],
                    y3 = points[index + 1];
            Triangle triangle = new Triangle(x1, y1, x2, y2, x3, y3);
            tris[triCount++] = triangle;
        }
        QuadraturePointIterables.TriangleArrayIterable iters = new TriangleArrayIterable(3, tris, false);
        double dx = 102;
        double dy = 50;
        double d2x = 103 * 103 - 1;
        double d2y = 52 * 52 - 4;
        double d3x = 103 * 103 * 103 - 1;
        double d3y = 52 * 52 * 52 - 8;
        double exp = d2x * dy + 1.5 * dx * d2y + 1 / 8.0 * d2x * d2y + 0.1 / 6.0 * d3x * d2y;
        double act = 0;
        int count = 0;
        for (QuadraturePoint qp : iters) {
            double f = fun(qp.coordinate.x, qp.coordinate.y);
            act += f * qp.weight;
            count++;
        }
        System.out.println("count = " + count);
        System.out.println("exp = " + exp);
        System.out.println("act = " + act);
        assertEquals(exp, act, exp * 1e-6);

    }

    public static double fun(double x, double y) {
        return 2 * x + 3 * y + 0.5 * x * y + 0.1 * x * x * y;
    }

    double fun1(double x, double y) {
        return 1;
    }

    @Test
    public void testQuadAndTri() {
        System.out.println("start test Quad and Tri");
        double[] corners = new double[]{1, 2, 51, 5, 60, 75, -1, 70};
//        double[] corners=new double[]{1,2,190,2,203,52,1,52};
//        Quadrangle[] quadrangles=new Quadrangle[1];
        Quadrangle[] quadrangles = new Quadrangle[4];
        quadrangles[0] = new Quadrangle(new double[]{1, 2, 26, 3.5, 30, 30, 0, 36});
        quadrangles[1] = new Quadrangle(new double[]{26, 3.5, 51, 5, 55.5, 40, 30, 30});
        quadrangles[2] = new Quadrangle(new double[]{30, 30, 55.5, 40, 60, 75, 29.5, 72.5});
        quadrangles[3] = new Quadrangle(new double[]{0, 36, 30, 30, 29.5, 72.5, -1, 70});
//        quadrangles[0]=new Quadrangle(corners);
        QuadraturePointIterables.QuadrangleArrayIterable quadIter = new QuadraturePointIterables.QuadrangleArrayIterable(3, quadrangles);
        double actQuadRes = 0;
        int count = 0;
        for (QuadraturePoint qp : quadIter) {
            actQuadRes += qp.weight * fun(qp.coordinate.x, qp.coordinate.y);
            count++;
        }
        System.out.println("count = " + count);
        System.out.println("actQuadRes = " + actQuadRes);

        TriangleJna.triangulateio in = TriangleJna.triangulateio.instanceWithoutAttributes(4, corners, new int[]{1, 2, 3, 4}, 4, new int[]{0, 1, 1, 2, 2, 3, 0, 3}, new int[]{1, 2, 3, 4}, 0, null);
        TriangleJna.triangulateio out = new TriangleJna.triangulateio();
        TriangleJna.triangle("zpqa20nQ", in, out, new TriangleJna.triangulateio());
        Triangle[] triangles = TriangleJna.convert(out);
        TriangleJna.trifree(out);
        QuadraturePointIterables.TriangleArrayIterable triIter = new TriangleArrayIterable(3, triangles, true);
        double actTriRes = 0;
        count = 0;
        for (QuadraturePoint qp : triIter) {
            double x = qp.coordinate.x;
            double y = qp.coordinate.y;
            actTriRes += fun(x, y) * qp.weight;
            count++;
        }
        System.out.println("triangles:");
        System.out.println(Arrays.toString(triangles));
        System.out.println("count = " + count);
        System.out.println("actTriRes = " + actTriRes);
        System.out.println("actQuadRes = " + actQuadRes);
        assertEquals(actTriRes, actQuadRes, actQuadRes * 1e-3);
    }

    @Test
    public void testMayFailTriangle() {
        System.out.println("may fail triangle, maybe a bug");
        double[] corners = new double[]{0, 0, 50, 50, 50, 100, 0, 25};
        TriangleJna.triangulateio in = TriangleJna.triangulateio.instanceWithoutAttributes(4, corners, new int[]{1, 2, 3, 4}, 4, new int[]{0, 1, 1, 2, 2, 3, 0, 3}, new int[]{1, 2, 3, 4}, 0, null);
        TriangleJna.triangulateio out = new TriangleJna.triangulateio();
        //workround follow q with a angle degree
        //TriangleJna.triangle("zpq20nQ", in, out, new TriangleJna.triangulateio());
        TriangleJna.triangle("zpqnQ", in, out, new TriangleJna.triangulateio());
        Triangle[] triangles = TriangleJna.convert(out);
        TriangleJna.trifree(out);
        System.out.println("triangles maybe fail");
        System.out.println(Arrays.toString(triangles));
    }
    
    @Test
    public void testWrapper(){
        System.out.println("test empty triangle quadrature iterable");
        for(QuadraturePoint qp:new TriangleArrayIterable(3, new Triangle[0], true)){
            fail("should not be here");
        }
        
        for(QuadraturePoint qp:new QuadraturePointIterables.QuadrangleArrayIterable(3, new Quadrangle[0])){
            fail("shoudl not be here quad");
        }
        Iterable<QuadraturePoint>[] iterables=new Iterable<>[2];
        iterables[0]=new TriangleArrayIterable(3, new Triangle[0]);
        iterables[1]=new QuadraturePointIterables.QuadrangleArrayIterable(3, new Quadrangle[0]);
        for(QuadraturePoint gp:new QuadraturePointIterables.IterablesWrapper(iterables)){
            fail("shoudl not be here quad");
        }
        iterables[0]=new TriangleArrayIterable(3, new Triangle[0]);
        iterables[1]=new QuadraturePointIterables.QuadrangleArrayIterable(3, new Quadrangle[]{new Quadrangle()});
        int count=0;
        for(QuadraturePoint gp:new QuadraturePointIterables.IterablesWrapper(iterables)){
            count++;
        }
        System.out.println("count1 = " + count);
        iterables[0]=new TriangleArrayIterable(3, new Triangle[]{new Triangle()});
        iterables[1]=new QuadraturePointIterables.QuadrangleArrayIterable(3, new Quadrangle[0]);
        count=0;
        for(QuadraturePoint gp:new QuadraturePointIterables.IterablesWrapper(iterables)){
            count++;
        }
        System.out.println("count2 = " + count);
        iterables[0]=new TriangleArrayIterable(3, new Triangle[]{new Triangle()});
        iterables[1]=new QuadraturePointIterables.QuadrangleArrayIterable(3, new Quadrangle[]{new Quadrangle()});
        count=0;
        for(QuadraturePoint gp:new QuadraturePointIterables.IterablesWrapper(iterables)){
            count++;
        }
        System.out.println("count3 = " + count);
    }
}
