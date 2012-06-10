/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseVector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 *
 * @author epsilon
 */
public class CommonPostProcessor {

    private ShapeFunction shapeFunction;
    int dim;
    public int defaultNodesSize = 50;
    DenseVector nodesVals;

    public CommonPostProcessor(ShapeFunction shapeFunction, DenseVector nodesVals, int dim) {
        this.shapeFunction = shapeFunction;
        this.dim = dim;
        this.nodesVals = nodesVals;
        //TODO 3Dissue
        if (3 == dim) {
            throw new UnsupportedOperationException();
        }
    }

    public CommonPostProcessor(ShapeFunction shapeFunction, DenseVector nodesVals) {
        this.shapeFunction = shapeFunction;
        this.dim = 2;
        this.nodesVals = nodesVals;
    }

    public List<TDoubleArrayList> displacements(List<? extends Coordinate> coords, List<? extends Boundary> coordBnds) {
        return displacements(coords, coordBnds, 0, null);
    }

    public List<TDoubleArrayList> displacements(List<? extends Coordinate> coords, List<? extends Boundary> coordBnds, int partDiffOrder) {
        return displacements(coords, coordBnds, partDiffOrder, null);
    }

    public List<TDoubleArrayList> displacements(List<? extends Coordinate> coords, List<? extends Boundary> coordBnds, int partDiffOrder, List<TDoubleArrayList> results) {
        if (dim > 2) {
            //TODO 3Dissue
            throw new UnsupportedOperationException();
        }

        shapeFunction.setDiffOrder(partDiffOrder);
        results = initResults(results, partDiffOrder, coords.size());
        ArrayList<Node> shapeFunNodes = new ArrayList<>(defaultNodesSize);
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(partDiffOrder);//
        TDoubleArrayList us = results.get(0);
        TDoubleArrayList vs = results.get(1);
        TDoubleArrayList u_xs = null, u_ys = null, v_xs = null, v_ys = null;
        if (partDiffOrder > 0) {
            u_xs = results.get(2);
            u_ys = results.get(3);
            v_xs = results.get(4);
            v_ys = results.get(5);
        }
        Iterator<? extends Boundary> bndIter = (coordBnds != null ? coordBnds.iterator() : null);

        for (Coordinate coord : coords) {
            Boundary bnd = (bndIter != null ? bndIter.next() : null);
            shapeFunction.values(coord, bnd, shapeFunVals, shapeFunNodes);

            
            double u = 0, v = 0;
            double u_x = 0, u_y = 0, v_x = 0, v_y = 0;
            TDoubleArrayList shapeFunVals_0 = shapeFunVals[0];
            TDoubleArrayList shape_x = null, shape_y = null;
            if (partDiffOrder > 0) {
                shape_x = shapeFunVals[1];
                shape_y = shapeFunVals[2];
            }
            int nodeCount = 0;
            for (Node nd : shapeFunNodes) {
                int index = nd.id * 2;

                double shapeValue = shapeFunVals_0.getQuick(nodeCount);
                double nd_u = nodesVals.get(index);
                double nd_v = nodesVals.get(index + 1);
                u += shapeValue * nd_u;
                v += shapeValue * nd_v;
                if (partDiffOrder > 0) {
                    double shapeValue_x = shape_x.getQuick(nodeCount);
                    double shapeValue_y = shape_y.getQuick(nodeCount);
                    u_x += shapeValue_x * nd_u;
                    u_y += shapeValue_y * nd_u;
                    v_x += shapeValue_x * nd_v;
                    v_y += shapeValue_y * nd_v;
                }
                nodeCount++;
            }
            us.add(u);
            vs.add(v);
            if (partDiffOrder > 0) {
                u_xs.add(u_x);
                u_ys.add(u_y);
                v_xs.add(v_x);
                v_ys.add(v_y);
            }
        }
        return results;
    }

    public TDoubleArrayList[] initShapeFunVals(int partDiffOrder) {
        if (dim == 2) {
            return ShapeFunctions2D.initOutputResult(partDiffOrder);
        } else {
            //TODO 3Dissue
            throw new UnsupportedOperationException();
        }
    }

    private List<TDoubleArrayList> initResults(List<TDoubleArrayList> results, int partDiffOrder, int size) {
        if (null == results) {
            results = new ArrayList<>(size);
        }

        int resDim = resultsDim(dim, partDiffOrder);
        for (int i = 0; i < results.size() && i < resDim; i++) {
            TDoubleArrayList res = results.get(i);
            res.resetQuick();
            res.ensureCapacity(size);
        }
        for (int i = results.size(); i < resDim; i++) {
            TDoubleArrayList res = new TDoubleArrayList(size);
            results.add(res);
        }
        return results;
    }

    public static int resultsDim(int dim, int partDiffOrder) {
        if (dim == 3) {
            throw new UnsupportedOperationException();
        }

        switch (partDiffOrder) {
            case 0:
                return 2;
            case 1:
                return 6;
            default:
                throw new UnsupportedOperationException();
        }

    }

    public static List<TDoubleArrayList> initStressStrain2DResults(int size, List<TDoubleArrayList> result) {
        if (null == result) {
            result = new ArrayList<>(3);
        }
        for (int i = 0; i < result.size() && i < 3; i++) {
            TDoubleArrayList tds = result.get(i);
            tds.resetQuick();
            tds.ensureCapacity(size);
        }
        for (int i = result.size(); i < 3; i++) {
            result.add(new TDoubleArrayList(size));
        }
        return result;
    }

    public static List<TDoubleArrayList> stress2D(List<TDoubleArrayList> dispPDs, int startIndex, DenseMatrix64F constutiveLaw) {
        return stress2D(dispPDs, startIndex, constutiveLaw, null);
    }

    public static List<TDoubleArrayList> stress2D(List<TDoubleArrayList> dispPDs, int startIndex, DenseMatrix64F constutiveLaw, List<TDoubleArrayList> results) {
        results = initStressStrain2DResults(dispPDs.get(0).size(), results);
        double[] strain_arr = new double[3], stress_arr = new double[3];
        DenseMatrix64F strain = DenseMatrix64F.wrap(3, 1, strain_arr);
        DenseMatrix64F stress = DenseMatrix64F.wrap(3, 1, stress_arr);
        TDoubleArrayList u_xs = dispPDs.get(0 + startIndex), u_ys = dispPDs.get(1 + startIndex), v_xs = dispPDs.get(2 + startIndex), v_ys = dispPDs.get(3 + startIndex);
        TDoubleArrayList res_0 = results.get(0), res_1 = results.get(1), res_2 = results.get(2);
        for (int i = 0; i < u_xs.size(); i++) {
            double u_x = u_xs.getQuick(i);
            double u_y = u_ys.getQuick(i);
            double v_x = v_xs.getQuick(i);
            double v_y = v_ys.getQuick(i);
            strain_arr[0] = u_x;
            strain_arr[1] = v_y;
            strain_arr[2] = u_y + v_x;
            CommonOps.mult(constutiveLaw, strain, stress);
            res_0.add(stress_arr[0]);
            res_1.add(stress_arr[1]);
            res_2.add(stress_arr[2]);
        }
        return results;
    }
    
    public static List<TDoubleArrayList> strain2D(List<TDoubleArrayList> dispPDs, int startIndex){
        return strain2D(dispPDs, startIndex, null);
    }
    
    public static List<TDoubleArrayList> strain2D(List<TDoubleArrayList> dispPDs, int startIndex, List<TDoubleArrayList> results) {
        results = initStressStrain2DResults(dispPDs.get(0).size(), results);
        TDoubleArrayList u_xs = dispPDs.get(0 + startIndex), u_ys = dispPDs.get(1 + startIndex), v_xs = dispPDs.get(2 + startIndex), v_ys = dispPDs.get(3 + startIndex);
        TDoubleArrayList res_0 = results.get(0), res_1 = results.get(1), res_2 = results.get(2);
        for (int i = 0; i < u_xs.size(); i++) {
            double u_x = u_xs.getQuick(i);
            double u_y = u_ys.getQuick(i);
            double v_x = v_xs.getQuick(i);
            double v_y = v_ys.getQuick(i);
            double xx = u_x;
            double yy = v_y;
            double xy = u_y + v_x;

            res_0.add(xx);
            res_1.add(yy);
            res_2.add(xy);
        }
        return results;
    }
}
