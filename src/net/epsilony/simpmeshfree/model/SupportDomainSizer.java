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
    double getRadiumSquare(Node node){
        return radiumSquares[node.id];
    }
    
    double getRadium(Node node){
        return radiums[node.id];
    }
    double[] getRadiumSquares(){
        return radiumSquares;
    }
    double[] getRadiums(){
        return radiums;
    }
}
