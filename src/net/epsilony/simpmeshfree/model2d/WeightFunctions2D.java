/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.BoundaryBasedCritieron;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.WeightFunction;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class WeightFunctions2D {

    public static abstract class Abstract implements WeightFunction {

        public Abstract() {
            this.critieron = null;
        }
        private final BoundaryBasedCritieron critieron;

        protected Abstract(BoundaryBasedCritieron critieron) {
            this.critieron = critieron;
        }
        private int opt, oriIndex, partialXIndex, partialYIndex;

        @Override
        public void setOrders(PartDiffOrd[] types) {
            opt = 0;
            for (int i = 0; i < types.length; i++) {
                PartDiffOrd type = types[i];
                switch (type.sumOrder()) {
                    case 0:
                        oriIndex = i;
                        opt += 1;
                        break;
                    case 1:
                        switch (type.respectDimension(0)) {
                            case 0:
                                opt += 2;
                                partialXIndex = i;
                                break;
                            case 1:
                                opt += 4;
                                partialYIndex = i;
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
            if (null != critieron && critieron.isDistanceTrans()) {
                critieron.setOrders(types);
            }
        }
        double[] distanceResults = new double[3];

        private double[] valuesWithCriterion(Node node, Coordinate point, double supportRad, double[] results) {
            double nx = node.coordinate.x;
            double ny = node.coordinate.y;
            double px = point.x;
            double py = point.y;
            double dltX = px - nx;
            double dltY = py - ny;
            double[] dists = distanceResults;
            critieron.distance(node, point, dists);
            double dis = dists[oriIndex];
            double r = dis / supportRad;
            if (r > 1) {
                switch (opt) {
                    case 1:
                        results[oriIndex] = 0;
                        break;
                    case 7:
                        results[oriIndex] = 0;
                        results[partialXIndex] = 0;
                        results[partialYIndex] = 0;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                return results;
            }
            double t;
            switch (opt) {
                case 1:
                    results[oriIndex] = value(r, 0);
                    break;
                case 7:
                    results[oriIndex] = value(r, 0);
                    t = value(r, 1);
                    results[partialXIndex] = t * dists[partialXIndex];
                    results[partialYIndex] = t * dists[partialYIndex];
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return results;
        }

        private double[] valuesWithoutCriterion(Node node, Coordinate point, double supportRad, double[] results) {
            double nx = node.coordinate.x;
            double ny = node.coordinate.y;
            double px = point.x;
            double py = point.y;
            double dltX = px - nx;
            double dltY = py - ny;
            double dis = Math.sqrt(dltX * dltX + dltY * dltY);
            double r = dis / supportRad;
            if (r > 1) {
                switch (opt) {
                    case 1:
                        results[oriIndex] = 0;
                        break;
                    case 7:
                        results[oriIndex] = 0;
                        results[partialXIndex] = 0;
                        results[partialYIndex] = 0;
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                return results;
            }
            double t;
            switch (opt) {
                case 1:
                    results[oriIndex] = value(r, 0);
                    break;
                case 7:
                    results[oriIndex] = value(r, 0);
                    t = value(r, 1);
                    results[partialXIndex] = t * dltX / supportRad / dis;
                    results[partialYIndex] = t * dltY / supportRad / dis;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return results;
        }

        @Override
        public double[] values(Node node, Coordinate point, double supportRad, double[] results) {
            if (null == critieron || critieron.isDistanceTrans() == false) {
                return valuesWithoutCriterion(node, point, supportRad, results);
            } else {
                return valuesWithCriterion(node, point, supportRad, results);
            }
        }

        abstract double value(double r, int partialOrder);
    }

    public static class TriSpline extends Abstract {

        public TriSpline(BoundaryBasedCritieron criterion) {
            super(criterion);
        }

        public TriSpline() {
        }

        @Override
        double value(double r, int partialOrder) {
            if (r > 1) {
                return 0;
            }
            switch (partialOrder) {
                case 0:
                    if (r <= 0.5) {
                        return 2 / 3.0 + 4 * r * r * (-1 + r);
                        //return 2/3.0-4*r*r+4*r*r*r;
                    } else {
                        double d = 1 - r;
                        return 4 / 3.0 * d * d * d;
                        //return 4/3.0-4*r+4*r*r-4/3.0*r*r*r;
                    }
                case 1:
                    if (r <= 0.5) {
                        return 4 * r * (-2 + 3 * r);
                        //return -8*r+12*r*r;
                    } else {
                        double d = 1 - r;
                        return -4 * d * d;
                    }
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    public static class SimpPower extends Abstract {

        int power;

        public SimpPower(int power) {
            super();
            this.power = power;
            if (power < 2) {
                throw new IllegalArgumentException();
            }
        }

        public SimpPower(int power, BoundaryBasedCritieron critieron) {
            super(critieron);
            this.power = power;
        }

        @Override
        double value(double r, int partialOrder) {
            if (r > 1) {
                return 0;
            }
            double t = r * r - 1;
            switch (partialOrder) {
                case 0:
                    return Math.pow(t, power);
                case 1:
                    return Math.pow(t, power - 1) * power * 2 * r;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}
