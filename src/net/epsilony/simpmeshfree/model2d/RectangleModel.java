/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import static java.lang.Math.ceil;
import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.utils.geom.Node;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Quadrangle;
import net.epsilony.utils.geom.Triangle;

/**
 *
 * @author epsilon
 */
public class RectangleModel {

    double width, height;
    double lineSize;
    double spaceNdsDis;
    private double[] xs;
    private double[] ys;
    private double[] nxs;
    private double[] nys;

    public RectangleModel(double width, double height, double lineSize, double spaceNdsDis) {
        
        init(width, height, lineSize, spaceNdsDis);
    }

    private void init(double width, double height, double lineSize, double spaceNdsDis) {
        this.width = width;
        this.height = height;
        this.lineSize = lineSize;
        this.spaceNdsDis = spaceNdsDis;
        double xSize = width / ceil(width / lineSize);
        double ySize = height / ceil(height / lineSize);
        xs = new double[(int) ceil(width / lineSize) + 1];
        ys = new double[(int) ceil(height / lineSize) + 1];
        xs[0] = 0;
        xs[xs.length - 1] = width;
        ys[0] = height / 2;
        ys[ys.length - 1] = - height / 2;
        for (int i = 1; i < xs.length - 1; i++) {
            xs[i] = xSize * i;
        }
        for (int i = 1; i < ys.length - 1; i++) {
            ys[i] = height / 2 - ySize * i;
        }

        xSize = width / ceil(width / spaceNdsDis);
        ySize = height / ceil(height / spaceNdsDis);
        nxs = new double[(int) ceil(width / spaceNdsDis)-1];
        nys = new double[(int) ceil(height / spaceNdsDis)-1];
        for (int i = 0; i < nxs.length; i++) {
            nxs[i] = xSize * (i + 1);
        }
        for (int i = 0; i < nys.length; i++) {
            nys[i] = height / 2 - ySize * (i + 1);
        }
    }

    public ArrayList<LineBoundary> boundaries() {
        Node start = new Node(xs[0], ys[0]);
        Node allStart = start;
        ArrayList<LineBoundary> res = new ArrayList<>(xs.length * 2 + ys.length * 2 - 4);
        for (int i = 1; i < ys.length; i++) {
            Node end = new Node(xs[0], ys[i]);
            res.add(new LineBoundary(start, end));
            start = end;
        }

        for (int i = 1; i < xs.length; i++) {
            Node end = new Node(xs[i], ys[ys.length - 1]);
            res.add(new LineBoundary(start, end));
            start = end;
        }

        for (int i = 1; i < ys.length; i++) {
            Node end = new Node(xs[xs.length - 1], ys[ys.length - 1 - i]);
            res.add(new LineBoundary(start, end));
            start = end;
        }

        for (int i = 1; i < xs.length - 1; i++) {
            Node end = new Node(xs[xs.length - 1 - i], ys[0]);
            res.add(new LineBoundary(start, end));
            start = end;
        }

        res.add(new LineBoundary(start, allStart));
        return res;
    }

    public ArrayList<Node> spaceNodes() {
        ArrayList<Node> res = new ArrayList<>(nxs.length * nys.length);
        for (int i = 0; i < nxs.length; i++) {
            for (int j = 0; j < nys.length; j++) {
                res.add(new Node(nxs[i], nys[j]));
            }
        }
        return res;
    }

    public ArrayList<Triangle> triangles() {
        ArrayList<Triangle> res = new ArrayList<>(2 * (xs.length - 1) * (ys.length - 1));
        Coordinate[][] coords = new Coordinate[xs.length][ys.length];
        for (int i = 0; i < xs.length; i++) {
            for (int j = 0; j < ys.length; j++) {
                coords[i][j] = new Coordinate(xs[i], ys[j]);
            }
        }

        for (int i = 0; i < xs.length - 1; i++) {
            for (int j = 0; j < ys.length - 1; j++) {
                Triangle tri = new Triangle(false);
                tri.c1 = coords[i][j];
                tri.c2 = coords[i][j + 1];
                tri.c3 = coords[i + 1][j + 1];
                res.add(tri);
                tri = new Triangle(false);
                tri.c1 = coords[i][j];
                tri.c2 = coords[i + 1][j + 1];
                tri.c3 = coords[i + 1][j];
                res.add(tri);
            }
        }
        return res;
    }

    public ArrayList<Quadrangle> quadrangles() {
        ArrayList<Quadrangle> res = new ArrayList<>((xs.length - 1) * (ys.length - 1));
        for (int i = 0; i < xs.length - 1; i++) {
            for (int j = 0; j < ys.length - 1; j++) {
                Quadrangle quad = new Quadrangle(xs[i], ys[j + 1], xs[i + 1], ys[j + 1], xs[i + 1], ys[j], xs[i], ys[j]);
                res.add(quad);
            }
        }
        return res;
    }
}
