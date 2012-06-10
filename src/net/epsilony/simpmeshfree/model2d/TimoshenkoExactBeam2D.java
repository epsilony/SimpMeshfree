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
            double[] stress=getStress(coordinate.x, coordinate.y,null);
            validities[0]=true;
            validities[1]=true;
            results[0]=stress[0];
            results[1]=stress[2];
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
            getDisplacement(input.x, input.y, results,0);
            validities[0]=true;
            validities[1]=true;
        }

    }
    double width, height;
    double E, nu;
    double P;
    double I;

    public TimoshenkoExactBeam2D(double width, double height, double E, double nu, double P) {
        this.width = width;
        this.height = height;
        this.E = E;
        this.nu = nu;
        this.P = P;
        setHeight(height);
    }

    private void setHeight(double height) {
        I = Math.pow(height, 3) / 12;
        this.height = height;
    }

    public double[] getDisplacement(double x, double y, double[] results,int partDiffOrder) {
        int resDim=0;
        switch(partDiffOrder){
            case 0:
                resDim=2;
                break;
            case 1:
                resDim=6;
                break;
            default:
                throw new IllegalArgumentException("partDiffOrder should be 0 or 1, others are not supported");
        }
        if(null==results){
            results=new double[resDim];
        }else{
            if (results.length<resDim){
                throw new IllegalArgumentException("When partDiffOrder is "+partDiffOrder+", the results.lenght should >= "+resDim+". Try to give a longer results all just give a null reference.");
            }
        }
        double D = height, L = width;
        double u = -P*y/(6*E*I)*((6*L-3*x)*x+(2+nu)*(y*y-D*D/4));
        double v = P/(6*E*I)*(3*nu*y*y*(L-x)+(4+5*nu)*D*D*x/4+(3*L-x)*x*x);
        results[0] = u;
        results[1] = v;
        if(partDiffOrder>0){
            double u_x=-P*y*(L-x)/E/I;
            double u_y=P/E/I*(-(nu + 2)*y*y/2 + (nu + 2)*D*D/24  - L*x +x*x/2);
            double v_x=P/E/I*(-nu*y*y/2+(4+5*nu)*D*D/24+L*x-x*x/2);
            double v_y=P/E/I*nu*y*(L-x);
            results[2]=u_x;
            results[3]=u_y;
            results[4]=v_x;
            results[5]=v_y;
        }
        return results;
    }
    
    public double[] getDisplacement(double x, double y, double[] results){
        return getDisplacement(x, y, results,0);
    }
    
    public double[] getStrain(double x, double y,double results[]){
                if(null==results){
            results=new double[3];
        }
        double L = width;
        double D = height;
        double xx=-P*y*(L-x)/E/I;
        double yy=P/E/I*nu*y*(L-x);
        double xy=P/E/I*(1+nu)*(D*D/4-y*y);
        results[0] = xx;
        results[1] = yy;
        results[2] = xy;
        return results;
    }

    public double[] getStress(double x, double y, double results[]) {
        if(null==results){
            results=new double[3];
        }
        double L = width;
        double D = height;
        double sxx = -P*(L-x)*y/I;
        double syy=0;
        double sxy = P/(2*I)*(D*D/4.0-y*y);
        results[0] = sxx;
        results[1] = syy;
        results[2] = sxy;
        return results;
    }
    
    public BoundaryCondition getNeumannBC(){
        return new NeumannBoundaryCondition();
    }
    
    public BoundaryCondition getDirichletBC(){
        return new DirichletBoundaryCondition();
    }
}
