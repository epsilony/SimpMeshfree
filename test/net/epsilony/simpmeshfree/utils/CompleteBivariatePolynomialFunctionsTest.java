/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial.*;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class CompleteBivariatePolynomialFunctionsTest {
    
    public CompleteBivariatePolynomialFunctionsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testfactory() {
        System.out.println("test ori:");
        double[] expResults5=new double[]{1,2,3,4,6,9,8,12,18,27,16,24,36,54,81,32,48,72,108,162,243};
        for (int i = 0; i < 6; i++) {
             BivariateArrayFunction fun=factory(i);
             double[] results=new double[getArrayLength(i)];
             double[] exp=Arrays.copyOf(expResults5, getArrayLength(i));
             fun.value(2, 3, results);
             assertArrayEquals(exp, results, 0.1);
             System.out.println(String.format("x=%d y=%d Arrays=%s", 2,3,Arrays.toString(results)));
        }
    }
    
    @Test
    public void testPartialXFactory() {
        System.out.println("test partial x:");
        double[] expResults5=new double[]{0,1,0,4,3,0,12,12,9,0,32,36,36,27,0,80,96,108,108,81,0};
        for (int i = 0; i < 6; i++) {
             BivariateArrayFunction fun=partialXFactory(i);
             double[] results=new double[getArrayLength(i)];
             double[] exp=Arrays.copyOf(expResults5, getArrayLength(i));
             fun.value(2, 3, results);
             assertArrayEquals(exp, results, 0.1);
             System.out.println(String.format("x=%d y=%d Arrays=%s", 2,3,Arrays.toString(results)));
        }
    }
    
    @Test
    public void testPartialYFactory() {
        System.out.println("test partial y:");
        double[] expResults5=new double[]{0,0,1,0,2,6,0,4,12,27,0,8,24,54,108,0,16,48,108,216,405};
        for (int i = 0; i < 6; i++) {
             BivariateArrayFunction fun=partialYFactory(i);
             double[] results=new double[getArrayLength(i)];
             double[] exp=Arrays.copyOf(expResults5, getArrayLength(i));
             fun.value(2, 3, results);
             assertArrayEquals(exp, results, 0.1);
             System.out.println(String.format("x=%d y=%d Arrays=%s", 2,3,Arrays.toString(results)));
        }
    }
}
