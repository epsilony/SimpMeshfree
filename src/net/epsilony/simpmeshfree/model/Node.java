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
    
    public Node(double x,double y,double z){
        coordinate=new Coordinate(x,y,z);
    }
    
    public Node(Coordinate coord){
        coordinate=coord;
    }
    
    public Node(){
        
    }

    @Override
    public String toString() {
        Coordinate coord=coordinate;
        return String.format("n(%f,%f,%f)", coord.x,coord.y,coord.z);
    }
    
    
}
