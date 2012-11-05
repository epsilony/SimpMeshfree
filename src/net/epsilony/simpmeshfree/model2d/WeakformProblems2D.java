/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.utils.geom.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.utils.QuadratureDomain;
import net.epsilony.simpmeshfree.utils.QuadratureDomains;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterators;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Quadrangle;
import net.epsilony.utils.geom.Triangle;

/**
 * Same standard mechanical sample problems, including</br> <ul>
 * <li>{@link #timoshenkoCantilevel(double, double, double, double, double, double, double)  Timoshenko's exact cantilevel}
 * </li>
 * <li>{@link #tensionBarHorizontal(double, double, double, double, double, double, double) horizontal tension bar}</li>
 * <li>{@link #tensionBarVertical(double, double, double, double, double, double, double) vertical tension bar}</li>
 * <li>{@link #displacementTensionBar(double, double, double, double, double, double, double) tension bar by displacement (Neumann boundary conditions only)}</li>
 * </ul>
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakformProblems2D {

    public static abstract class LineBoundaryBased implements WeakformProblem {

        protected abstract BoundaryCondition getDirichletBC();

        protected abstract Collection<LineBoundary> getDirichletBnds();

        protected abstract BoundaryCondition getNeumannBC();

        protected abstract Collection<LineBoundary> getNeumannBnds();
        List<Triangle> triQuads;
        List<Quadrangle> quadQuads;
        VolumeCondition volumeCondition;
        int power;

        @Override
        public VolumeCondition volumeCondition() {
            return volumeCondition;
        }

        public LineBoundaryBased(List<Triangle> triQuads, List<Quadrangle> quadQuads, VolumeCondition volumeCondition, int power) {
            this.triQuads = triQuads;
            this.quadQuads = quadQuads;
            this.volumeCondition = volumeCondition;
            this.power = power;
        }

        @Override
        public QuadraturePointIterator volumeIterator() {
            ArrayList<QuadratureDomain> domains = new ArrayList<>();
            if (null != triQuads) {
                domains.addAll(QuadratureDomains.fromTriangles(triQuads));
            }

            if (null != quadQuads) {
                domains.addAll(QuadratureDomains.fromQuadrangles(quadQuads));
            }
            return new QuadraturePointIterators.DomainsBased(domains, power);
        }

        @Override
        public QuadraturePointIterator neumannIterator() {
            Collection<LineBoundary> neumannBnds = getNeumannBnds();
            return new QuadraturePointIterators.LineBoundaryConditionIterator(power, neumannBnds, getNeumannBC());
        }

        @Override
        public QuadraturePointIterator dirichletIterator() {
            Collection<LineBoundary> dirichletBnds = getDirichletBnds();
            return new QuadraturePointIterators.LineBoundaryConditionIterator(power, dirichletBnds, getDirichletBC());
        }

        @Override
        public List<Node> dirichletNodes() {
            HashSet<Node> set = new HashSet<>();
            Collection<LineBoundary> dirichletBnds = getDirichletBnds();
            for (LineBoundary bnd : dirichletBnds) {
                set.add(bnd.start);
                set.add(bnd.end);
            }
            return new ArrayList<>(set);
        }
    }

    public static class TimoshenkoExactBeamProblem extends LineBoundaryBased {

        TimoshenkoExactBeam2D tBeam;
        private final Iterable<LineBoundary> bnds;
        double width, height, E, v, P;
        public double eps = 1e-3;

        public TimoshenkoExactBeamProblem(double width, double height, double E, double v, double P, Iterable<LineBoundary> bnds, List<Triangle> triQuads, List<Quadrangle> quadQuads, int power) {
            super(triQuads, quadQuads, null, power);
            this.tBeam = new TimoshenkoExactBeam2D(width, height, E, v, P);
            this.bnds = bnds;
            this.width = width;
            this.height = height;
            this.E = E;
            this.v = v;
            this.P = P;
        }

        @Override
        protected BoundaryCondition getDirichletBC() {
            return tBeam.getDirichletBC();
        }

        @Override
        protected Collection<LineBoundary> getDirichletBnds() {
            LinkedList<LineBoundary> res = new LinkedList<>();
            for (LineBoundary ln : bnds) {
                double x1 = ln.start.x;
                double x2 = ln.end.x;
                if (Math.abs(x1 - 0) < eps && Math.abs(x2 - 0) < eps) {
                    res.add(ln);
                }
            }
            return res;
        }

        @Override
        protected BoundaryCondition getNeumannBC() {
            return tBeam.getNeumannBC();
        }

        @Override
        protected Collection<LineBoundary> getNeumannBnds() {
            LinkedList<LineBoundary> res = new LinkedList<>();
            for (LineBoundary ln : bnds) {
                double x1 = ln.start.x;
                double x2 = ln.end.x;
                if (Math.abs(x1 - width) < eps && Math.abs(x2 - width) < eps) {
                    res.add(ln);
                }
            }
            return res;
        }
    }

    public static class TensionBar extends LineBoundaryBased {

        Iterable<LineBoundary> bnds;
        public static double eps = 1e-3;
        private double width;
        private double tension;

        public TensionBar(double width, double tension, Iterable<LineBoundary> bnds, List<Triangle> triQuads, List<Quadrangle> quadQuads, VolumeCondition volumeCondition, int power) {
            super(triQuads, quadQuads, volumeCondition, power);
            this.bnds = bnds;
            this.width = width;
            this.tension = tension;
        }

        @Override
        protected BoundaryCondition getDirichletBC() {
            return new BoundaryCondition() {
                @Override
                public boolean setBoundary(Boundary bnd) {
                    return true;
                }

                @Override
                public void values(Coordinate input, double[] results, boolean[] validities) {
                    double x = input.x;
                    if (x == 0) {
                        validities[1] = true;
                        results[1] = 0;
                    }
                    validities[0] = true;
                    results[0] = 0;
                }
            };
        }

        @Override
        protected Collection<LineBoundary> getDirichletBnds() {
            LinkedList<LineBoundary> res = new LinkedList<>();
            for (LineBoundary ln : bnds) {
                double x1 = ln.start.x;
                double x2 = ln.end.x;
                if (Math.abs(x1 - 0) < eps && Math.abs(x2 - 0) < eps) {
                    res.add(ln);
                }
            }
            return res;
        }

        @Override
        protected BoundaryCondition getNeumannBC() {
            return new BoundaryCondition() {
                @Override
                public boolean setBoundary(Boundary bnd) {
                    return true;
                }

                @Override
                public void values(Coordinate input, double[] results, boolean[] validities) {
                    results[0] = tension;
                    results[1] = 0;
                    validities[0] = false;
                    validities[1] = true;
                }
            };
        }

        @Override
        protected Collection<LineBoundary> getNeumannBnds() {
            LinkedList<LineBoundary> res = new LinkedList<>();
            for (LineBoundary ln : bnds) {
                double x1 = ln.start.x;
                double x2 = ln.end.x;
                if (Math.abs(x1 - width) < eps && Math.abs(x2 - width) < eps) {
                    res.add(ln);
                }
            }
            return res;
        }
    }
}