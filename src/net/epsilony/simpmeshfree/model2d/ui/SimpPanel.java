/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.ui;

import java.awt.*;
import java.awt.geom.*;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model2d.LineBoundary2D;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class SimpPanel extends LayerUI<JPanel> {

    List<Node> nodes;
    List<Boundary> boundaries;
    List<Coordinate> resultCoords;
    List<double[]> results;
    double nodeRad = 5;
    double headRad = 5;

    public SimpPanel(List<Node> nodes, List<Boundary> boundaries, List<Coordinate> resultCoords, List<double[]> results) {
        this.nodes = nodes;
        this.boundaries = boundaries;
        this.resultCoords = resultCoords;
        this.results = results;
    }
    int margin = 50;
    private double resultLengthFactor = 2;

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        Graphics2D g2 = (Graphics2D) g.create();
        int w = c.getWidth();
        int h = c.getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape boundShape = boundariesShape();
        Shape resultsShape=resultShape(0, resultLengthFactor);
        Rectangle2D bounds2D = boundShape.getBounds2D();
        bounds2D.add(resultsShape.getBounds2D());
        double maxX = bounds2D.getMaxX();
        double maxY = bounds2D.getMaxY();
        double minX = bounds2D.getMinX();
        double minY = bounds2D.getMinY();
        double width = maxX - minX;
        double height = maxY - minY;
        double t = w * height - width * h;

        double x0, y0, scale;
        if (t >= 0) {
            double height2 = h - margin * 2;
            scale = height2 / height;
            double width2 = scale * width;
            x0 = (w - width2) / 2;
            y0 = margin;
        } else {
            double width2 = w - margin * 2;
            scale = width2 / width;
            double height2 = scale * height;
            x0 = margin;
            y0 = (h - height2) / 2 +height2/2;
        }
        g2.setColor(Color.red);
        AffineTransform shapeTrans = AffineTransform.getScaleInstance(scale, -scale);
        shapeTrans.preConcatenate(AffineTransform.getTranslateInstance(x0, y0));
        
        g2.draw(shapeTrans.createTransformedShape(boundShape));
        g2.draw(shapeTrans.createTransformedShape(nodesShape(nodeRad / scale)));
        g2.draw(shapeTrans.createTransformedShape(resultShape(headRad / scale, resultLengthFactor)));

        g2.dispose();
    }

    Shape boundariesShape() {

        Path2D path = new Path2D.Double();
        if (null != boundaries) {
            for (Boundary bound : boundaries) {
                LineBoundary2D bnd=(LineBoundary2D)bound;
                path.moveTo(bnd.rear.x, bnd.rear.y);
                path.lineTo(bnd.front.x, bnd.front.y);
            }
        }
        return path.createTransformedShape(null);
    }

    Shape nodesShape(double nodeRadium) {
        Path2D path = new Path2D.Double();
        if (nodes != null) {
            for (Node nd : nodes) {
                double nx = nd.coordinate.x;
                double ny = nd.coordinate.y;
                path.append(new Ellipse2D.Double(nx - nodeRadium, ny - nodeRadium, 2 * nodeRadium, 2 * nodeRadium), false);
            }
        }
        return path.createTransformedShape(null);
    }

    Shape resultShape(double headRad, double lengthFac) {
        Path2D path = new Path2D.Double();
        if(null==results){
            return path.createTransformedShape(null);
        }
        Iterator<double[]> resultIter = results.iterator();
        for (Coordinate coordinate : resultCoords) {
            double[] result = resultIter.next();
            double x = coordinate.x;
            double y = coordinate.y;
            //+
            path.append(new Line2D.Double(x - headRad, y, x + headRad, y), false);
            path.append(new Line2D.Double(x, y - headRad, x, y + headRad), false);

            // 线
            double x2 = x + result[0] * lengthFac;
            double y2 = y + result[1] * lengthFac;
            path.append(new Line2D.Double(x, y, x2, y2), false);

            // x
            path.append(new Line2D.Double(x2 - headRad, y2 - headRad, x2 + headRad, y2 + headRad), false);
            path.append(new Line2D.Double(x2 - headRad, y2 + headRad, x2 + headRad, y2 - headRad), false);

        }
        return path.createTransformedShape(null);
    }
}
