/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class Point extends ModelElement {

    double x, y;
    static ModelElementIndexManager pointIm = new ModelElementIndexManager();

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point(double x, double y) {
        index=pointIm.getNewIndex();
        this.x = x;
        this.y = y;
    }

    public Point(Point p){
        this.x=p.x;
        this.y=p.y;
        index=pointIm.getNewIndex();
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

    @Override
    public ModelElementType getType() {
        return ModelElementType.Point;
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return pointIm;
    }
}
