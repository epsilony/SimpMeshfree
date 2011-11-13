/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface Solver {
    Vector Solve(Matrix mat,Vector vec,Vector result);
}
