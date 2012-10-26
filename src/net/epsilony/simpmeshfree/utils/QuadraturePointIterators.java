/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import net.epsilony.simpmeshfree.model.BoundaryCondition;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import net.epsilony.utils.geom.Quadrangle;
import net.epsilony.utils.geom.Triangle;
import net.epsilony.utils.math.GaussLegendreQuadratureUtils;
import net.epsilony.utils.math.QuadrangleMapper;
import net.epsilony.utils.math.TriangleSymmetricQuadrature;

/**
 *
 * @author epsilon
 */
public class QuadraturePointIterators {

    public static class LineBoundaryIterator implements QuadraturePointIterator {

        Iterator<LineBoundary> lines;
        int quadNum;
        double[] weights;
        double[] coords;
        LineBoundary line;
        double lineLen;
        int cIdx;
        Coordinate linePar = new Coordinate();
        int count = 0;
        private int sumNum;

        private void init(int power, Collection<LineBoundary> lines) {
            this.lines = lines.iterator();
            int numOfPtsPerDim = (int) Math.ceil((power + 1) / 2.0);
            quadNum = numOfPtsPerDim;
            weights = GaussLegendreQuadratureUtils.getWeights(numOfPtsPerDim);
            coords = GaussLegendreQuadratureUtils.getPositions(numOfPtsPerDim);
            cIdx = quadNum;
            sumNum = lines.size() * quadNum;
        }

        public LineBoundaryIterator(int power, Collection<LineBoundary> lines) {
            init(power, lines);
        }

        public LineBoundaryIterator(int power, LineBoundary[] lines) {
            init(power, Arrays.asList(lines));
        }

        protected void setLine(LineBoundary line) {
            this.line = line;
            lineLen = GeometryMath.distance(line.start, line.end);
        }

        @Override
        public boolean next(QuadraturePoint qp) {
            if (cIdx >= quadNum) {
                if (!lines.hasNext()) {
                    return false;
                }
                setLine(lines.next());
                cIdx = 0;
            }
            double u = coords[cIdx];
            double t = (u + 1) / 2.0;
            double weight = weights[cIdx] * lineLen / 2.0;
            linePar.x = t;
            Coordinate c = qp.coordinate;
            Coordinate start = line.start;
            Coordinate end = line.end;
            c.x = start.x * (1 - t) + end.x * t;
            c.y = start.y * (1 - t) + end.y * t;
            c.z = start.z * (1 - t) + end.z * t;
            qp.weight = weight;
            qp.boundary = line;
            qp.id = count;
            cIdx++;
            count++;
            return true;
        }

        protected Coordinate getBoundaryParameter() {
            return linePar;
        }

        @Override
        public int getDispatchedNum() {
            return count;
        }

        @Override
        public int getSumNum() {
            return sumNum;
        }
    }

    public static class LineBoundaryConditionIterator extends LineBoundaryIterator {

        BoundaryCondition bc;
        private boolean isCartesian;

        private void init(BoundaryCondition bc) {
            this.bc = bc;
        }

        public LineBoundaryConditionIterator(int power, Collection<LineBoundary> lines, BoundaryCondition bc) {
            super(power, lines);
            init(bc);
        }

        public LineBoundaryConditionIterator(int power, LineBoundary[] lines, BoundaryCondition bc) {
            super(power, lines);
            init(bc);
        }

        @Override
        public boolean next(QuadraturePoint qp) {
            boolean res = super.next(qp);
            if (!res) {
                return false;
            }
            Coordinate input;
            if (isCartesian) {
                input = qp.coordinate;
            } else {
                input = linePar;
            }
            bc.values(input, qp.values, qp.validities);
            return true;
        }

        @Override
        protected void setLine(LineBoundary line) {
            super.setLine(line);
            isCartesian = bc.setBoundary(line);

        }
    }

    public static class TriangleIterator implements QuadraturePointIterator {

        private Iterator<Triangle> triangles;
        private int power;
        private int numPtsPerTri;
        private Coordinate[] coords;
        private double[] weights;
        private int coordIdx;
        private double triArea;
        private int count = 0;
        private int sumNum;

        private void init(int power, Iterable<Triangle> triangles) {
            this.triangles = triangles.iterator();
            numPtsPerTri = TriangleSymmetricQuadrature.getNumPoints(power);
            coords = new Coordinate[numPtsPerTri];
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new Coordinate();
            }
            weights = TriangleSymmetricQuadrature.getWeights(power);
            coordIdx = coords.length;
            this.power = power;
        }

        public TriangleIterator(int power, Triangle[] triangles) {
            init(power, Arrays.asList(triangles));
        }

        public TriangleIterator(int power, Iterable<Triangle> triangles) {
            init(power, triangles);
        }

        @Override
        public boolean next(QuadraturePoint qp) {
            if (coordIdx >= coords.length) {
                if (!triangles.hasNext()) {
                    return false;
                } else {
                    coordIdx = 0;
                    Triangle tri = triangles.next();
                    TriangleSymmetricQuadrature.getPositions(power, tri, coords);
                    triArea = GeometryMath.triangleArea2D(tri);
                }
            }
            qp.coordinate.set(coords[coordIdx]);
            qp.weight = weights[coordIdx] * triArea;
            qp.id = count;
            coordIdx++;
            count++;
            return true;
        }

        @Override
        public int getDispatchedNum() {
            return count;
        }

        @Override
        public int getSumNum() {
            return sumNum;
        }
    }

    public static class QuadrangleIterator implements QuadraturePointIterator {

        private Iterator<Quadrangle> quadrangles;
        private int count = 0;
        private int sumNum;

        private void init(int power, Collection<Quadrangle> quadrangles) {
            this.quadrangles = quadrangles.iterator();
            int numOfPtsPerDim = (int) Math.ceil((power + 1) / 2.0);
            quadSize = numOfPtsPerDim;
            weights = GaussLegendreQuadratureUtils.getWeights(numOfPtsPerDim);
            uvs = GaussLegendreQuadratureUtils.getPositions(numOfPtsPerDim);
            uIdx = quadSize;
            vIdx = quadSize;
            sumNum = uIdx * vIdx * quadrangles.size();
        }

        public QuadrangleIterator(int power, Collection<Quadrangle> quadrangles) {
            init(power, quadrangles);
        }

        public QuadrangleIterator(int power, Quadrangle[] quadrangles) {
            init(power, Arrays.asList(quadrangles));
        }
        int uIdx;
        int vIdx;
        int quadIndex;
        int quadSize;
        double[] weights;
        double[] uvs;
        QuadrangleMapper quadMapper = new QuadrangleMapper();
        double[] xyJacb = new double[3];
        boolean isOff = false;

        @Override
        public boolean next(QuadraturePoint qp) {
            if (isOff) {
                return false;
            }
            if (uIdx >= quadSize) {
                uIdx = 0;
                vIdx++;
                if (vIdx >= quadSize) {
                    if (!quadrangles.hasNext()) {
                        isOff = true;
                        return false;
                    } else {
                        Quadrangle quad = quadrangles.next();
                        quadMapper.setVertes(quad);
                        vIdx = 0;
                    }
                }
            }

            double u = uvs[uIdx];
            double v = uvs[vIdx];
            quadMapper.getResults(u, v, xyJacb);
            qp.coordinate.x = xyJacb[0];
            qp.coordinate.y = xyJacb[1];
            qp.weight = xyJacb[2] * weights[uIdx] * weights[vIdx];
            qp.id = count;
            count++;
            uIdx++;
            return true;
        }

        @Override
        public int getDispatchedNum() {
            return count;
        }

        @Override
        public int getSumNum() {
            return sumNum;
        }
    }

    public static class CompoundIterator implements QuadraturePointIterator {

        private Iterator<QuadraturePointIterator> iters;
        private QuadraturePointIterator currentIter = null;
        private int count = 0;
        private int sumNum;

        @Override
        public boolean next(QuadraturePoint qp) {
            do {
                if (null == currentIter) {
                    if (iters.hasNext()) {
                        currentIter = iters.next();
                        continue;
                    }
                    return false;
                } else {
                    boolean res = currentIter.next(qp);
                    if (res) {
                        qp.id = count;
                        count++;
                        return true;
                    } else {
                        currentIter = null;
                    }
                }

            } while (true);
        }

        private void init(Collection<QuadraturePointIterator> iters) {
            sumNum = 0;
            for (QuadraturePointIterator iter : iters) {
                sumNum += iter.getSumNum();
            }
            this.iters = iters.iterator();
        }

        public CompoundIterator(Collection<QuadraturePointIterator> iters) {
            init(iters);
        }

        @Override
        public int getDispatchedNum() {
            return count;
        }

        @Override
        public int getSumNum() {
            return sumNum;
        }
    }

    public static class DomainsBased implements QuadraturePointIterator {

        Collection<? extends QuadratureDomain> domains;
        int power;
        private int sum;
        private Iterator<? extends QuadratureDomain> iter;
        int domainSize = -1;
        int domainIndex = -1;
        QuadratureDomain currentDomain = null;
        int dispatched=0;

        private void init(Collection<? extends QuadratureDomain> domains, int power) {
            this.power = power;
            this.domains = domains;
            int sumPts = 0;
            for (QuadratureDomain qd : domains) {
                qd.setPower(power);
                sumPts += qd.size();
            }
            sum = sumPts;
            iter = domains.iterator();
        }

        public DomainsBased(Collection<? extends QuadratureDomain> domains, int power) {
            init(domains,power);
        }

        @Override
        public boolean next(QuadraturePoint qp) {
            do {
                if (domainIndex >= domainSize) {
                    if (iter.hasNext()) {
                        currentDomain = iter.next();
                        domainSize = currentDomain.size();
                        domainIndex = 0;
                        continue;
                    } else {
                        return false;
                    }
                }
                qp.weight=currentDomain.coordinateAndWeight(domainIndex, qp.coordinate);
                domainIndex++;
                dispatched++;
                return true;
            } while (true);
        }

        @Override
        public int getDispatchedNum() {
            return dispatched;
        }

        @Override
        public int getSumNum() {
            return sum;
        }
    }

    public static QuadraturePointIterator compoundIterators(Collection<QuadraturePointIterator> iters) {
        return new CompoundIterator(iters);
    }

    public static QuadraturePointIterator fromLineBoundaries(int power, Collection<LineBoundary> lines) {
        return new LineBoundaryIterator(power, lines);
    }

    public static QuadraturePointIterator fromLineBoundariesAndBC(int power, Collection<LineBoundary> lines, BoundaryCondition bc) {
        return new LineBoundaryConditionIterator(power, lines, bc);
    }

    public static QuadraturePointIterator fromQuadrangles(int power, Collection<Quadrangle> quads) {
        return new QuadrangleIterator(power, quads);
    }

    public static QuadraturePointIterator fromTriangles(int power, Collection<Triangle> tris) {
        return new TriangleIterator(power, tris);
    }

    public static class SynchronizedCountableWrapper implements QuadraturePointIterator {

        private QuadraturePointIterator qpIter;
        ReentrantLock lock = new ReentrantLock();

        public SynchronizedCountableWrapper(QuadraturePointIterator qpIter) {
            this.qpIter = qpIter;
        }

        @Override
        public int getSumNum() {
            return qpIter.getSumNum();
        }

        @Override
        public int getDispatchedNum() {
            lock.lock();
            try {
                return qpIter.getDispatchedNum();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean next(QuadraturePoint qp) {
            lock.lock();
            boolean res;
            try {
                res = qpIter.next(qp);
            } finally {
                lock.unlock();
            }
            return res;
        }
    }
    
    public static QuadraturePointIterator fromDomains(Collection<? extends QuadratureDomain>domains,int power){
        return new DomainsBased(domains,power);
    }

    public static QuadraturePointIterator synchronizedWrapper(QuadraturePointIterator qpIter) {
        return new SynchronizedCountableWrapper(qpIter);
    }
}
