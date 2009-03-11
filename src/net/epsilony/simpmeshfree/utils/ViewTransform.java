/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.epsilony.simpmeshfree.model.Point;

/**
 *
 * @author epsilon
 */
public class ViewTransform extends AffineTransform {

    double width, height, x1, x2, y1, y2;
    double topMargin,leftMargin,downMargin,rightMargin;
    {
        topMargin=30;
        leftMargin=rightMargin=downMargin=10;
    }

    public void setMargin(double top,double down,double left,double right) {
        topMargin=top;
        downMargin=down;
        leftMargin=left;
        rightMargin=right;
    }


    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public ViewTransform(double width, double height, double x1, double y1, double x2, double y2) {
        this.width = width;
        this.height = height;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        viewWhole();
    }

    public void viewMove(double dx, double dy) {
        AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);//);
        preConcatenate(tx);
    }

    public void viewScale(double centerX, double centerY, double t) {
        AffineTransform tx = AffineTransform.getTranslateInstance(centerX, centerY);
        tx.scale(t, t);
        tx.translate(-centerX, -centerY);
        preConcatenate(tx);
    }

    public void viewWhole() {
        viewZoom(x1, y1, x2, y2);
    }

    public AffineTransform viewTranslate() {
        return AffineTransform.getTranslateInstance(getTranslateX(), getTranslateY());
    }

    public void viewZoom(double zx1, double zy1, double zx2, double zy2) {
        this.setToIdentity();
        double dx = Math.abs(zx2 - zx1);
        double dy = Math.abs(zy2 - zy1);
        translate((getWidth() +leftMargin-rightMargin)/ 2, (getHeight() +topMargin-downMargin)/ 2);
        double t =Math.min((getWidth()-leftMargin-rightMargin) / dx, (getHeight()-topMargin-downMargin) / dy);
        scale(t, -t);
        translate(-(zx1 + zx2) / 2, -(zy1 + zy2) / 2);
    }
    private static Point2D scrPt,  distPt;


    static {
        scrPt = new Point2D.Double();
        distPt = new Point2D.Double();
    }

    public Shape viewMarker(double x, double y, double size, ViewMarkerType type, Shape sp) {
        scrPt.setLocation(x, y);
        transform(scrPt, distPt);
        switch (type) {
            case Rectangle:
                ((Rectangle2D) sp).setRect(distPt.getX()-size/2,distPt.getY()-size/2,size,size);
                break;
        }
        return sp;
    }

    public Shape viewMarker(Point p,double size,ViewMarkerType type,Shape sp){
        return viewMarker(p.getX(),p.getY(),size,type,sp);
    }
}
