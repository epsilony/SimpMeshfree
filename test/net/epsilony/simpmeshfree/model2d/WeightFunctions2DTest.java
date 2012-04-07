/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.WeightFunction;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.utils.geom.Coordinate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeightFunctions2DTest {
    
    public WeightFunctions2DTest() {
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
    public void testSimpPower() {

        double radium=6;
        WeightFunction weightFunction=new WeakFormProcessor2DTest.TempWeightFunction(radium);
        WeightFunction sampleFunction=new WeightFunctions2D.SimpPower(2);
        Node testNode=new Node(4, 3);
        Coordinate testCoord=new Coordinate(0, 1);
        final PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI(),PartDiffOrd.X(),PartDiffOrd.Y()};
        weightFunction.setOrders(types);
        sampleFunction.setOrders(types);
        
        double[] expResults=weightFunction.values(testNode, testCoord, radium, new double[3]);
        double[] actResults=sampleFunction.values(testNode, testCoord, radium, new double[3]);
        
        assertArrayEquals(expResults, actResults, 1e-6);
    }
    
    @Test
    public void testTriSpline(){
        double radium=6;
        WeightFunction sampleFunction=new WeightFunctions2D.TriSpline();
        Node testNode=new Node(4, 3);
        Coordinate testCoord=new Coordinate(0, 1);
        final PartDiffOrd[] types = new PartDiffOrd[]{PartDiffOrd.ORI(),PartDiffOrd.X(),PartDiffOrd.Y()};
        sampleFunction.setOrders(types);
        double[] expResult=new double[]{0.022016035555887936,0.03866523511102389,0.019332617555511945};
        double[] actResult=sampleFunction.values(testNode, testCoord, radium, new double[3]);
        assertArrayEquals(expResult, actResult, 1e-6);
    }
}
