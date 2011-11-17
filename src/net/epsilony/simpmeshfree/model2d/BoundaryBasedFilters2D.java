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
import net.epsilony.simpmeshfree.model.PartialDiffType;
import net.epsilony.simpmeshfree.model.SupportDomainSizer;

/**
 *
 * @author epsilon
 */
public class BoundaryBasedFilters2D {

    public static class AngleComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            Coordinate cood = o1.coordinate;

            double tan1 = Math.atan2(cood.x, cood.y);
            cood = o2.coordinate;
            double tan2 = Math.atan2(cood.x, cood.y);
            if (tan1 < tan2) {
                return -1;
            } else if (tan1 > tan2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static class Visible implements BoundaryBasedFilter {

        SupportDomainSizer domainSizer;
//        Comparator<Node> angleComp = new AngleComparator();
        ArrayList<Boundary> boundarysList;
        public static int defaultBoundaryListCapacity = 50;

        public Visible(SupportDomainSizer domainSizer) {
            this.domainSizer = domainSizer;
            boundarysList = new ArrayList<>(defaultBoundaryListCapacity);
        }

        @Override
        public void setPDTypes(PartialDiffType[] types) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void filterNodes(Collection<Boundary> bnds, Coordinate center, List<Node> nodes, List<Node> results) {
            results.clear();

            ArrayList<Boundary> bounds = boundarysList;
            bounds.clear();


            double cx = center.x;
            double cy = center.y;
            boolean centerOnBoundary=false;
            double cFx = 0,cFy=0,cRx=0,cRy=0;
            for (Boundary bn : bnds) {
                Coordinate rear = bn.getBoudaryCoordinate(0);
                Coordinate front = bn.getBoudaryCoordinate(1);
                if(front==center){
                    centerOnBoundary=true;
                    cRx=rear.x;
                    cRy=rear.y;
                    continue;
                }
                if(rear==center){
                    cFx=front.x;
                    cFy=front.y;
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

            double[] radSqs = domainSizer.radiumSquares;

            for (Node nd : nodes) {
                Coordinate coord = nd.coordinate;
                double x = coord.x;
                double y = coord.y;
                double dx = (x - cx);
                double dy = (y - cy);
                double disSq = dx * dx + dy * dy;
                if (disSq >= radSqs[nd.id]) {
                    continue;
                }
                
                if(coord==center){
                    results.add(nd);
                    continue;
                }
                if(centerOnBoundary){
                    double t1=GeometryMath.crossProduct(cx, cy, cFx, cFy, x, y);
                    if(t1<0){
                        continue;
                    }
                    double t2=GeometryMath.crossProduct(cRx, cRy, cx, cy, x, y);
                    if(t2<0){
                        continue;
                    }
                }
                
                boolean add = true;
                
                

                for (Boundary bn : bounds) {
                    Coordinate rear = bn.getBoudaryCoordinate(0);
                    Coordinate front = bn.getBoudaryCoordinate(1);
                    if(coord==rear||coord==front){
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

                    if (t12 > 0 || t34 > 0||t12>=0&&t34==0) {
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
