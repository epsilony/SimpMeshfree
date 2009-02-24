/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;



/**
 *
 * @author epsilon
 */
public class Point extends Primitive{
double x,y;

 public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    protected Point(){
    }

    protected void setX(double x) {
        this.x = x;
    }

    protected void setY(double y) {
        this.y = y;
    }
    
    protected void setXY(Point p){
        this.x=p.x;
        this.y=p.y;
    }
}
