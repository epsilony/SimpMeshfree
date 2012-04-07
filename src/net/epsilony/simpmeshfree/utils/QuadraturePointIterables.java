/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import net.epsilony.utils.geom.Quadrangle;
import net.epsilony.utils.geom.Triangle;
import net.epsilony.utils.math.GaussLegendreQuadratureUtils;
import net.epsilony.utils.math.QuadrangleMapper;
import net.epsilony.utils.math.TriangleSymmetricQuadrature;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class QuadraturePointIterables {

    private QuadraturePointIterables() {
    }

    /**
     * 对称三角区域积分的迭代器，{@link #iterator()}返回新实例，每个{@link #iterator()}实例的每次迭代{@link Iterator#next() }返回修改过的同一个{@link QuadraturePoint}实例
     */
    public static class TriangleArrayIterable implements Iterable<QuadraturePoint> {

        int power;
        Triangle[] triangles;

        /**
         * 
         * @param power 可精确积出的多项式的最高阶数
         * @param triangles 三角形数组
         * @param copy 是否先复制triangle数组
         */
        public TriangleArrayIterable(int power, Triangle[] triangles, boolean copy) {
            if (copy) {
                this.triangles = Arrays.copyOf(triangles, triangles.length);
            } else {
                this.triangles = triangles;
            }
            this.power = power;
        }

        /**
         * as default the triangles will not be copied
         * @param power the max order of polynomials that can be acurately quadrated
         * @param triangles 
         */
        public TriangleArrayIterable(int power, Triangle[] triangles) {
            this.power = power;
            this.triangles = triangles;
        }

        /**
         * 
         * @return a new Instance
         */
        @Override
        public Iterator<QuadraturePoint> iterator() {
            return new TriangleArrayIterator(power, triangles, false);
        }
    }

    /**
     * Iterator that iterates out a same QuadraturePoint instance
     */
    public static class TriangleArrayIterator implements Iterator<QuadraturePoint> {

        /**
         * 
         * @param power 可精确积出的多项式的最高阶数
         * @param triangles 三角形数组
         * @param copy 是否先复制triangle数组
         */
        public TriangleArrayIterator(int power, Triangle[] triangles, boolean copy) {
            this.power = power;

            if (copy) {
                this.triangles = Arrays.copyOf(triangles, triangles.length);
            } else {
                this.triangles = triangles;
            }
            if (0 != triangles.length) {
                int size = TriangleSymmetricQuadrature.getNumPoints(power);
                inTriangleSize = size;
                pointPositions = new double[size * 2];
                weights = TriangleSymmetricQuadrature.getWeights(power);
                Triangle tri = triangles[0];
                double x1 = tri.x1, y1 = tri.y1, x2 = tri.x2, y2 = tri.y2, x3 = tri.x3, y3 = tri.y3;
                TriangleSymmetricQuadrature.getPositions(x1, y1, x2, y2, x3, y3, power, pointPositions);
                area = GeometryMath.triangleArea(x1, y1, x2, y2, x3, y3);
            }
        }
        double area;
        int inTriangleIndex;
        int power;
        int inTriangleSize;
        int triangleIndex;
        Triangle[] triangles;
        QuadraturePoint quadraturePoint = new QuadraturePoint();
        double[] pointPositions;
        double[] weights;

        @Override
        public boolean hasNext() {
            if (inTriangleIndex < inTriangleSize) {
                return true;
            } else if (triangleIndex < triangles.length - 1) {
                return true;
            }
            return false;
        }

        /**
         * 
         * @return in the whole life time of this Object, actually returns a same QuadraturePoint with dynamic values
         */
        @Override
        public QuadraturePoint next() {
            if (inTriangleIndex >= inTriangleSize) {
                inTriangleIndex = 0;
                triangleIndex++;
                Triangle triangle = triangles[triangleIndex];
                double x1 = triangle.x1,
                        y1 = triangle.y1,
                        x2 = triangle.x2,
                        y2 = triangle.y2,
                        x3 = triangle.x3,
                        y3 = triangle.y3;
                area = GeometryMath.triangleArea(x1, y1, x2, y2, x3, y3);
                TriangleSymmetricQuadrature.getPositions(x1, y1, x2, y2, x3, y3, power, pointPositions);
                weights = TriangleSymmetricQuadrature.getWeights(power, false);
            }
            QuadraturePoint qp = quadraturePoint;
            Coordinate point = qp.coordinate;
            qp.weight = area * weights[inTriangleIndex];
            int index = 2 * inTriangleIndex;
            point.x = pointPositions[index++];
            point.y = pointPositions[index];
            inTriangleIndex++;
            return qp;
        }

        /**
         * not supported
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

     /**
     * 四边形区域积分的迭代器，{@link #iterator()}返回新实例，每个{@link #iterator()}实例的每次迭代{@link Iterator#next() }返回修改过的同一个{@link QuadraturePoint}实例</br>
     * 四边形迭代的算法大体上是将[-1,1]*[-1,1]的标准积分空间映射到{@link Quadrangle}并进行积分，
     * 其实际由：{@link QuadrangleMapper} 实现</br>
     * 具体和映身方法见{@link http://epsilony.net/mywiki/Math/GaussQuadrature}
     * @see QuadrangleMapper
     */
    public static class QuadrangleArrayIterable implements Iterable<QuadraturePoint> {

        int power;
        Quadrangle[] quadrangles;

        /**
         * 
         * @param power 可精确积出的多项式的最高阶数
         * @param quadrangles 四边形数组 使用前不先复制
         */
        public QuadrangleArrayIterable(int power, Quadrangle[] quadrangles) {
            this.power = power;
            this.quadrangles = quadrangles;
        }

        /**
         * 返回一个新实例
         * @return 一个新实例
         */
        @Override
        public Iterator<QuadraturePoint> iterator() {
            return new QuadrangleArrayIterator(power, quadrangles);
        }
    }

    /**
     * Iterator that iterates out a same QuadraturePoint instance
     */
    public static class QuadrangleArrayIterator implements Iterator<QuadraturePoint> {

        /**
         * 
         * @param power 可精确积出的多项式的最高阶数
         * @param quadrangles 四边形数组 使用前不先复制
         */
        public QuadrangleArrayIterator(int power, Quadrangle[] quadrangles) {
            this.quadrangles = quadrangles;

            if (quadrangles.length > 0) {
                int numOfPoints = (int) Math.ceil((power + 1) / 2.0);
                quadSize = numOfPoints;
                weights = GaussLegendreQuadratureUtils.getWeights(numOfPoints);
                uvs = GaussLegendreQuadratureUtils.getPositions(numOfPoints);
                Quadrangle quad = quadrangles[0];
                quadMapper.setVertices(quad.x1, quad.y1, quad.x2, quad.y2, quad.x3, quad.y3, quad.x4, quad.y4);
            }
        }
        int qi;
        int qj;
        int quadIndex;
        int quadSize;
        double[] weights;
        double[] uvs;
        Quadrangle[] quadrangles;
        QuadrangleMapper quadMapper = new QuadrangleMapper();
        double[] xyJacb = new double[3];
        QuadraturePoint quadraturePoint = new QuadraturePoint();

        @Override
        public boolean hasNext() {
            if (qi >= quadSize && qj >= quadSize - 1 && quadIndex >= quadrangles.length - 1) {
                return false;
            }
            return true;
        }

        /**
         * 
         * @return in the whole life time of this Object, actually returns a same QuadraturePoint with dynamic values
         */
        @Override
        public QuadraturePoint next() {
            if (qi >= quadSize) {
                qi = 0;
                qj++;
                if (qj >= quadSize) {
                    qj = 0;
                    quadIndex++;
                    Quadrangle quad = quadrangles[quadIndex];
                    quadMapper.setVertices(quad.x1, quad.y1, quad.x2, quad.y2, quad.x3, quad.y3, quad.x4, quad.y4);
                }
            }
            double u = uvs[qi];
            double v = uvs[qj];
            quadMapper.getResults(u, v, xyJacb);
            quadraturePoint.coordinate.x = xyJacb[0];
            quadraturePoint.coordinate.y = xyJacb[1];
            quadraturePoint.weight = xyJacb[2] * weights[qi] * weights[qj];
            qi++;
            return quadraturePoint;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * 将多个迭代器合包装为一个</br>
     * 注，与{@link QuadraturePointIterables.TriangleArrayIterator}等不同，这里的Iterator.next()返回的元素可能不是同一个。
     */
    public static class IterablesWrapper implements Iterable<QuadraturePoint> {

        Iterable<QuadraturePoint>[] iterables;
        Iterator<QuadraturePoint> iterator;
        int index;

        public IterablesWrapper(Iterable<QuadraturePoint>[] iterables) {
            this.iterables = iterables;
            if (iterables.length > 0) {
                iterator = iterables[0].iterator();
            }
        }

        public IterablesWrapper(Collection<Iterable<QuadraturePoint>> iterables) {
            ArrayList<Iterable<QuadraturePoint>> alist = new ArrayList<>(iterables);
            this.iterables = alist.toArray(new Iterable<>[0]);
            if (iterables.size() > 0) {
                iterator = this.iterables[0].iterator();
            }
        }

        @Override
        public Iterator<QuadraturePoint> iterator() {
            return new Iterator<QuadraturePoint>() {

                @Override
                public boolean hasNext() {
                    if (null == iterator) {
                        return false;
                    }
                    while (iterator.hasNext() == false && index < iterables.length - 1) {
                        index++;
                        iterator = iterables[index].iterator();
                    }
                    return iterator.hasNext();
                }

                @Override
                public QuadraturePoint next() {
                    return iterator.next();
                }

                /**
                 * Unsupported
                 */
                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
    }
}
