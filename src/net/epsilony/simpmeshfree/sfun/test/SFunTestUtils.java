/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun.test;

import static java.lang.Math.*;
import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.simpmeshfree.utils.CoordinatePartDiffFunction;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public class SFunTestUtils {

    public static CoordinatePartDiffFunction sinSin(double x0, double y0, double w, double h, double wCir, double hCir,double wPhase,double hPhase) {
        return new SinSin(x0, y0, w, h, wCir, hCir, wPhase, hPhase);
    }

    public abstract static class Adapter implements CoordinatePartDiffFunction {

        protected int baseLen = 1;
        int diffOrder = 0;
        double h;
        double w;
        double x0;
        double y0;

        protected Adapter(double x0, double y0, double w, double h) {
            this.h = h;
            this.w = w;
            this.x0 = x0;
            this.y0 = y0;
        }

        @Override
        public int getDiffOrder() {
            return diffOrder;
        }

        @Override
        public void setDiffOrder(int order) {
            if (order < 0 || order > 1) {
                throw new UnsupportedOperationException();
            }
            this.diffOrder = order;
            baseLen = CommonUtils.len2DBase(order);
        }

        abstract double v(double x, double y);

        abstract double v_x(double x, double y);

        abstract double v_y(double x, double y);

        @Override
        public double[] values(Coordinate coord, double[] results) {
            if (null == results) {
                results = new double[baseLen];
            }
            double x = coord.x;
            double y = coord.y;
            for (int i = 0; i < baseLen; i++) {
                double value = 0;
                switch (i) {
                    case 0:
                        value = v(x, y);
                        break;
                    case 1:
                        value = v_x(x, y);
                        break;
                    case 2:
                        value = v_y(x, y);
                }
                results[i] = value;
            }
            return results;
        }
    }

    public static class SinSin extends Adapter {

        double wCir, hCir;
        double wPhase;
        double hPhase;

        public SinSin(double x0, double y0, double w, double h, double wCir, double hCir, double wPhase, double hPhase) {
            super(y0, x0, w, h);
            this.wCir = wCir;
            this.hCir = hCir;
            this.wPhase = wPhase;
            this.hPhase = hPhase;
        }

        @Override
        double v(double x, double y) {
            return sin(2 * PI * (x - x0) * wCir / w + wPhase) * sin(2 * PI * (y - y0) * hCir / h + hPhase);
        }

        @Override
        double v_x(double x, double y) {
            return 2 * PI * wCir / w * cos(2 * PI * (x - x0) * wCir / w + wPhase) * sin(2 * PI * (y - y0) * hCir / h + hPhase);
        }

        @Override
        double v_y(double x, double y) {
            return 2 * PI * hCir / h * sin(2 * PI * (x - x0) * wCir / w + wPhase) * cos(2 * PI * (y - y0) * hCir / h + hPhase);
        }
    }

    public static ArrayList<Node> genNodes(double x0, double y0, double w, double h, double step) {
        int wSize = (int) Math.ceil(w / step) + 1;
        int hSize = (int) Math.ceil(h / step) + 1;
        double wStep = w / (wSize - 1);
        double hStep = h / (hSize - 1);
        ArrayList<Node> results = new ArrayList<>(hSize * wSize);
        for (int i = 0; i < wSize; i++) {
            double x;
            if (i == wSize - 1) {
                x = x0 + w;
            } else {
                x = wStep * i + x0;
            }
            for (int j = 0; j < hSize; j++) {
                double y;
                if (j == hSize - 1) {
                    y = y0 + h;
                } else {
                    y = y0 + hStep * j;
                }
                results.add(new Node(x, y));
            }
        }
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setId(i);
        }
        return results;
    }
}
