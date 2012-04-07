/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryBasedCritieron;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizer;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class BoundaryBasedCriterions2D {

    /**
     * 可视准则，详见：{@link http://epsilony.net/mywiki/SimpMeshfree/VisibleCriterion}
     */
    public static class Visible implements BoundaryBasedCritieron {

        NodeSupportDomainSizer domainSizer;

        ArrayList<Boundary> boundarysList;
        public static int defaultBoundaryListCapacity = 50;

        public Visible(NodeSupportDomainSizer domainSizer) {
            this.domainSizer = domainSizer;
            boundarysList = new ArrayList<>(defaultBoundaryListCapacity);
        }

        @Override
        public void filterNodes(Collection<Boundary> bnds, Coordinate center, List<Node> nodes, List<Node> results) {
            results.clear();

            if (bnds == null||bnds.isEmpty()) {
                for (Node nd : nodes) {
                    Coordinate coord = nd.coordinate;
                    double x = coord.x;
                    double y = coord.y;
                    double dx = (x - center.x);
                    double dy = (y - center.y);
                    double disSq = dx * dx + dy * dy;
                    if (disSq >= domainSizer.getRadiumSquare(nd)) {
                        continue;
                    }
                    results.add(nd);
                }
                return;
            }

            ArrayList<Boundary> bounds = boundarysList;
            bounds.clear();


            double cx = center.x;
            double cy = center.y;
            boolean centerOnBoundary = false;
            double cFx = 0, cFy = 0, cRx = 0, cRy = 0;
            for (Boundary bn : bnds) {
                Coordinate rear = bn.getPoint(0);
                Coordinate front = bn.getPoint(bn.pointsSize()-1);
                if (front == center) {
                    centerOnBoundary = true;
                    cRx = rear.x;
                    cRy = rear.y;
                    continue;
                }
                if (rear == center) {
                    cFx = front.x;
                    cFy = front.y;
                    continue;
                }
                double rx = rear.x;
                double ry = rear.y;
                double fx = front.x;
                double fy = front.y;

                double t = GeometryMath.crossProduct(rx, ry, fx, fy, cx, cy);
                if (t <= 0) {
                    continue;
                }

                bounds.add(bn);
            }



            for (Node nd : nodes) {
                Coordinate coord = nd.coordinate;
                double x = coord.x;
                double y = coord.y;
                double dx = (x - cx);
                double dy = (y - cy);
                double disSq = dx * dx + dy * dy;
                if (disSq >= domainSizer.getRadiumSquare(nd)) {
                    continue;
                }

                if (coord == center) {
                    results.add(nd);
                    continue;
                }
                
                //与{@link http://epsilony.net/mywiki/SimpMeshfree/VisibleCriterion#2dprep}一样的道理
                //与wiki不同的是，这preprocess 2步放在了算法的结点循环内
                //如center point 在边界上，则center point 为端点的边界严格外侧的点要去除
                if (centerOnBoundary) {
                    double t1 = GeometryMath.crossProduct(cx, cy, cFx, cFy, x, y);
                    if (t1 < 0) {
                        continue;
                    }
                    double t2 = GeometryMath.crossProduct(cRx, cRy, cx, cy, x, y);
                    if (t2 < 0) {
                        continue;
                    }
                }

                boolean add = true;


                //过滤结点
                for (Boundary bn : bounds) {
                    Coordinate rear = bn.getPoint(0);
                    Coordinate front = bn.getPoint(bn.pointsSize()-1);
                    if (coord == rear || coord == front) {
                        continue;
                    }
                    double rx = rear.x;
                    double ry = rear.y;
                    double t1 = GeometryMath.crossProduct(cx, cy, x, y, rx, ry);
                    double fx = front.x;
                    double fy = front.y;
                    double t2 = GeometryMath.crossProduct(cx, cy, x, y, fx, fy);
                    double t3 = GeometryMath.crossProduct(rx, ry, fx, fy, x, y);
                    double t4 = GeometryMath.crossProduct(rx, ry, fx, fy, cy, cy);
                    double t12 = t1 * t2;
                    double t34 = t3 * t4;

                    //似可以再改进
                    if (t12 > 0 || t34 > 0 || t12 >= 0 && t34 == 0) {
                        continue;
                    }
                    add = false;
                    break;
                }
                if (add) {
                    results.add(nd);
                }
            }
        }

        @Override
        public double[] distance(Node node, Coordinate center, double[] result) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOrders(PartDiffOrd[] orders) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDistanceTrans() {
            return false;
        }
    }
}
