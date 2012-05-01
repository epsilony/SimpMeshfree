/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.ArrayUtils;
import net.epsilony.utils.geom.Coordinate;
import static net.epsilony.utils.geom.GeometryMath.*;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author epsilon
 */
public class BoundaryUtils {

    /**
     * Determines whether
     * <code>bnd</code> and a sphere has common points.
     *
     * @param bnd
     * @param center
     * @param radius
     * @return
     */
    public static boolean isBoundaryIntersect(Boundary bnd, Coordinate center, double radius) {
        Class bndClass = bnd.getClass();
        if (bndClass == LineBoundary.class) {
            return isBoundaryIntersect((LineBoundary) bnd, center, radius);
        } else if (bndClass == TriangleBoundary.class) {
            return isBoundaryIntersect((TriangleBoundary) bnd, center, radius);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Determines whether
     * <code>line</code> and a sphere has common points.
     *
     * @param line
     * @param center
     * @param radius
     * @return
     */
    public static boolean isBoundaryIntersect(LineBoundary line, Coordinate center, double radius) {
        double distanceSq = radius * radius;
        double startDisSq = distanceSquare(center, line.start);
        if (startDisSq <= distanceSq) {
            return true;
        }
        double endDisSq = distanceSquare(center, line.end);
        if (endDisSq <= distanceSq) {
            return true;
        }
        double lineLenSq = distanceSquare(line.start, line.end);
        if (startDisSq - distanceSq > lineLenSq || endDisSq - distanceSq > lineLenSq) {
            return false;
        }


        Coordinate nearPt, farPt;
        if (startDisSq < distanceSq) {
            nearPt = line.start;
            farPt = line.end;
        } else {
            nearPt = line.end;
            farPt = line.end;
        }
        Coordinate vec1 = minus(center, nearPt);
        Coordinate vec2 = minus(farPt, nearPt);
        Coordinate normal = cross(cross(vec1, vec2), vec2, vec2);
        double norm = Math.abs(dot(normal, vec1));
        if (norm > radius) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determines whether
     * <code>tri</code> and a sphere has common points.
     *
     * @param tri
     * @param center
     * @param radius
     * @return
     */
    public static boolean isBoundaryIntersect(TriangleBoundary tri, Coordinate center, double radius) {
        Coordinate normal = outNormal(tri, null);
        double[] dists = new double[3];
        Node[] nodes = tri.nodes;
        for (int i = 0; i < 3; i++) {
            dists[i] = distanceSquare(center, nodes[i]);
        }

        int minDistId = ArrayUtils.minItemId(dists);
        double distanceSq = radius * radius;
        double minDistSq = dists[minDistId];
        if (minDistSq <= distanceSq) {
            return true;
        }
        Node minDistNode = nodes[minDistId];
        double norm = dot(normal, minus(minDistNode, center));
        Coordinate footOfProjection = linearCombine(center, 1, normal, norm);
        Coordinate vec = new Coordinate();
        Coordinate vec2 = new Coordinate();
        for (int i = 0; i < 3; i++) {
            minus(footOfProjection, nodes[i], vec);
            minus(nodes[(i + 1) % 3], nodes[i], vec2);
            cross(vec, vec2, vec);
            double dot = dot(vec, normal);
            if (dot < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the unit out normal vector of
     * <code>tri</code> by righthanded rule.
     *
     * @param tri
     * @param result
     * @return
     */
    public static Coordinate outNormal(TriangleBoundary tri, Coordinate result) {
        if (null == result) {
            result = new Coordinate();
        }
        Node[] nodes = tri.nodes;
        double[] distSqs = new double[]{distanceSquare(nodes[0], nodes[1]),
            distanceSquare(nodes[1], nodes[2]),
            distanceSquare(nodes[2], nodes[0])};
        int maxId = ArrayUtils.maxItemId(distSqs);
        int startId = (maxId - 1) % 3;
        int endId2 = (maxId + 1) % 3;
        int endId1 = maxId;
        double norm = Math.sqrt(distSqs[startId] * distSqs[endId2]);
        Coordinate start, end1, end2;
        start = nodes[startId];
        end1 = nodes[endId1];
        end2 = nodes[endId2];
        cross(end1.x - start.x, end1.y - start.y, end1.z - start.z, end2.x - start.x, end2.y - start.y, end2.z - start.z, result);
        result.scale(1 / norm);
        return result;
    }

    /**
     * Calculates the unit out normal vector of
     * <code>line</code> by righthanded rule, Which is along
     * <code>line</code> x
     * <code>publicNormal</code>
     *
     * @param line
     * @param publicNormal if null the publicNormal seams as z axis
     * @param result
     * @return
     */
    public static Coordinate outNormal(LineBoundary line, Coordinate publicNormal, Coordinate result) {
        if (null == publicNormal) {
            result.x = line.end.y - line.start.y;
            result.y = -line.end.x + line.start.x;
            result.z = 0;
        } else {
            Coordinate c = minus(line.end, line.start);
            cross(publicNormal, c, result);
        }
        normalize(result);
        return result;
    }

    public static Node nearestBoundaryNode(Coordinate pt, Boundary bnd) {
        Node minDistNode = bnd.getNode(0);
        double minDistSq = distanceSquare(pt, minDistNode);
        for (int i = 1; i < bnd.num(); i++) {
            Node bndNode = bnd.getNode(i);
            double distSq = distanceSquare(pt, bndNode);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                minDistNode = bndNode;
            }
        }
        return minDistNode;
    }

    /**
     * Determines whether
     * <code>pt-center</code> is in the convex cone of
     * <code>{line.getNode(i)- center:i = 0 or 1}</code>
     *
     * @param line
     * @param center
     * @param pt
     * @return
     */
    public static boolean isInConvexCone(LineBoundary line, Coordinate center, Coordinate pt) {
        double cross1 = cross2D(pt.x - center.x, pt.y - center.y, line.start.x - center.x, line.start.y - center.y);
        double cross2 = cross2D(line.end.x - center.x, line.end.y - center.y, pt.x - center.x, pt.y - center.y);
        if (cross1 >= 0 && cross2 >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines whether
     * <code>pt-center</code> is in the convex cone of
     * <code>{tri.getNode(i)- center:i = 0, 1 or 2}</code>
     *
     * @param tri
     * @param center
     * @param pt
     * @return
     */
    public static boolean isInConvexCone(TriangleBoundary tri, Coordinate center, Coordinate nd, DenseMatrix tMat, DenseVector tVec1, DenseVector tVec2) {
        if (null == tMat) {
            tMat = new DenseMatrix(3, 3);
        }
        if (null == tVec1) {
            tVec1 = new DenseVector(3);
        }
        if (null == tVec2) {
            tVec2 = new DenseVector(3);
        }

        for (int i = 0; i < 3; i++) {
            tMat.set(i, 0, tri.nodes[i].x);
            tMat.set(i, 1, tri.nodes[i].y);
            tMat.set(i, 2, tri.nodes[i].z);
        }
        tVec1.set(0, nd.x - center.x);
        tVec1.set(1, nd.y - center.y);
        tVec1.set(2, nd.z - center.z);
        tMat.solve(tVec1, tVec2);
        for (int i = 0; i < 3; i++) {
            if (tVec2.get(i) < 0) {
                return false;
            }
        }
        return true;
    }
}
