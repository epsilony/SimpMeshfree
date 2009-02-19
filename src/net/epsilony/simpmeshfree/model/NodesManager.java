/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.epsilony.simpmeshfree.exceptions.NodeOutsideManagerDomainException;
import static java.lang.Math.*;

/**
 * <title>Buckets算法节点管理器</title>
 * 在任何时候，矩形二维分析域&Oemga总是被Buckets所划分</br>
 * 每一个Buckets的二维空间占踞一个矩形区域&Omega<sub>B<sub>i</sub></sub>，其上的点集为：<br>
 * {(x,y)|x&isin[x<sub>mini</sub>,x<sub>maxi</sub>),y&isin[y<sub>mini</sub>,y<sub>maxi</sub>)}<br>
 * 由此可见Buckets域应比所分析的结构更大一点，以便完全“套住”结构
 * 一个Bucket也是一个存放节点的容器，每个Bucket有个存放结点的上限bucketCapacity,<br>
 * 新增节点会造成一些Bucket的分裂增殖以适应bucketCapacity
 * <p> <Bold> changelist: </Bold>
 * <br> 0.10 初建 </br>
 * <br> 0.11 通过RPIMTest </br>
 * <br> 0.20 通过CantilevelTest </br>
 * @author M.Yuan J.-J.Chen
 * @version 0.20
 */
public class NodesManager {

    private LinkedList<NodeBucket> buckets = new LinkedList<NodeBucket>(); //存储所有NodeBucket的链表
    int bucketCapacity; //每一个bucket的最大容量
    double xMin, yMin, xMax, yMax; //NodesManager 的管理范围，初始的那个Bucket与其等同。
    public LinkedList<Node> nodes = new LinkedList<Node>();
    boolean nodesChanged = false;
    boolean bucketsChanged = false;
    Shape nodesShape = null;
    Shape bucketsShape = null;

    /**
     * @return the buckets
     */
    public LinkedList<NodeBucket> getBuckets() {
        return buckets;
    }

    /**
     * 用以调式记录运行结果的类
     */
    public class Status {

        LinkedList<Node> outErrSupportNodes = new LinkedList<Node>();
        LinkedList<Double> outErrSupportNodesMinErr = new LinkedList<Double>();

        public void initialize() {
            outErrSupportNodes.clear();
            outErrSupportNodesMinErr.clear();

        }
    }
    Status status = new Status();

    public double getXMax() {
        return xMax;
    }

    public double getXMin() {
        return xMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getYMin() {
        return yMin;
    }

    /**
     * @param xMin
     * @param yMin
     * @param xMax
     * @param yMax
     * @param bucketCapacity 每个bucket最多允许链接的节点数
     */
    public NodesManager(double xMin, double yMin, double xMax, double yMax, int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        buckets.add(new NodeBucket(xMin, yMin, xMax, yMax));

    }

    public void insertNode(Node n) throws NodeOutsideManagerDomainException {
        NodeBucket origBucket = null;
        for (NodeBucket nb : getBuckets()) {
            if (nb.isLocateIn(n)) {
                origBucket = nb;
                break;
            }
        }

        if (null == origBucket) {
            throw new NodeOutsideManagerDomainException(n, this);
        }

        nodesChanged = true;
        if (origBucket.nodesSize() >= getBucketCapacity()) {
            bucketsChanged = true;
            LinkedList<NodeBucket> fissionBuckets = new LinkedList<NodeBucket>();
            getBuckets().remove(origBucket);
            fissionBuckets.add(origBucket);
            fissionBuckets.add(origBucket.fission());
            int i = 0;
            while (i < fissionBuckets.size()) {
                origBucket = fissionBuckets.get(i);
                if (!origBucket.isLocateIn(n)) {
                    i++;
                    continue;
                }
                if (origBucket.nodesSize() >= getBucketCapacity()) {
                    fissionBuckets.add(origBucket.fission());
                    continue;
                } else {
                    origBucket.addNode(n);
                    getBuckets().addAll(fissionBuckets);
                    break;
                }
            }
        } else {
            origBucket.addNode(n);
        }
        nodes.add(n);
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    /**
     * 获取以节点(x,y)为中心，dis为半径的圆形区域内的所有点，并将这些点存放在influenceNode List表内
     * @param dis
     * @param key
     * @param influenceNodes 初始化后的一个List
     * @return 搜出的节点数目
     */
    public int getNodesInCircel(double radiu, double x, double y, List<Node> influenceNodes) {

        influenceNodes.clear();
        for (NodeBucket bucket : getBuckets()) {
            if (bucket.isSqureIntersected(x, y, radiu)) {
                for (Node bucketNode : bucket.getNodes()) {
                    if (bucketNode.isInDistance(x, y, radiu)) {
                        influenceNodes.add(bucketNode);
                    }
                }
            }
        }
        return influenceNodes.size();
    }

    /**
     * <br>获取一个坐标的支持域内的所有节点。在模型中修改节点（增、删、移）之后，必须先运行</br>
     * <br>{@link NodesManager#generateNodesInfluence(double, double, int) generateNodesInfluence}</br>
     * <br>使得NodesManager中的所有节点的影响域半径以及每个NodeBucket的可能支持节点都被正确的设置</br>
     * @param x
     * @param y
     * @param supportNodes 用以存储支持节点的List，非null。
     * @return 节点的平均间距 d<sub>c</sub>
     * @see NodesManager#generateNodesInfluence(double, double, int) 
     */
    public double getSupportNodes(double x, double y, List<Node> supportNodes) {
        supportNodes.clear();
        double maxRadiu = 0;
        for (NodeBucket bucket : getBuckets()) {
            if (bucket.isLocateIn(x, y)) {
                for (Node node : bucket.getSupportNodes()) {
                    if (node.isInfluenced(x, y)) {
                        supportNodes.add(node);
                        if (maxRadiu < node.getInfRadius()) {
                            maxRadiu = node.getInfRadius();
                        }
                    }
                }
            }
        }
        return maxRadiu * 2 / (sqrt(supportNodes.size() * 4 / PI) - 1);
    }

    /**
     * 输出NodesManager的一些信息
     * @param fileName 输出的文件名
     */
    public void statusReport(String fileName) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            out.println("--------------------------------------------------------------");
            out.println("**************************************************************");
            out.println(Calendar.getInstance().getTime());
            out.println("buckets.size=" + getBuckets().size());
            int i = 0;
            for (NodeBucket b : getBuckets()) {
                out.println("bucket" + i + ":");
                out.printf("nodes.size = %d, xmin=%g, ymin = %g, xmax = %g, ymax = %g%n", b.nodes.size(), b.getXMin(), b.getYMin(), b.getXMax(), b.getYMax());
                for (Node n : b.nodes) {
                    out.printf("Node index:%d, x = %g, y = %g, type = %b%n", n.getIndex(), n.getX(), n.getY(), n.isEssential());
                }
                i++;
            }
            out.println("*******************************************************************");
            out.println("*******************************************************************");
            out.println("*******************************************************************");
            out.println("out iterate max time nodes during searching influence radiu ");
            out.printf("%20s%20s%20s%20s%20s%20s", "Index", "x", "y", "radiu", "err", "type");
            for (i = 0; i < status.outErrSupportNodes.size(); i++) {
                Node node = status.outErrSupportNodes.get(i);
                out.printf("%20d%20e%20e%20e%20e%20s%n", node.getIndex(), node.getX(), node.getY(), node.getInfRadius(), status.outErrSupportNodesMinErr.get(i), node.getType());
            }
        } catch (IOException ex) {
            Logger.getLogger(NodesManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    /**
     * 计算并设置所有Node的影响域半径以及每个NodeBunket的可能支持节点
     * @param alphai<br> &alpha;<sub>i</sub> 影响域的节点重数，影响域的直径d<sub>i</sub>=&alpha;<sub>i</sub>d<sub>c</sub></br> <br> d<sub>c</sub>为节点附近的节点平均距离</br>
     * @param maxErr 允许的节点影响域半径的最大误差
     * @param maxIters 计算节点影响域半径的迭代次数，迭代超过maxIters则选择误差最小的那个解，同时将结点和误差记录在{@link Status NodesManager.Status} 相关变量中。
     */
    public void generateNodesInfluence(double alphai, double maxErr, int maxIters) {
        double dc = sqrt((yMax - yMin) * (xMax - yMin)) / (sqrt(nodes.size()) - 1);
        double radiu = dc * alphai / 2;
        double minErrRadiu = 0;
        double result;
        double err = 0;
        double minErr = 0;
        int n;
        int itersCount = 0;
        double x, y;

        for (Node node : nodes) {

            x = node.getX();
            y = node.getY();


            //计算结点Node的影响域尺寸
            itersCount = 0;
            do {
                n = 0;
                minErr = 0;
                for (NodeBucket bucket : getBuckets()) {
                    if (bucket.isSqureIntersected(x, y, radiu)) {
                        for (Node bucketNode : bucket.getNodes()) {
                            if (bucketNode.isInDistance(x, y, radiu)) {
                                n++;
                            }
                        }
                    }
                }

                //radiu显然过小的情况的处理
                if (round(alphai) >= n) {
                    radiu = radiu * 2;
                    continue;
                }
                result = 2 * radiu / (sqrt(4 * n / PI) - 1) * alphai / 2;//<=is for circle support domain,and for sqare support domain:sqrt(PI * radiu * radiu) / (sqrt(n) - 1) * alphai / 2;
                err = (result - radiu) / radiu;

                if (minErr == 0 || err < minErr) {
                    minErr = err;
                    minErrRadiu = radiu;
                }
                if (abs(err) >= abs(maxErr)) {
                    if (itersCount >= maxIters) {
                        radiu = minErrRadiu;
                        status.outErrSupportNodes.add(node);
                        status.outErrSupportNodesMinErr.add(new Double(radiu));
                        break;
                    }
                    radiu = result * 0.618 + radiu * 0.382;
                    itersCount++;
                    continue;
                } else {
                    break;
                }
            } while (true);

            //设置结点的影响域半径并将其加入到可能影响到的NodeBucket的supportNodes链表中
            node.setInfRadius(radiu);
            for (NodeBucket bucket : getBuckets()) {
                if (bucket.isSqureIntersected(x, y, radiu)) {
                    bucket.addSupportNodes(node);
                }
            }
        }
    }

    /**
     * 获取所有NodeBucket的几何外形,与{@link NodeBucket#getShape()} 不同的是,试图将重叠段去掉但是失败了
     * @return 可用于java2D
     */
    public Shape getBucketsShape() {

        if (!bucketsChanged && bucketsShape != null) {
            return bucketsShape;
        }
        LinkedList<double[]> vLines = new LinkedList<double[]>();
        LinkedList<double[]> hLines = new LinkedList<double[]>();
        double x1, y1, x2, y2;
        double[] tds;

        Iterator ite = getBuckets().iterator();
        if (!ite.hasNext()) {
            return null;
        }

        NodeBucket nb = (NodeBucket) ite.next();

        x1 = nb.getXMin();
        y1 = nb.getYMin();
        x2 = nb.getXMax();
        y2 = nb.getYMax();

        vLines.add(new double[]{x1, y1, y2});
        vLines.add(new double[]{x2, y1, y2});
        hLines.add(new double[]{y1, x1, x2});
        hLines.add(new double[]{y2, x1, x2});

        ListIterator vIte, hIte;
        boolean tb;

        //getting the lines' pos
        while (ite.hasNext()) {
            nb = (NodeBucket) ite.next();
            x1 = nb.getXMin();
            y1 = nb.getYMin();
            x2 = nb.getXMax();
            y2 = nb.getYMax();
            vIte = vLines.listIterator();
            hIte = hLines.listIterator();

            //add verticle lines
            tb = false;
            while (vIte.hasNext()) {
                tds = (double[]) vIte.next();
                if (x1 < tds[0]) {
                    if (vIte.hasPrevious()) {
                        vIte.previous();
                        vIte.add(new double[]{x1, y1, y2});
                        tb = true;
                    } else {
                        vLines.add(0, new double[]{x1, y1, y2});
                        tb = true;
                    }
                    break;
                } else {
                    if (x1 == tds[0]) {
                        if ((y1 - tds[1]) * (y1 - tds[2]) <= 0 || (y2 - tds[1]) * (y2 - tds[2]) <= 0) {
                            tds[1] = min(y1, tds[1]);
                            tds[2] = max(y2, tds[2]);
                            tb = true;
                            break;
                        }
                    }
                }
            }

            if (!tb) {
                vIte.add(new double[]{x1, y1, y2});
            }

            vIte.previous();
            tb = false;
            while (vIte.hasNext()) {
                tds = (double[]) vIte.next();
                if (x2 < tds[0]) {
                    vIte.previous();
                    vIte.add(new double[]{x2, y1, y2});
                    tb = true;
                    break;
                } else {
                    if (x2 == tds[0]) {
                        if ((y1 - tds[1]) * (y1 - tds[2]) <= 0 || (y2 - tds[1]) * (y2 - tds[2]) <= 0) {
                            tds[1] = min(y1, tds[1]);
                            tds[2] = max(y2, tds[2]);
                            tb = true;
                            break;
                        }
                    }
                }
            }
            if (!tb) {
                vIte.add(new double[]{x2, y1, y2});
            }


            //add horizontal lines
            tb = false;
            while (hIte.hasNext()) {
                tds = (double[]) hIte.next();
                if (y1 < tds[0]) {
                    if (hIte.hasPrevious()) {
                        hIte.previous();
                        hIte.add(new double[]{y1, x1, x2});
                        tb = true;
                    } else {
                        hLines.add(0, new double[]{y1, x1, x2});
                        tb = true;
                    }
                    break;
                } else {
                    if (y1 == tds[0]) {
                        if ((x1 - tds[1]) * (x1 - tds[2]) <= 0 || (x2 - tds[1]) * (x2 - tds[2]) <= 0) {
                            tds[1] = min(x1, tds[1]);
                            tds[2] = max(x2, tds[2]);
                            tb = true;
                            break;
                        }
                    }
                }
            }
            if (!tb) {
                hIte.add(new double[]{y1, x1, x2});
            }

            tb = false;
            hIte.previous();
            while (hIte.hasNext()) {
                tds = (double[]) hIte.next();
                if (y2 < tds[0]) {
                    hIte.previous();
                    hIte.add(new double[]{y2, x1, x2});
                    tb = true;
                    break;
                } else {
                    if (y2 == tds[0]) {
                        if ((x1 - tds[1]) * (x1 - tds[2]) <= 0 || (x2 - tds[1]) * (x2 - tds[2]) <= 0) {
                            tds[1] = min(x1, tds[1]);
                            tds[2] = max(x2, tds[2]);
                            tb = true;
                            break;
                        }
                    }
                }
            }
            if (!tb) {
                hIte.add(new double[]{y2, x1, x2});
            }
        }
        //end of getting the lines pos

        //create shape
        Path2D path = new Path2D.Double();
        for (double[] ds : vLines) {
            path.moveTo(ds[0], ds[1]);
            path.lineTo(ds[0], ds[2]);
        }
        for (double[] ds : hLines) {
            path.moveTo(ds[1], ds[0]);
            path.lineTo(ds[2], ds[0]);
        }
        bucketsChanged = false;
        return bucketsShape = path.createTransformedShape(new AffineTransform());
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public Shape getNodesShape() {
        if (!nodesChanged && nodesShape != null) {
            return nodesShape;
        }
        Path2D path = new Path2D.Double();
        Path2D nodePath = new Path2D.Double(new Line2D.Double(0, 0, 0, 0));

        for (Node n : getNodes()) {
            path.append(nodePath.getPathIterator(AffineTransform.getTranslateInstance(n.getX(), n.getY())), false);
        }
        nodesChanged = false;
        return nodesShape = path.createTransformedShape(new AffineTransform());
    }
}

