/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.epsilony.geom.Coordinate;
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
        ArrayList<Node> filtedNodes;
        Comparator<Node> angleComp=new AngleComparator();
        @Override
        public void setPDTypes(PartialDiffType[] types) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void filterNodes(Collection<Boundary> bnds, Coordinate center, List<Node> nodes, List<Node> results) {
            double[] radSquares=domainSizer.radiumSquares;
            ArrayList<Node> fns=filtedNodes;
            fns.clear();
            double cx=center.x;
            double cy=center.y;
            double[] radSqs=domainSizer.radiumSquares;
            
            for(Node nd:nodes){
                Coordinate coord=nd.coordinate;
                double x=coord.x;
                double y=coord.y;
                double dx=(x-cx);
                double dy=(y-cy);
                double disSq=dx*dx+dy*dy;
                if(disSq>=radSqs[nd.id]){
                    continue;
                }
                fns.add(nd);
            }
            
            Collections.sort(fns, angleComp);
            for(Boundary bnd:bnds){
                
            }
        }

        @Override
        public double distanceSqure(Node node, Coordinate center) {
            Coordinate coord=node.coordinate;
            double x1=coord.x;
            double y1=coord.y;
            double x2=center.x;
            double y2=center.y;
            double dx=x1-x2;
            double dy=y1-y2;
            return dx*dx+dy*dy;
        }

        @Override
        public double distance(Node node, Coordinate center) {
            Coordinate coord=node.coordinate;
            double x1=coord.x;
            double y1=coord.y;
            double x2=center.x;
            double y2=center.y;
            double dx=x1-x2;
            double dy=y1-y2;
            return Math.sqrt(dx*dx+dy*dy);
        }
    }
}
