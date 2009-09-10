/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model2D;

import java.util.LinkedList;

/**
 *
 * @author epsilon
 */
public interface ModelPreprocessorCore {
        LinkedList<double[]> getTriangleQuadratureDomains();
        LinkedList<double[]> getQuadQudratureDomains();
        LinkedList<Node> getNodes();
}
