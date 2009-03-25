/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.geometry.ApproximatePoint;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.Node;

/**
 *
 * @author epsilon
 */
public class MechanicsModel {
    GeometryModel gm;
    SupportDomain supportDomain=null;

    public MechanicsModel(GeometryModel gm) {
        this.gm = gm;
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }
    
    public class RoundSupportDomain implements SupportDomain{

        double r;
        @Override
        public List<Node> supportNode(Node n, List<Node> list) {
            double x=n.getX();
            double y=n.getY();
            LinkedList<Node> nodes=new LinkedList<Node>();
            gm.nodeDomainSearch(x-r, y-r, x+r, y+2, nodes);
            LinkedList<ApproximatePoint> aps=new LinkedList<ApproximatePoint>();

            gm.pointDomainSearch(ApproximatePoint.tempApproximatePoint(x, y), r+gm.getSegmentApproximateSize(), aps);
            for(Node node:list){
                if(!n.isInDistance(node.getX(),node.getY(), r)){
                    continue;
                }
                if(GeometryModel.canSeeEach(n, node, aps)){
                    list.add(node);
                }
            }
            return list;
        }

        @Override
        public List<Node> supportNode(double x, double y, List<Node> list) {
            return supportNode(Node.tempNode(x, y),list);
        }
    }

    public class LayerSupportDomain implements SupportDomain{
        int layer;
        double searchRadiu;
        LinkedList<ApproximatePoint> aps=new LinkedList<ApproximatePoint>();
        ArrayList<Node> nodes=new ArrayList<Node>(100);
        private ApproximatePoint tempApproximatePoint1;
        private ApproximatePoint tempApproximatePoint2;
        {
            tempApproximatePoint1 = ApproximatePoint.tempApproximatePoint(0, 0);
            tempApproximatePoint2 = ApproximatePoint.tempApproximatePoint(0, 0);
        }
        @Override
        public List<Node> supportNode(Node n, List<Node> list) {
            list.clear();
            n.bfsTraverse(layer, nodes);
            double minX,maxX,minY,maxY,x,y;
            minX=maxX=n.getX();
            minY=maxY=n.getY();
            for(Node node:nodes){
                x=node.getX();
                y=node.getY();
                if(x>maxX){
                    maxX=x;
                }else{
                    if(x<minX){
                        minX=x;
                    }
                }
                if(y>maxY){
                    maxY=y;
                }else{
                    if(y<minY){
                        minY=y;
                    }
                }
            }
            tempApproximatePoint1.setXY(minX,minY);
            tempApproximatePoint2.setXY(maxX,maxY);
            gm.pointDomainSearch(tempApproximatePoint1, tempApproximatePoint2,aps);
            for(Node node:nodes){
                if(GeometryModel.canSeeEach(n, node, aps)){
                    list.add(node);
                }
            }
            return list;
        }

        @Override
        public List<Node> supportNode(double x, double y, List<Node> list) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
}
