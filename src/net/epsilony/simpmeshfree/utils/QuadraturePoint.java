/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class QuadraturePoint{
    public double weight(){
        return weight;
    }
    public Coordinate coordinate(){
        return coordinate;
    }
    
    public double weight;
    public Coordinate coordinate;
    public Coordinate parameter;
    public QuadraturePoint(){
        coordinate=new Coordinate();
        parameter=new Coordinate();
    }
}
