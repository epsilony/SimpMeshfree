/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class SupportDomainSizer {
    public double[] radiumSquares;
    public double[] radiums;
    public double getRadiumSquare(Node node){
        return radiumSquares[node.id];
    }
    
    public double getRadium(Node node){
        return radiums[node.id];
    }
    public double[] getRadiumSquares(){
        return radiumSquares;
    }
    public double[] getRadiums(){
        return radiums;
    }
    
    public SupportDomainSizer(int nodeNum){
        radiumSquares=new double[nodeNum];
        radiums=new double[nodeNum];
    }
}
