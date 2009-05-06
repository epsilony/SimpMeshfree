/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;
import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;
import static java.lang.Math.*;

/**
 *
 * @author epsilon
 */
public class Route extends ModelElement {

    static ModelElementIndexManager routeIm = new ModelElementIndexManager();
    LinkedList<Segment> segments = new LinkedList<Segment>();

    public LinkedList<Segment> getSegments() {
        return segments;
    }

    public Route() {
        index = routeIm.getNewIndex();
    }

    public Segment getLast() {
        return segments.getLast();
    }

    public Segment getFirst() {
        return segments.getFirst();
    }

    public void clear() {
        segments.clear();
    }

    public boolean add(Segment e) {
        e.route = this;
        return segments.add(e);
    }
    LinkedList<ApproximatePoint> aprxPts = new LinkedList<ApproximatePoint>();

    public LinkedList<ApproximatePoint> GenerateApproximatePoints(double size, double flatness) {
        aprxPts.clear();
        for (Segment s : segments) {
            s.GenerateApproximatePoints(size, flatness, aprxPts);
        }
        ApproximatePoint tAp = aprxPts.getLast();
        for (ApproximatePoint ap : aprxPts) {
            ap.back = tAp;
            tAp.front = ap;
            tAp = ap;
        }
        return aprxPts;
    }

    public LinkedList<ApproximatePoint> GetApproximatePoints() {
        return aprxPts;
    }

    public boolean isCounterClockwise() {
        ApproximatePoint start, end;
        double sumAngle = 0;
        ListIterator<ApproximatePoint> aprxIterator = aprxPts.listIterator();
        start = aprxPts.getLast();
        end = aprxIterator.next();

        double angle, formAngle;
        formAngle = atan2(end.x - start.x, end.y - start.y);

        while (aprxIterator.hasNext()) {
            start = end;
            end = aprxIterator.next();
            angle = atan2(end.x - start.x, end.y - start.y);
            if (angle == formAngle || angle - formAngle == -2 * PI || angle - formAngle == 2 * PI || angle - formAngle == PI || angle - formAngle == -PI) {
                continue;
            }
            //线段左拐
            if (formAngle >= 0 && (angle > formAngle || angle < formAngle - PI)) {
                //左拐则加上拐夹角的正值
                if (angle < 0) {
                    sumAngle += 2 * PI + angle - formAngle;
                } else {
                    sumAngle += angle - formAngle;
                }
            } else if (formAngle < 0 && angle > formAngle && angle < formAngle + PI) {
                //左拐则加上拐夹角的正值
                sumAngle += angle - formAngle;
            } else {//线段右拐则加上拐夹角的负值
                if (formAngle >= 0) {
                    sumAngle += angle - formAngle;
                } else {
                    if (angle > 0) {
                        sumAngle += angle - formAngle - 2 * PI;
                    } else {
                        sumAngle += angle - formAngle;
                    }
                }
            }
            formAngle = angle;
        }
        if (sumAngle > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Point getHolePoint() {
        Point pt = Point.tempPoint(0, 0);
        //假设本Route是顺时针的，因此要找一个右拐的ApproximatePoint
        ApproximatePoint start, end;
        ListIterator<ApproximatePoint> listIterator = aprxPts.listIterator();

        start = aprxPts.getLast();
        end = listIterator.next();
        double angle, formAngle;
        formAngle = atan2(end.x - start.x, end.y - start.y);
        while (listIterator.hasNext()) {
            start = end;

            end = listIterator.next();

            angle = atan2(end.x - start.x, end.y - start.y);
            if (angle == formAngle || angle - formAngle == -2 * PI || angle - formAngle == 2 * PI || angle - formAngle == PI || angle - formAngle == -PI) {
                continue;
            }
            //线段右拐
            if ((formAngle >= 0 && !(angle > formAngle || angle < formAngle - PI)) || (formAngle < 0 && !(angle > formAngle && angle < formAngle + PI))) {
                break;
            }
            formAngle = angle;
        }
        ApproximatePoint ap = start;
        double x1, y1, x2, y2;
        x1 = (start.back.x + start.x) / 2;
        y1 = (start.back.y + start.y) / 2;
        x2 = (start.x + end.x) / 2;
        y2 = (start.y + end.y) / 2;
        start = end;
        end = end.front;
        while (end != ap) {
            //如果平行
            if (-(x2 - x1) * (end.y - start.y) + (end.x - start.x) * (y2 - y1) == 0) {
                //竖直平行共线
                if (x2 == x1 && x1 == end.x) {
                    if ((y2 - y1) * (end.y - start.y) < 0) {
                        y2 = end.y;
                    } else {
                        y2 = start.y;
                    }

                } else if (y2 == y1 && y1 == end.y) {//水平平行
                    if ((x2 - x1) * (end.x - start.x) < 0) {
                        x2 = end.x;
                    } else {
                        x2 = start.x;
                    }

                } else if ((x2 - x1) * (end.y - y1) - (y2 - y1) * (end.x - x1) == 0) {//一一般平行，是否共线
                    if ((end.x - x1) * (end.x - x1) + (end.y - y1) * (end.y - y1) > (start.x - x1) * (start.x - x1) + (start.y - y1) * (start.y - y1)) {
                        x2 = start.x;
                        y2 = start.y;
                    } else {
                        x2 = end.x;
                        y2 = end.y;
                    }

                }

            } else if (EYMath.isLineSegmentIntersect(x1, y1, x2, y2, start.x, start.y, end.x, end.y)) {
                Point2D pt2D = EYMath.linesIntersectionPoint(x1, y1, x2, y2, start.x, start.y, end.x, end.y);
                x2 = pt2D.getX();
                y2 = pt2D.getY();
            }
            start = end;
            end = end.front;
        }
        return pt = Point.tempMaxIndexPoint((x1 + x2) / 2, (y1 + y2) / 2);
    }

    @Override
    public ModelElementType type() {
        return ModelElementType.SegmentRoute;
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return routeIm;
    }

    public void addToPath(Path2D path) {
        if (segments == null || segments.isEmpty()) {
            return;
        }
        Point p = segments.getFirst().getFirstVertex();
        path.moveTo(p.x, p.y);
        for (Segment seg : segments) {
            seg.addToPath(path);
        }
        path.closePath();
    }
}
