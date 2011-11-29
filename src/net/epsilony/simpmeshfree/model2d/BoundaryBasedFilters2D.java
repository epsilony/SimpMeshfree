/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.epsilony.geom.Coordinate;
import net.epsilony.geom.GeometryMath;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.BoundaryBasedFilter;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizer;
import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizers;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class BoundaryBasedFilters2D {

    public static class Visible implements BoundaryBasedFilter {

        NodeSupportDomainSizer domainSizer;
//        Comparator<Node> angleComp = new AngleComparator();
        ArrayList<Boundary> boundarysList;
        public static int defaultBoundaryListCapacity = 50;

        public Visible(NodeSupportDomainSizer domainSizer) {
            this.domainSizer = domainSizer;
            boundarysList = new ArrayList<>(defaultBoundaryListCapacity);
        }

        @Override
        public void setPDTypes(PartDiffOrd[] types) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void filterNodes(Collection<Boundary> bnds, Coordinate center, List<Node> nodes, List<Node> results) {
            results.clear();

            if (bnds == null||bnds.size()==0) {
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
                Coordinate rear = bn.getBoudaryPoint(0);
                Coordinate front = bn.getBoudaryPoint(bn.getBoudaryPointsSize()-1);
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
                if (t < 0) {
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



                for (Boundary bn : bounds) {
                    Coordinate rear = bn.getBoudaryPoint(0);
                    Coordinate front = bn.getBoudaryPoint(1);
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
        public double distanceSqure(Node node, Coordinate center) {
            Coordinate coord = node.coordinate;
            double x1 = coord.x;
            double y1 = coord.y;
            double x2 = center.x;
            double y2 = center.y;
            double dx = x1 - x2;
            double dy = y1 - y2;
            return dx * dx + dy * dy;
        }

        @Override
        public double distance(Node node, Coordinate center) {
            Coordinate coord = node.coordinate;
            double x1 = coord.x;
            double y1 = coord.y;
            double x2 = center.x;
            double y2 = center.y;
            double dx = x1 - x2;
            double dy = y1 - y2;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
