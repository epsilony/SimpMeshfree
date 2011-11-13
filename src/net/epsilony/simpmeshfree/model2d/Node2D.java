/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;
import net.epsilony.simpmeshfree.model.Node;


/**
 *
 * @author epsilon
 */
public class Node2D extends Node<Coordinate2D>{
    public Node2D(double x,double y){
        coordinate=new Coordinate2D(x, y);
    }
}
