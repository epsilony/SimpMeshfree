/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public class Node{
    public int id;
    public Coordinate coordinate;

    public Node(double x, double y) {
        coordinate=new Coordinate(x,y);
    }
    
    public Node(){
        
    }
}
