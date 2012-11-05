/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.SomeFactory;
import net.epsilony.utils.Unitable;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakformAssemblier extends SomeFactory<WeakformAssemblier>, Unitable<WeakformAssemblier> {

    void asmBalance(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals, VolumeCondition volBc);

    void asmNeumann(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals);

    void asmDirichlet(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunVals);

    Matrix getEquationMatrix();
    
    DenseVector getEquationVector();
}
