/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import static java.lang.Math.*;
import net.epsilony.simpmeshfree.model.geometry.Point;

/**
 *
 * @author epsilon
 */
public class ViewTransform extends AffineTransform {

    double width, height, x1, x2, y1, y2;
    private AffineTransform oriTrans = new AffineTransform();
    double topMargin, leftMargin, downMargin, rightMargin;


    {
        topMargin = leftMargin = rightMargin = downMargin = 10;
    }

    public Point2D inverseTransform(double x1, double y1, Point2D dstPt) throws NoninvertibleTransformException {
        return inverseTransform(new Point2D.Double(x1, y1), dstPt);
    }

    public void setMargin(double top, double down, double left, double right) {
        topMargin = top;
        downMargin = down;
        leftMargin = left;
        rightMargin = right;
    }

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
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
        this.setTransform(oriTrans);
        double dx = Math.abs(zx2 - zx1);
        double dy = Math.abs(zy2 - zy1);
        translate((getWidth() + leftMargin - rightMargin) / 2, (getHeight() + topMargin - downMargin) / 2);
        double t = Math.min((getWidth() - leftMargin - rightMargin) / dx, (getHeight() - topMargin - downMargin) / dy);
        scale(t, -t);
        translate(-(zx1 + zx2) / 2, -(zy1 + zy2) / 2);
    }
    private static Point2D modelPt,  screenPt;


    static {
        modelPt = new Point2D.Double();
        screenPt = new Point2D.Double();
    }

    private Rectangle2D rectMarker=new Rectangle2D.Double();
    private Path2D pathMarker=new Path2D.Double();
    private Ellipse2D ellMarker=new Ellipse2D.Double();
    public Shape viewMarker(double x, double y, double size, ViewMarkerType type) {
        modelPt.setLocation(x, y);
        transform(modelPt, screenPt);
        Shape result;
        size=Math.abs(size);
        switch (type) {
            case Rectangle:
                rectMarker.setRect(screenPt.getX() - size / 2, screenPt.getY() - size / 2, size, size);
                result=rectMarker;
                break;
            case X:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX()-size/2, screenPt.getY()-size/2);
                pathMarker.lineTo(screenPt.getX()+size/2, screenPt.getY()+size/2);
                pathMarker.moveTo(screenPt.getX()-size/2, screenPt.getY()+size/2);
                pathMarker.lineTo(screenPt.getX()+size/2, screenPt.getY()-size/2);
                result=pathMarker.createTransformedShape(oriTrans);
                break;
            case Round:
                ellMarker.setFrame(screenPt.getX()-size/2, screenPt.getY()-size/2, size, size);
                result=ellMarker;
                break;
            case DownTriangle:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX()-size*sqrt(3)/4, screenPt.getY()-size/4);
                pathMarker.lineTo(screenPt.getX()+size*sqrt(3)/4, screenPt.getY()-size/4);
                pathMarker.lineTo(screenPt.getX(), screenPt.getY()+size/2);
                pathMarker.closePath();
                result=pathMarker.createTransformedShape(oriTrans);
                break;
            case UpTriangle:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX()-size*sqrt(3)/4, screenPt.getY()+size/4);
                pathMarker.lineTo(screenPt.getX()+size*sqrt(3)/4, screenPt.getY()+size/4);
                pathMarker.lineTo(screenPt.getX(), screenPt.getY()-size/2);
                pathMarker.closePath();
                result=pathMarker.createTransformedShape(oriTrans);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }

    public Shape viewMarker(Point p, double size, ViewMarkerType type) {
        return viewMarker(p.getX(), p.getY(), size, type);
    }

    public double inverseTransLength(double length){
        return length/getScaleX();
    }
}
