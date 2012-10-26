/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.ArrayList;
import java.util.Collection;
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
public class QuadratureDomains {

    public static class Tri implements QuadratureDomain {

        Triangle tri;
        int power = 0;
        double triArea;

        public Tri(Triangle tri) {
            this.tri = tri;
            triArea = GeometryMath.triangleArea2D(tri);
        }

        @Override
        public void setPower(int power) {
            this.power = power;
        }

        @Override
        public int size() {
            if (power <= 0) {
                throw new IllegalStateException("Please invoke setPower(int) first");
            }
            int size = TriangleSymmetricQuadrature.getNumPoints(power);
            return size;
        }

        @Override
        public double coordinateAndWeight(int index, Coordinate output) {
            if (null == output) {
                throw new NullPointerException();
            }
            TriangleSymmetricQuadrature.getPosition(power, index, tri, output);
            return TriangleSymmetricQuadrature.getWeight(power, index)*triArea;
        }
    }

    public static class Rect implements QuadratureDomain {

        int power = 0;
        int xLow, yLow, xUp, yUp;
        private int numPtPerDim = -1;

        public Rect(int xLow, int xUp, int yLow, int yUp) {
            this.xLow = xLow;
            this.yLow = yLow;
            this.xUp = xUp;
            this.yUp = yUp;
        }

        @Override
        public void setPower(int power) {
            this.power = power;
            numPtPerDim = GaussLegendreQuadratureUtils.getNumPoints(power);
        }

        @Override
        public int size() {
            return numPtPerDim * numPtPerDim;
        }

        @Override
        public double coordinateAndWeight(int index, Coordinate output) {
            if (null == output) {
                throw new NullPointerException();
            }
            int xIdx = numPtPerDim / index;
            int yIdx = numPtPerDim % index;
            output.x = GaussLegendreQuadratureUtils.getPosition(numPtPerDim, xIdx, xLow, xUp);
            output.y = GaussLegendreQuadratureUtils.getPosition(numPtPerDim, yIdx, yLow, yUp);
            double xWeight = GaussLegendreQuadratureUtils.getWeight(numPtPerDim, xIdx, xLow, xUp);
            double yWeight = GaussLegendreQuadratureUtils.getWeight(numPtPerDim, yIdx, yLow, yUp);
            return xWeight * yWeight;
        }
    }

    public static class Quad implements QuadratureDomain {

        private int numPtPerDim;
        Quadrangle quad;

        public Quadrangle getQuadrangle() {
            return quad;
        }

        public Quad(Quadrangle quad) {
            this.quad = quad;
        }

        @Override
        public void setPower(int power) {
            numPtPerDim = GaussLegendreQuadratureUtils.getNumPoints(power);
        }

        @Override
        public int size() {
            return numPtPerDim * numPtPerDim;
        }

        @Override
        public double coordinateAndWeight(int index, Coordinate output) {
            if (output == null) {
                throw new NullPointerException();
            }
            int indexU = index / numPtPerDim;
            int indexV = index % numPtPerDim;
            double iu = GaussLegendreQuadratureUtils.getPosition(numPtPerDim, indexU);
            double iv = GaussLegendreQuadratureUtils.getPosition(numPtPerDim, indexV);
            double jacobi = QuadrangleMapper.iuv2xyC(quad, iu, iv, output);
            double xWeight = GaussLegendreQuadratureUtils.getWeight(numPtPerDim, indexU);
            double yWeight = GaussLegendreQuadratureUtils.getWeight(numPtPerDim, indexV);
            return xWeight * yWeight * jacobi;
        }
    }

    public static ArrayList<QuadratureDomain> fromTriangles(Collection<Triangle> triangles) {
        ArrayList<QuadratureDomain> result = new ArrayList<>(triangles.size());
        for (Triangle tri : triangles) {
            result.add(new Tri(tri));
        }
        return result;
    }

    public static ArrayList<QuadratureDomain> fromQuadrangles(Collection<Quadrangle> quads) {
        ArrayList<QuadratureDomain> result = new ArrayList<>(quads.size());
        for (Quadrangle quad : quads) {
            result.add(new Quad(quad));
        }
        return result;
    }
}
