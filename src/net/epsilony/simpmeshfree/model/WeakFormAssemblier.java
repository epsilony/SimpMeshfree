/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author epsilon
 */
public interface WeakFormAssemblier {
void asmBalance(QuadraturePoint qp,List<Node> nodes,Vector[] vectors,BoundaryCondition volBc);
void asmDirichlet(BCQuadraturePoint qp,List<Node> nodes,Vector[] vectors);
void asmNeumann(BCQuadraturePoint qp,List<Node> nodes,Vector[] vectors);
Matrix getConstitutiveLaw(Coordinate pos);
Matrix getEquationMatrix();
Vector getEquationVector();
}
