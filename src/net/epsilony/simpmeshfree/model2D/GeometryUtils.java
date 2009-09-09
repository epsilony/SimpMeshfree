/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import static net.epsilony.math.util.EYMath.*;

/**
 *
 * @author epsilon
 */
public class GeometryUtils {

    transient static Logger log = Logger.getLogger(GeometryUtils.class);

    /**
     * 过滤nodes，使其只含有半径为r的，以x,y为中心的闭区域内的与(x,y)之间没有曲段遮挡的结点
     * @param x
     * @param y
     * @param r
     * @param segment 如点(x,y)在边界上，则此变量为所在的Segment否则设为null
     * @param t 如segment!=null，则此为求出x,y的曲线参数值
     * @param aps 可能阻挡结点与(x,y)的曲线段上的结点
     * @param nodes 候选结点
     */
    public static void insightNodes(double x, double y, double r, Segment segment, double t, Collection<ApproximatePoint> aps, LinkedList<Node> nodes) {
        TreeSet<ApproximatePoint> apTree = new TreeSet<ApproximatePoint>(ModelElement.indexComparator);
        double outProduct;
        ApproximatePoint start, end;
        ApproximatePoint tempNodeAp = null;
        ListIterator<Node> nodeIt = nodes.listIterator();
        //去除半径r以外的结点
        while (nodeIt.hasNext()) {
            Node tnode = nodeIt.next();
            if ((tnode.x - x) * (tnode.x - x) + (tnode.y - y) * (tnode.y - y) > r * r) {
                nodeIt.remove();
            }
        }
        //如果搜索中心在一个线段上，那么新增一个临时的ApproximatePoint，使得解符合要求
        if (null != segment) {
            for (ApproximatePoint ap : aps) {

                start = ap.back;
                end = ap;

                if (start.segment == segment && start.segmentParm < t && (end.segmentParm > t || end.segmentParm == 0)) {
                    tempNodeAp = ApproximatePoint.tempApproximatePoint(x, y);
                    tempNodeAp.segment = segment;
                    tempNodeAp.back = start;
                    tempNodeAp.front = end;
                    tempNodeAp.segmentParm = t;
                    start.front = tempNodeAp;
                    end.back = tempNodeAp;
                    break;
                }
                start = ap;
                end = ap.front;
                if (start.segment == segment && start.segmentParm < t && (end.segmentParm > t || end.segmentParm == 0)) {
                    tempNodeAp = ApproximatePoint.tempApproximatePoint(x, y);
                    tempNodeAp.segment = segment;
                    tempNodeAp.back = start;
                    tempNodeAp.front = end;
                    tempNodeAp.segmentParm = t;
                    start.front = tempNodeAp;
                    end.back = tempNodeAp;
                    break;
                }
            }
            if (null == tempNodeAp) {
                log.error("tempNode==null");
            }
            aps.add(tempNodeAp);
        }

        //去除每一段相关的由ApproximatePoint连成的线段的“背面”的结点
        for (ApproximatePoint ap : aps) {
            start = ap.back;
            end = ap;
            outProduct = vectorProduct(end.x - start.x, end.y - start.y, x - start.x, y - start.y);
            if (outProduct <= 0 && !apTree.contains(start) && isLineCircleIntersects(x, y, r, start.x, start.y, end.x, end.y)) {
                removeOutsightNodes(x, y, start, end, nodes);
            }
            start = ap;
            end = ap.front;
            outProduct = vectorProduct(end.x - start.x, end.y - start.y, x - start.x, y - start.y);
            if (outProduct <= 0 && !apTree.contains(end) && isLineCircleIntersects(x, y, r, start.x, start.y, end.x, end.y)) {
                removeOutsightNodes(x, y, start, end, nodes);
            }
            apTree.add(ap);
        }

        if (null != segment) {
            aps.remove(tempNodeAp);
            tempNodeAp.back.front = tempNodeAp.front;
            tempNodeAp.front.back = tempNodeAp.back;
        }
    }

    /**
     * <br>去除从点(x,y)往去被(start)-(end)段曲线所挡住的结点</br>
     * <br>注！：这是个近似算法，如结点不在曲线段(start-end)上，且结点在真线段(start-end)后，则这个节点将被删除</br>
     * <br>也就是说，如果结点出现夹在曲线段(start-end)与直线段(start-end)中的开区域里，其将被错误删除</br>
     * <br>但是，一般来说，如果start与end的距离足够小，即{@link GeometryModel#compile(double, double) 的参数设得合适</br>
     * <br>上述悲剧不会发生</br>
     * @param x
     * @param y
     * @param start
     * @param end
     * @param nodes
     */
    private static void removeOutsightNodes(double x, double y, ApproximatePoint start, ApproximatePoint end, LinkedList<Node> nodes) {
        ListIterator<Node> nodeIt = nodes.listIterator();
        double v1x, v1y, v2x, v2y, v3x, v3y, v4x, v4y;
        v1x = end.x - start.x;
        v1y = end.y - start.y;
        v2x = start.x - x;
        v2y = start.y - y;
        v3x = end.x - x;
        v3y = end.y - y;
        double outProduct;
        while (nodeIt.hasNext()) {
            Node tNode = nodeIt.next();
            outProduct = vectorProduct(v1x, v1y, tNode.x - start.x, tNode.y - start.y);
            if (outProduct > 0) {
                v4x = tNode.x - x;
                v4y = tNode.y - y;
                if (vectorProduct(v2x, v2y, v4x, v4y) <= 0 && vectorProduct(v3x, v3y, v4x, v4y) >= 0) {//如果结点在线段(start)-(end)的背面或线段上
                    nodeIt.remove();
                }
            }
        }
    }

    public static void main(String[] args) {
        Model gm = new Model();
        gm.addShape(new Rectangle2D.Double(0, -6, 48, 6));
        gm.generateApproximatePoints(0.5, 0.1);
        LinkedList<ApproximatePoint> aprxPts = new LinkedList<ApproximatePoint>();
//        gm.approximatePointSearch(aprxPts, 0.2-0.5-10)
    }
}
