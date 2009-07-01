/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.io.Serializable;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author epsilon
 */
public interface ModelCore extends Serializable{
    void accurateEssentialCore(double[] values, int index, byte flag, FlexCompRowMatrix matrix,DenseVector vector);
    void quadrateCore(int mIndex, double dphim_dx, double dphim_dy, int nIndex, double dphin_dx, double dphin_dy, double coefs, FlexCompRowMatrix matrix);
    void natureConBoundaryQuadrateCore(double[] values, int index, double phi, DenseVector vector);
    void natureBoundaryQudarateCore(double[] values, int index, double phi, double coef, DenseVector vector);
     FlexCompRowMatrix initialKMatrix(int nodesSize);
}
