/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface NodeSupportDomainSizer {

    double getRadium(Node node);

    double getRadiumSquare(Node node);
    
    double getMaxRadium();
    
}
