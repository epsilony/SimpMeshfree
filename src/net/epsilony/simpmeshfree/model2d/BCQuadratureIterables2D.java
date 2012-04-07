/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.Iterator;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.GaussLegendreQuadratureUtils;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class BCQuadratureIterables2D {

    public static class ArrayIterable implements Iterable<BCQuadraturePoint> {

        BoundaryCondition[] bcs;
        int power;
        int bcValueDim;

        public ArrayIterable(int power, int bcValueDim, BoundaryCondition[] bcs) {
            this.bcs = bcs;
            this.power = power;
            this.bcValueDim = bcValueDim;
        }

        @Override
        public Iterator<BCQuadraturePoint> iterator() {
            return new ArrayIterator(power, bcValueDim, bcs);
        }
    }

    public static class ArrayIterator implements Iterator<BCQuadraturePoint> {

        int pointSize;
        BoundaryCondition[] bcs;
        double[] points;
        double[] weights;
        int index, bcIndex;
        BoundaryCondition bc;
        BCQuadraturePoint bcQP = new BCQuadraturePoint();
        Coordinate parameter = new Coordinate();
        Coordinate tempCoord = new Coordinate();
        Coordinate[] tempCoords = new Coordinate[]{tempCoord};

        public ArrayIterator(int power, int boundaryConditionValueDim, BoundaryCondition[] bcs) {
            this.bcs = bcs;
            if (null!=bcs&&bcs.length > 0) {
                int numberOfPoint = (int) Math.ceil((power + 1) / 2.0);
                pointSize = numberOfPoint;
                points = GaussLegendreQuadratureUtils.getPositions(numberOfPoint);
                weights = GaussLegendreQuadratureUtils.getWeights(numberOfPoint);
                bc=bcs[0];
                bcQP.boundaryCondition=bc;
                bcQP.parameter=parameter;
            }
        }

        @Override
        public boolean hasNext() {
            if(null==bcs){
                return false;
            }
            if (index < pointSize || bcIndex < bcs.length - 1) {
                return true;
            }
            return false;

        }

        @Override
        public BCQuadraturePoint next() {
            if (index >= pointSize) {
                index = 0;
                bcIndex++;
                bc = bcs[bcIndex];
                bcQP.boundaryCondition=bc;
            }
            double t = (points[index] + 1) / 2;
            Coordinate par = parameter;
            par.x = t;
            Boundary bound=bcQP.boundaryCondition.getBoundary();
            bound.valueByParameter(par, bcQP.coordinate);
            bound.valuePartialByParameter(par, tempCoords);
            double dx_dt = tempCoord.x;
            double dy_dt = tempCoord.y;
            bcQP.weight = weights[index] / 2 * Math.sqrt(dx_dt * dx_dt+dy_dt*dy_dt);

            index++;
            return bcQP;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
