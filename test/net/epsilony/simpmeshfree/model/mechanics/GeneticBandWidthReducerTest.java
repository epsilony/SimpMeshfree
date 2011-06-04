/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import net.epsilony.simpmeshfree.model.mechanics.GeneticBandWidthReducer.Chromosome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author epsilon
 */
public class GeneticBandWidthReducerTest {

    public GeneticBandWidthReducerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getPopulation method, of class GeneticBandWidthReducer.
     */
    @Test
    public void testGetPopulation() {
        System.out.println("getPopulation");
        GeneticBandWidthReducer instance = null;
        Chromosome[] expResult = null;
        Chromosome[] result = instance.getPopulation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generate method, of class GeneticBandWidthReducer.
     */
    @Test
    public void testGenerate() {
        System.out.println("generate");
        GeneticBandWidthReducer instance = null;
        instance.generate();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}