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
public class TimoshenkoExactBeam2D {

    
    public class NeumannBoundaryCondition implements BoundaryCondition {

        @Override
        public void values(Coordinate coordinate, double[] results,boolean[] validities) {
            getStress(coordinate.x, coordinate.y, results);
            validities[0]=true;
            validities[1]=true;

        }

        @Override
        public boolean setBoundary(Boundary bnd) {
            return true;
        }
    }

    public class DirichletBoundaryCondition implements BoundaryCondition {

        @Override
        public boolean setBoundary(Boundary bnd) {
            return true;
        }

        @Override
        public void values(Coordinate input, double[] results, boolean[] validities) {
            getDisplacement(input.x, input.y, results);
            validities[0]=true;
            validities[1]=true;
        }

    }
    double width, height;
    double E, v;
    double P;
    double I;

    public TimoshenkoExactBeam2D(double width, double height, double E, double v, double P) {
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
    
    public BoundaryCondition getNeumannBC(){
        return new NeumannBoundaryCondition();
    }
    
    public BoundaryCondition getDirichletBC(){
        return new DirichletBoundaryCondition();
    }
}
