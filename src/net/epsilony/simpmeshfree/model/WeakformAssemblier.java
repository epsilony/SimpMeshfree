/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.simpmeshfree.utils.Avatarable;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakformAssemblier extends Avatarable<WeakformAssemblier>{

    void asmBalance(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunVals, VolumeCondition volBc);

    void asmNeumann(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunVals);

    void asmDirichlet(QuadraturePoint qp, List<Node> nodes, DenseVector[] shapeFunVals);

    DenseMatrix getConstitutiveLaw(Coordinate pos);

    Matrix getEquationMatrix();

    DenseVector getEquationVector();
}
