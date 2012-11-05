/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.List;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.utils.geom.Node;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.ejml.data.DenseMatrix64F;

/**
 *
 * @author epsilon
 */
public class LagrangeAssemblier2D extends SimpAssemblier2D {

    FlexCompColMatrix g;
    DenseVector q;
    TIntIntHashMap indexMap;
    int dirichletNdsSize;

    public LagrangeAssemblier2D(DenseMatrix64F constitutiveLaw, int nodesSize, int dirichletNdsSize) {
        super(constitutiveLaw, 0, nodesSize);
        init(dirichletNdsSize);
    }

    public LagrangeAssemblier2D(DenseMatrix constitutiveLaw, int nodesSize, int dirichletNdsSize) {
        super(constitutiveLaw, 0, nodesSize);
        init(dirichletNdsSize);
    }

    private void init(int dirichletNdsSize) {
        this.dirichletNdsSize = dirichletNdsSize;
        g = new FlexCompColMatrix(mainMatrix.numRows(), dirichletNdsSize * 2);
        q = new DenseVector(dirichletNdsSize * 2);
        indexMap = new TIntIntHashMap(dirichletNdsSize);
    }

    @Override
    public synchronized WeakformAssemblier produce() {
        LagrangeAssemblier2D res = new LagrangeAssemblier2D(constitutiveLaw, modelNdsSize, dirichletNdsSize);
        return res;
    }

    @Override
    public Matrix getEquationMatrix() {
        if (mainMatrix.numRows() == modelNdsSize * 2) {
            asmKGQ();
        }
        return mainMatrix;
    }

    @Override
    public DenseVector getEquationVector() {
        if (mainMatrix.numRows() == modelNdsSize * 2) {
            asmKGQ();
        }
        return mainVector;
    }

    @Override
    public void asmDirichlet(QuadraturePoint qp, List<Node> nodes, TDoubleArrayList[] shapeFunctions) {
        LineBoundary bnd = (LineBoundary) qp.boundary;
        Node n0 = bnd.start;
        Node n1 = bnd.end;
        double[] ns = linearLagrange(n0, n1, qp.coordinate);
        TDoubleArrayList phis = shapeFunctions[0];
        double n_0 = ns[0];
        double n_1 = ns[1];
        int col_index_0;
        int col_index_1;
        if (indexMap.containsKey(n0.id)) {
            int id = indexMap.get(n0.id);
            col_index_0 = id * 2;
        } else {
            int id = indexMap.size();
            indexMap.put(n0.id, id);
            col_index_0 = id * 2;
        }
        if (indexMap.containsKey(n1.id)) {
            int id = indexMap.get(n1.id);
            col_index_1 = id * 2;
        } else {
            int id = indexMap.size();
            indexMap.put(n1.id, id);
            col_index_1 = id * 2;
        }
        int i = 0;
        double weight = qp.weight;
        for (Node nd : nodes) {
            double phi_i = phis.getQuick(i);
            double g_i0 = -weight * phi_i * n_0;
            double g_i1 = -weight * phi_i * n_1;
            int row_index = nd.id * 2;
            if (qp.validities[0]) {
                g.add(row_index, col_index_0, g_i0);
                g.add(row_index, col_index_1, g_i1);
            }
            if (qp.validities[1]) {
                g.add(row_index + 1, col_index_0 + 1, g_i0);
                g.add(row_index + 1, col_index_1 + 1, g_i1);
            }
            i++;
        }
        double q_i0 = -weight * n_0;
        double q_i1 = -weight * n_1;
        if (qp.validities[0]) {
            q.add(col_index_0, q_i0 * qp.values[0]);
            q.add(col_index_1, q_i1 * qp.values[0]);
        }
        if (qp.validities[1]) {
            q.add(col_index_0 + 1, q_i0 * qp.values[1]);
            q.add(col_index_1 + 1, q_i1 * qp.values[1]);
        }
    }

    @Override
    public void uniteIn(WeakformAssemblier w) {
        LagrangeAssemblier2D l = (LagrangeAssemblier2D) w;
        g.add(l.g);
        q.add(l.q);
        super.uniteIn(w);
    }

    private void asmKGQ() {
        int sum = 0;
        for (int j = 0; j < q.size(); j++) {
            SparseVector col = g.getColumn(j);
            if (col.getUsed() > 0) {
                sum++;
            }
        }
        FlexCompRowMatrix oldMainMat = mainMatrix;
        DenseVector oldMainVec = mainVector;
        mainMatrix = new FlexCompRowMatrix(oldMainMat.numRows() + sum, oldMainMat.numColumns() + sum);
        mainVector = new DenseVector(oldMainVec.size() + sum);
        for (MatrixEntry me : oldMainMat) {
            mainMatrix.set(me.row(), me.column(), me.get());
        }
        for (VectorEntry ve : oldMainVec) {
            mainVector.set(ve.index(), ve.get());
        }
        int new_index = oldMainVec.size();
        for (int j = 0; j < q.size(); j++) {
            SparseVector col = g.getColumn(j);
            if (col.getUsed() > 0) {
                for (VectorEntry ve : col) {
                    mainMatrix.set(ve.index(), new_index, ve.get());
                }
                mainVector.set(new_index, q.get(j));
                new_index++;
            }
        }
    }

    public static double[] linearLagrange(Node n0, Node n1, Coordinate coord) {
        double d0 = GeometryMath.distance(n0, coord);
        double d1 = GeometryMath.distance(n1, coord);
        double l = GeometryMath.distance(n0, n1);
        return new double[]{d1 / l, d0 / l};
    }
}
