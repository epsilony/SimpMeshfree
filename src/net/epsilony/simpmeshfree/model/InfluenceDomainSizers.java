/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

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

        public Array(List<Node> nodes, DomainSizer sizer) {
            nodesInfRads = new double[nodes.size()];
            for (Node nd : nodes) {
                double size= sizer.domain(nd, null);
                nodesInfRads[nd.id]=size;
                if(size>maxSize){
                    maxSize=size;
                }
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
