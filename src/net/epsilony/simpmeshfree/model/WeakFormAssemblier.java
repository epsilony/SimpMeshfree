/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.Avatarable;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakFormAssemblier extends Avatarable<WeakFormAssemblier>{

    void asmBalance(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions, VolumeCondition volBc);

    void asmNeumann(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions);

    void asmDirichlet(BCQuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunctions);

    DenseMatrix getConstitutiveLaw(Coordinate pos);

    Matrix getEquationMatrix();

    DenseVector getEquationVector();
}
