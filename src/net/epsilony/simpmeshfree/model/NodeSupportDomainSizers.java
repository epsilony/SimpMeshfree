/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.Collection;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class NodeSupportDomainSizers {

    public static ArraySizer arraySizerByRadiums(double[] radiums) {
        double maxRadium = 0;
        double[] radiumsSquare = new double[radiums.length];
        for (int i = 0; i < radiums.length; i++) {
            double t = radiums[i];
            radiumsSquare[i] = t * t;
            if (maxRadium < t) {
                maxRadium = t;
            }
        }
        return new ArraySizer(radiumsSquare, radiums, maxRadium);
    }

    public static class ArraySizer implements NodeSupportDomainSizer {

        public static ArraySizer arraySizerByRadiumSquares(double[] radiumSquares) {
            double maxRadium = 0;
            double[] radiums = new double[radiumSquares.length];
            for (int i = 0; i < radiumSquares.length; i++) {
                double t = Math.sqrt(radiumSquares[i]);
                radiumSquares[i] = t;
                if (maxRadium < t) {
                    maxRadium = t;
                }
            }
            return new ArraySizer(radiumSquares, radiums, maxRadium);
        }
        public double[] radiumSquares;
        public double[] radiums;
        double maxRadium;

        public ArraySizer(double[] radiumSquares, double[] radiums, double maxRadium) {
            this.radiumSquares = radiumSquares;
            this.radiums = radiums;
            this.maxRadium = maxRadium;
        }

        @Override
        public double getRadiumSquare(Node node) {
            return radiumSquares[node.id];
        }

        @Override
        public double getRadium(Node node) {
            return radiums[node.id];
        }

        @Override
        public double getMaxRadium() {
            return maxRadium;
        }
    }

    public static class ConstantSizer implements NodeSupportDomainSizer {

        final double radium, radiumSquare;

        public ConstantSizer(double radiums) {
            this.radium = radiums;
            this.radiumSquare = radiums * radiums;
        }

        @Override
        public double getRadium(Node node) {
            return radium;
        }

        @Override
        public double getRadiumSquare(Node node) {
            return radiumSquare;
        }

        @Override
        public double getMaxRadium() {
            return radium;
        }
    }

    public static double maxRadium(Collection<Node> nodes, NodeSupportDomainSizer sizer) {
        double max = 0;
        for (Node nd : nodes) {
            double t = sizer.getRadium(nd);
            if (max < t) {
                max = t;
            }
        }
        return max;
    }
}
