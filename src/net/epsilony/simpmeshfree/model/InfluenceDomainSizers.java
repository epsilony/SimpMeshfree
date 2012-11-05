/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;

/**
 *
 * @author epsilon
 */
public class InfluenceDomainSizers {

    private InfluenceDomainSizers() {
    }
    
    public static class Array implements InfluenceDomainSizer {

        private double[] nodesInfRads;
        double maxSize=0;

        public Array(List<Node> nodes, double[] rads) {
            nodesInfRads = new double[nodes.size()];
            int i=0;
            for (Node nd : nodes) {
                double size= rads[i];
                
                nodesInfRads[nd.id]=size;
                if(size>maxSize){
                    maxSize=size;
                }
                
                i++;
            }
        }

        @Override
        public double getSize(Node node) {
            return nodesInfRads[node.id];
        }

        @Override
        public double getMaxSize() {
            return maxSize;
        }
    }
    
    public static InfluenceDomainSizer constantSizer(final double rad){
    return new InfluenceDomainSizer() {

            @Override
            public double getSize(Node nd) {
                return rad;
            }

            @Override
            public double getMaxSize() {
                return rad;
            }
        };
}
    
    public static InfluenceDomainSizer byGivenRads(List<Node> nds,double[] rads){
        return new Array(nds,rads);
    }
    
    public static InfluenceDomainSizer bySupportDomainSizer(List<Node> nds,SupportDomainSizer sizer){
        double[] rads=new double[nds.size()];
        int i=0;
        for(Node nd:nds){
            rads[i]=sizer.domain(nd, null);
            i++;
        }
        return byGivenRads(nds, rads);
    }
    
    public static TDoubleArrayList getInfRadius(List<Node> nodes, InfluenceDomainSizer infSizer, TDoubleArrayList results){
        if(null==results){
            results=new TDoubleArrayList(nodes.size());
        }else{
            results.resetQuick();
            results.ensureCapacity(nodes.size());
        }
        for(Node nd:nodes){
            results.add(infSizer.getSize(nd));
        }
        return results;
    }
}
