/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.boundaryCondition;

import net.epsilony.simpmeshfree.model.geometry.BoundaryNode.BoundaryConditionType;

/**
 *
 * @author epsilon
 */
public class BoundaryConditions {
    public static BoundaryCondition getTimoshenkoBeamEssentialBC(final double L,final double D,final double P,final double E,final double rou){
        return new BoundaryCondition() {

            double I=D*D*D/12;
            @Override
            public BoundaryConditionType type() {
                return BoundaryConditionType.Dirichlet;
            }

            @Override
            public double[] boundaryValues(double x, double y,double[] result) {
                result[0]=-P*(2+rou)/6/E/I*(y*y-D*D/4);
                result[1]=P*rou*L/2/E/I*y*y;
                return result;
            }

            @Override
            public double[] boundaryValues(double parm,double[] result) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static BoundaryCondition getTimoshenkoBeanNaturalBC(final double L,final double D,final double P,final double E,final double rou){
        return new BoundaryCondition() {

            double I=D*D*D/12;
            @Override
            public BoundaryConditionType type() {
                return BoundaryConditionType.Neumann;
            }

            @Override
            public double[] boundaryValues(double x, double y, double[] result) {
                result[0]=0;
                result[1]=P/2/I*(D*D/4-y*y);
                return result;
            }

            @Override
            public double[] boundaryValues(double parm, double[] result) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

}
