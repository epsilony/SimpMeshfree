/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author epsilon
 */
public class CommonPostProcessor {

    private ShapeFunction shapeFunction;
    int dim;
    public int defaultNodesSize = 50;
    DenseVector nodesVals;

    public CommonPostProcessor(ShapeFunction shapeFunction, DenseVector nodesVals,int dim) {
        this.shapeFunction = shapeFunction;
        this.dim = dim;
        this.nodesVals = nodesVals;
        //TODO 3Dissue
        if(3==dim){
            throw new UnsupportedOperationException();
        }
    }
    
    public CommonPostProcessor(ShapeFunction shapeFunction, DenseVector nodesVals) {
        this.shapeFunction = shapeFunction;
        this.dim = 2;
        this.nodesVals = nodesVals;
    }
    
    public List<double[]> result(List<? extends Coordinate> coords, List<? extends Boundary> coordBnds){
        return result(coords, coordBnds, 1);
    }
    
    public List<double[]> result(List<? extends Coordinate> coords, List<? extends Boundary> coordBnds, int partDiffOrder) {

        shapeFunction.setDiffOrder(partDiffOrder);
        LinkedList<double[]> results = new LinkedList<>();
        ArrayList<Node> shapeFunNodes = new ArrayList<>(defaultNodesSize);
        Iterator<? extends Boundary> bndIter = (coordBnds != null ? coordBnds.iterator() : null);
        TDoubleArrayList[] shapeFunVals = initShapeFunVals(partDiffOrder);//
        for (Coordinate coord : coords) {
            Boundary bnd = (bndIter != null ? bndIter.next() : null);


            shapeFunction.values(coord, bnd, shapeFunVals, shapeFunNodes);

            double[] result = new double[2];
            int nodeCount = 0;
            for (Node nd : shapeFunNodes) {
                int index = nd.id * 2;
                double shapeValue = shapeFunVals[0].get(nodeCount++);
                result[0] += shapeValue * nodesVals.get(index);
                result[1] += shapeValue * nodesVals.get(index + 1);
            }
            results.add(result);
        }
        return results;
    }

    private TDoubleArrayList[] initShapeFunVals(int partDiffOrder) {
        if (dim == 2) {
            return ShapeFunctions2D.initOutputResult(partDiffOrder);
        } else {
            //TODO 3Dissue
            throw new UnsupportedOperationException();
        }
    }
}
