/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Random;
import net.epsilony.math.util.EYMath;
import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class Route extends ModelElement {

    static Logger log = Logger.getLogger(Route.class);
    static ModelElementIndexManager routeIm = new ModelElementIndexManager();
    LinkedList<Segment> segments = new LinkedList<Segment>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        for (Segment segment : segments) {
            Point firstVertex = segment.getFirstVertex();
            sb.append(firstVertex.x);
            sb.append(" ");
            sb.append(firstVertex.y);
            sb.append(segment.type());
        }
        return sb.toString();
    }

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

    public void close(){
        Segment back=segments.getLast();
        for(Segment s:segments){
            back.front=s;
            s.back=back;
            back=s;
        }
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

    /**
     * 计算一条Route是否是逆时针的（右手法则z轴正向的）
     * @return true:顺时针 false:逆时针 特殊:两 条真线l12,l21组成的重合Route被视为是顺时针的
     */
    public boolean isCounterClockwise() {
        ApproximatePoint stopCond, start, end, front;
        start = aprxPts.getFirst();
        stopCond = start;
        double sumRotation = 0;

        double v1x, v1y, v2x, v2y, v1len, v2len, innerProduct, outProduct, atan, len;
        do {
            end = start.front;
            front = end.front;
            v1x = end.x - start.x;
            v1y = end.y - start.y;
            v2x = front.x - end.x;
            v2y = front.y - end.y;
            innerProduct = v1x * v2x + v1y * v2y;

            outProduct = v1x * v2y - v1y * v2x;
            if (0 == outProduct) {
                start = start.front;
                continue;
            }
            if (0 == innerProduct) {
                sumRotation += outProduct > 0 ? Math.PI / 2 : -Math.PI / 2;
            } else {
                len = Math.sqrt((v1x * v1x + v1y * v1y) * (v2x * v2x + v2y * v2y));
                sumRotation += outProduct > 0 ? Math.acos(innerProduct / len) : -Math.acos(innerProduct / len);
            }
            start = start.front;
        } while (start != stopCond);
        if (sumRotation <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public Point getHolePoint() {
        Point pt = Point.tempPoint(0, 0);
        //假设本Route是顺时针的，因此要找一个右拐的ApproximatePoint
        ApproximatePoint start, end, front, stopCond;
        double outProduct, v1x, v1y, v2x, v2y;
        start = aprxPts.get(new Random().nextInt(aprxPts.size()));
        stopCond = start;
        boolean find = false;
        //寻找一个凸顶点（end)
        do {
            end = start.front;
            front = end.front;
            v1x = end.x - start.x;
            v1y = end.y - start.y;
            v2x = front.x - end.x;
            v2y = front.y - end.y;
            outProduct = v1x * v2y - v2x * v1y;
            if (outProduct > 0) {
                find = true;
                break;
            }
            start = start.front;
        } while (start != stopCond);
        if (!find) {
            log.error("haven't find!");
            return null;
        }

        //连接找到的凸顶两边的中点，并将这个段线与整个Route求交取最靠start,end的这一侧的求交结果
        double x1, y1, x2, y2;
        x1 = (end.x + start.x) / 2;
        y1 = (end.y + start.y) / 2;
        x2 = (front.x + end.x) / 2;
        y2 = (front.y + end.y) / 2;
        v1x = x2 - x1;
        v1y = y2 - y1;
        stopCond = start;
        start = front;
        do {
            end = start.front;
            if (EYMath.isLineSegmentIntersect(x1, y1, x2, y2, start.x, start.y, end.x, end.y)) {
                outProduct = v1x * (end.y - start.y) - v1y * (end.x - start.x);
                if (outProduct != 0) {
                    Point2D cpt = EYMath.linesIntersectionPoint(x1, y1, x2, y2, start.x, start.y, end.x, end.y);
                    x2 = cpt.getX();
                    y2 = cpt.getY();
                    v1x = x2 - x1;
                    v1y = y2 - y1;
                } else {
                    if ((start.x - x1) * (start.x - x1) + (start.y - y1) * (start.y - y1) >= (end.x - x1) * (end.x - x1) + (end.y - y1) * (end.y - y1)) {
                        x2 = start.x;
                        y2 = start.y;

                    } else {
                        x2 = end.x;
                        y2 = end.y;
                    }
                    v1x = x2 - x1;
                    v1y = y2 - y1;
                }
            }

            start = start.front;
        } while (start != stopCond);
        pt.x = (x1 + x2) / 2;
        pt.y = (y1 + y2) / 2;
        return pt;

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

    public void addApproximateRouteToPath(Path2D path) {
        if (segments == null || segments.isEmpty() || aprxPts.isEmpty()) {
            return;
        }
        ApproximatePoint start, end, stop;
        start = aprxPts.getFirst();
        stop = start;
        path.moveTo(start.x, start.y);
        do {
            end = start.front;
            path.lineTo(end.x, end.y);
            start = start.front;
        } while (start != stop);
        path.closePath();
    }

    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.add(0);
        list.add(1);
        list.add(2);
        for (Integer i : list) {
            System.out.println("i=" + i);
        }
    }
}
