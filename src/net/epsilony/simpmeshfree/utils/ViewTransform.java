/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.Shape;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
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
public class ViewTransform extends AffineTransform implements MouseMotionListener, MouseListener, MouseWheelListener, JPanelPainter {

    double minX, maxX, minY, maxY;
    private AffineTransform oriTrans = new AffineTransform();
    double topMargin, leftMargin, downMargin, rightMargin;
    JPanel panel;
    ViewOperation viewOperation = ViewOperation.None;
    final Shape[] rubbers = new Shape[1];
    final int zoomRubberI = 0;
    Color zooRubberColor = Color.BLUE;

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        switch (viewOperation) {
            case MoveFirstPointSet:
                viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                viewFirstX = e.getX();
                viewFirstY = e.getY();
                panel.repaint();
                return;

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        switch (viewOperation) {
            case ZoomFirstPointSet:
                Graphics2D g2 = (Graphics2D) panel.getGraphics();
                g2.setColor(zooRubberColor);
                g2.setXORMode(Color.WHITE);
                if (null == rubbers[zoomRubberI]) {
                    rubbers[zoomRubberI] = new Rectangle2D.Double(viewFirstX, viewFirstY, e.getX() - viewFirstX, e.getY() - viewFirstY);
                    g2.draw(rubbers[zoomRubberI]);
                } else {
                    g2.draw(rubbers[zoomRubberI]);
                    ((Rectangle2D) rubbers[zoomRubberI]).setRect(viewFirstX, viewFirstY, e.getX() - viewFirstX, e.getY() - viewFirstY);
                    g2.draw(rubbers[zoomRubberI]);
                }
        }
    }
    int viewFirstX, viewFirstY;

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:

                switch (e.getClickCount()) {
                    case 2:
                        viewWhole();
                        viewOperation = ViewOperation.None;
                        panel.repaint();
                        return;
                }
                break;
            case MouseEvent.BUTTON1:
                switch (viewOperation) {
                    case None:
                        break;
                    case MoveStarted:
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            viewFirstX = e.getX();
                            viewFirstY = e.getY();
                            viewOperation = ViewOperation.MoveFirstPointSet;
                        }
                        break;
                    case MoveFirstPointSet:
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                            viewOperation = ViewOperation.None;
                            panel.repaint();
                            return;
                        }
                        break;
                    case ZoomStarted:
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            viewFirstX = e.getX();
                            viewFirstY = e.getY();
                            viewOperation = ViewOperation.ZoomFirstPointSet;
                        }
                        break;
                    case ZoomFirstPointSet:
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            try {
                                inverseTransform(viewFirstX, viewFirstY, modelPt);
                                inverseTransform(e.getX(), e.getY(), modelPt2);
                            } catch (NoninvertibleTransformException ex) {
                                Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            viewZoom(modelPt.getX(), modelPt.getY(), modelPt2.getX(), modelPt2.getY());
                            viewOperation = ViewOperation.None;
                            panel.repaint();
                            return;
                        }
                        break;
                }

        }
    }

    public void startViewZoom() {
        if (rubbers[zoomRubberI] != null) {
            Graphics2D g2 = (Graphics2D) panel.getGraphics();
            g2.setColor(zooRubberColor);
            g2.setXORMode(Color.white);
            g2.draw(rubbers[zoomRubberI]);
            rubbers[zoomRubberI] = null;
        }
        viewOperation = ViewOperation.ZoomStarted;
    }

    public void startViewMove() {
        viewOperation = ViewOperation.ZoomStarted;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                viewFirstX = e.getX();
                viewFirstY = e.getY();
                viewOperation = ViewOperation.MoveFirstPointSet;
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                if (viewOperation == ViewOperation.MoveFirstPointSet) {
                    viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                    viewOperation = ViewOperation.None;
                    panel.repaint();
                    return;
                }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        switch (viewOperation) {
            case ZoomFirstPointSet:
                Graphics2D g2 = (Graphics2D) panel.getGraphics();
                g2.setColor(zooRubberColor);
                g2.setXORMode(Color.white);
                if (null != rubbers[zoomRubberI]) {
                    g2.draw(rubbers[zoomRubberI]);
                }
                rubbers[zoomRubberI] = new Rectangle2D.Double(viewFirstX, viewFirstY, e.getX() - viewFirstX, e.getY() - viewFirstY);
                g2.draw(rubbers[zoomRubberI]);
                break;

        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        switch (viewOperation) {
            case ZoomFirstPointSet:
                if (null != rubbers[zoomRubberI]) {
                    Graphics2D g2 = (Graphics2D) panel.getGraphics();
                    g2.setColor(zooRubberColor);
                    g2.setXORMode(Color.white);
                    g2.draw(rubbers[zoomRubberI]);
                    rubbers[zoomRubberI] = null;
                }
                break;
        }
    }
    double wheelRatio = 0.05;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double r = Math.pow(1 + wheelRatio, e.getWheelRotation());
        viewScale(e.getX(), e.getY(), r);
        panel.repaint();
    }

    @Override
    public void setViewTransform(ViewTransform vt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum ViewOperation {

        None,
        MoveStarted, MoveFirstPointSet,
        ZoomStarted, ZoomFirstPointSet,
    }


    {
        topMargin = leftMargin = rightMargin = downMargin = 10;
    }

    public void setModelBound(double x1, double y1, double x2, double y2) {
        this.minX = x1;
        this.maxX = x2;
        this.minY = y1;
        this.maxY = y2;
    }
    private Point2D forInverseTransform = new Point2D.Double();

    public Point2D inverseTransform(double scrX, double scrY, Point2D dstPt) throws NoninvertibleTransformException {
        forInverseTransform.setLocation(scrX, scrY);
        return inverseTransform(forInverseTransform, dstPt);
    }

    public void setMargin(double top, double down, double left, double right) {
        topMargin = top;
        downMargin = down;
        leftMargin = left;
        rightMargin = right;
    }

    @Override
    public void paint(Graphics2D g2) {
        for (int i = 0; i < rubbers.length; i++) {
            rubbers[i] = null;
        }
    }

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
    }

    public ViewTransform(double x1, double y1, double x2, double y2) {
        this.minX = x1;
        this.maxX = x2;
        this.minY = y1;
        this.maxY = y2;
        viewWhole();
    }

    public ViewTransform() {
        super();
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

    public void viewZoom(double zx1, double zy1, double zx2, double zy2) {
        this.setTransform(oriTrans);
        double dx = Math.abs(zx2 - zx1);
        double dy = Math.abs(zy2 - zy1);
        translate((panel.getWidth()) / 2, panel.getHeight() / 2);
        double t = Math.min((panel.getWidth() - leftMargin - rightMargin) / dx, (panel.getHeight() - topMargin - downMargin) / dy);
        scale(t, -t);
        translate(-(zx1 + zx2) / 2, -(zy1 + zy2) / 2);
    }

    public void viewWhole() {
        viewZoom(minX, minY, maxX, maxY);
    }

    public AffineTransform viewTranslate() {
        return AffineTransform.getTranslateInstance(getTranslateX(), getTranslateY());
    }
    private static Point2D modelPt = new Point2D.Double(),  modelPt2 = new Point2D.Double(),  screenPt = new Point2D.Double();


    static {
        modelPt = new Point2D.Double();
        screenPt = new Point2D.Double();
    }
    private Rectangle2D rectMarker = new Rectangle2D.Double();
    private Path2D pathMarker = new Path2D.Double();
    private Ellipse2D ellMarker = new Ellipse2D.Double();

    public Shape viewMarker(double x, double y, double size, ViewMarkerType type) {
        modelPt.setLocation(x, y);
        transform(modelPt, screenPt);
        Shape result;
        size = Math.abs(size);
        switch (type) {
            case Rectangle:
                rectMarker.setRect(screenPt.getX() - size / 2, screenPt.getY() - size / 2, size, size);
                result = rectMarker;
                break;
            case X:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX() - size / 2, screenPt.getY() - size / 2);
                pathMarker.lineTo(screenPt.getX() + size / 2, screenPt.getY() + size / 2);
                pathMarker.moveTo(screenPt.getX() - size / 2, screenPt.getY() + size / 2);
                pathMarker.lineTo(screenPt.getX() + size / 2, screenPt.getY() - size / 2);
                result = pathMarker.createTransformedShape(oriTrans);
                break;
            case Round:
                ellMarker.setFrame(screenPt.getX() - size / 2, screenPt.getY() - size / 2, size, size);
                result = ellMarker;
                break;
            case DownTriangle:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX() - size * sqrt(3) / 4, screenPt.getY() - size / 4);
                pathMarker.lineTo(screenPt.getX() + size * sqrt(3) / 4, screenPt.getY() - size / 4);
                pathMarker.lineTo(screenPt.getX(), screenPt.getY() + size / 2);
                pathMarker.closePath();
                result = pathMarker.createTransformedShape(oriTrans);
                break;
            case UpTriangle:
                pathMarker.reset();
                pathMarker.moveTo(screenPt.getX() - size * sqrt(3) / 4, screenPt.getY() + size / 4);
                pathMarker.lineTo(screenPt.getX() + size * sqrt(3) / 4, screenPt.getY() + size / 4);
                pathMarker.lineTo(screenPt.getX(), screenPt.getY() - size / 2);
                pathMarker.closePath();
                result = pathMarker.createTransformedShape(oriTrans);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }

    public Shape viewMarker(Point p, double size, ViewMarkerType type) {
        return viewMarker(p.getX(), p.getY(), size, type);
    }

    public double inverseTransLength(double length) {
        return length / getScaleX();
    }
}
