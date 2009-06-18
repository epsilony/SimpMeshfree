/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.List;

/**
 *
 * @author epsilon
 */
public class BoundaryConditions {

    public static BoundaryCondition getStretchNatural(final double tx) {
        return new BoundaryCondition() {

            @Override
            public BoundaryConditionType getType() {
                return BoundaryConditionType.Natural;
            }

            @Override
            public byte getValues(double t, double[] output) {

                output[0] = tx;
                output[1] = 0;
                return BoundaryCondition.XY;
            }

            @Override
            public List<double[]> getConNaturalValues(List<double[]> output) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static BoundaryCondition getStretchEssential(final double ux) {
        return new BoundaryCondition() {

            @Override
            public BoundaryConditionType getType() {
                return BoundaryConditionType.Essential;
            }

            @Override
            public byte getValues(double t, double[] output) {
                output[0] = ux;
                output[1] = 0;
                if (t == 0.5&&ux==0) {
                    return BoundaryCondition.XY;
                } else {
                    return BoundaryCondition.X;
                }
            }

            @Override
            public List<double[]> getConNaturalValues(List<double[]> output) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static BoundaryCondition getTimoshenkoNatural(final double E, final double v, final double P, final double L, final double D, final LineSegment line) {
        return new BoundaryCondition() {

            double I = D * D * D / 12;

            @Override
            public BoundaryConditionType getType() {
                return BoundaryConditionType.Natural;
            }
            double[] tds = new double[2];

            @Override
            public byte getValues(double t, double[] output) {
                line.parameterPoint(t, tds);
                double y = tds[1];
                output[0] = 0;
                output[1] = P / (2 * I) * (D * D / 4 - y * y);
                return BoundaryCondition.Y;

            }

            @Override
            public List<double[]> getConNaturalValues(List<double[]> output) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static BoundaryCondition getTimoshenkoEssential(final double E, final double v, final double P, final double L, final double D, final LineSegment line) {
        return new BoundaryCondition() {

            double I = D * D * D / 12;

            @Override
            public BoundaryConditionType getType() {
                return BoundaryConditionType.Essential;
            }
            double[] tds = new double[2];

            @Override
            public byte getValues(double t, double[] output) {
                line.parameterPoint(t, tds);
                double y = tds[1];
                output[0] = -P * y / (6 * E * I) * (2 + v) * (y * y - D * D / 4);
                output[1] = P * v * L / (2 * E * I) * y * y;
                return BoundaryCondition.XY;

            }

            @Override
            public List<double[]> getConNaturalValues(List<double[]> output) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static BoundaryCondition getConstantEssentialBoundaryConditions(final double ux, final boolean bx, final double uy, final boolean by) {
        return new BoundaryCondition() {

            @Override
            public BoundaryConditionType getType() {
                return BoundaryConditionType.Essential;
            }

            @Override
            public byte getValues(double t, double[] output) {
                output[0] = ux;
                output[1] = uy;
                byte result = 0;
                if (bx) {
                    result |= X;
                }
                if (by) {
                    result |= Y;
                }
                return result;
            }

            @Override
            public List<double[]> getConNaturalValues(List<double[]> output) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static void main(String[] args) {
        System.out.println((BoundaryCondition.XY & BoundaryCondition.X) == BoundaryCondition.X);
    }
}
