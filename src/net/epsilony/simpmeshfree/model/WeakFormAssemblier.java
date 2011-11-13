/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface WeakFormAssemblier<COORD,VCOORD> {
void asmBalance(QuadraturePoint<COORD> qp,List<Node<COORD>> nodes,Vector[] vectors,BoundaryCondition<COORD,VCOORD> volBc);
void asmDirichlet(BCQuadraturePoint<COORD,VCOORD> qp,List<Node<COORD>> nodes,Vector[] vectors);
void asmNeumann(BCQuadraturePoint<COORD,VCOORD> qp,List<Node<COORD>> nodes,Vector[] vectors);
Matrix getConstitutiveLaw(COORD pos);
Matrix getEquationMatrix();
Vector getEquationVector();
}
