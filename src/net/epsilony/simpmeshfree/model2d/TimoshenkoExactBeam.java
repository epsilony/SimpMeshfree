/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class TimoshenkoExactBeam {

    public class NeumannBoundaryCondition implements BoundaryCondition {

        Boundary boundary;

        public NeumannBoundaryCondition(Boundary boundary) {
            this.boundary = boundary;
        }

        @Override
        public Boundary getBoundary() {
            return boundary;
        }
        final boolean[] b1 = new boolean[]{true, false};
        final boolean[] b2 = new boolean[]{true, true};

        @Override
        public boolean[] valueByParameter(Coordinate parameter, double[] results) {
            throw new UnsupportedOperationException("Not supported yet.");
            //            Coordinate coordinate = new Coordinate();
//            boundary.valueByParameter(parameter, coordinate);
//            getDisplacement(coordinate.x, coordinate.y, results);
//            return b2;
        }

        @Override
        public boolean[] valueByCoordinate(Coordinate coordinate, double[] results) {
            getDisplacement(coordinate.x, coordinate.y, results);
            return b2;
        }
    }

    public class DirichletBoundaryCondition implements BoundaryCondition {

        Boundary boundary;

        public DirichletBoundaryCondition(Boundary boundary) {
            this.boundary = boundary;
        }

        @Override
        public Boundary getBoundary() {
            return boundary;
        }
        final boolean[] b = new boolean[]{false, true};

        @Override
        public boolean[] valueByParameter(Coordinate parameter, double[] results) {
            throw new UnsupportedOperationException("Not supported yet.");
            //            Coordinate coordinate = new Coordinate();
//            boundary.valueByParameter(parameter, coordinate);
//            getStress(coordinate.x, coordinate.y, results);
//            return b;
        }

        @Override
        public boolean[] valueByCoordinate(Coordinate coord, double[] results) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    double width, height;
    double E, v;
    double P;
    double I;

    public TimoshenkoExactBeam(double width, double height, double E, double v, double P) {
        this.width = width;
        this.height = height;
        this.E = E;
        this.v = v;
        this.P = P;
        setHeight(height);
    }

    private void setHeight(double height) {
        I = Math.pow(height, 3) / 12;
        this.height = height;
    }

    public double[] getDisplacement(double x, double y, double[] results) {
        double D = height, L = width;
        double ux = P*y/(6*E*I)*((6*L-3*x)*x+(2+v)*(y*y-D*D/4));
        double uy = -P/(6*E*I)*(3*v*y*y*(L-x)+(4+5*v)*D*D*x/4+(3*L-x)*x*x);
        results[0] = ux;
        results[1] = uy;
        return results;
    }

    public double[] getStress(double x, double y, double results[]) {
        double L = width;
        double D = height;
        double sxx = P*(L-x)*y;
        double sxy = -P/(2*I)*(D*D/4-y*y);
        results[0] = sxx;
        results[1] = sxy;
        return results;
    }
}
