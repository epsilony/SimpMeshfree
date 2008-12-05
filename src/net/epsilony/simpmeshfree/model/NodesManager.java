/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import static java.lang.Math.*;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.epsilony.simpmeshfree.exceptions.NodeOutsideManagerDomainException;
import static java.lang.Math.PI;

/**
 * <title>Buckets算法节点管理器</title>
 * 在任何时候，矩形二维分析域&Oemga总是被Buckets所划分</br>
 * 每一个Buckets的二维空间占踞一个矩形区域&Omega<sub>B<sub>i</sub></sub>，其上的点集为：<br>
 * {(x,y)|x&isin[x<sub>mini</sub>,x<sub>maxi</sub>),y&isin[y<sub>mini</sub>,y<sub>maxi</sub>)}<br>
 * 由此可见Buckets域应比所分析的结构更大一点，以便完全“套住”结构
 * 一个Bucket也是一个存放节点的容器，每个Bucket有个存放结点的上限bucketCapacity,<br>
 * 新增节点会造成一些Bucket的分裂增殖以适应bucketCapacity
 * @author M.Yuan J.-J.Chen
 */
public class NodesManager {

    public LinkedList<NodeBucket> buckets = new LinkedList<NodeBucket>();
    int bucketCapacity; //每一个bucket的最大容量
    double xMin, yMin, xMax, yMax;
    public LinkedList<Node> nodes = new LinkedList<Node>();
    public class Status{
        LinkedList<Node> outErrSupportNodes=new LinkedList<Node>();
        LinkedList<Double> outErrSupportNodesMinErr=new LinkedList<Double>();
        
        public void initialize(){
            outErrSupportNodes.clear();
            outErrSupportNodesMinErr.clear();
            
        }
    }
    
    Status status=new Status();

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
        for (NodeBucket nb : buckets) {
            if (nb.isLocateIn(n)) {
                origBucket = nb;
                break;
            }
        }

        if (null == origBucket) {
            throw new NodeOutsideManagerDomainException(n, this);
        }

        if (origBucket.nodesSize() >= getBucketCapacity()) {
            LinkedList<NodeBucket> fissionBuckets = new LinkedList<NodeBucket>();
            buckets.remove(origBucket);
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
                    buckets.addAll(fissionBuckets);
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
     * 获取以节点key为中心，dis为半径的圆形区域内的所有点，并将这些点存放在influenceNode List表内
     * @param dis
     * @param key
     * @param influenceNodes 初始化后的一个List
     * @return 搜出的节点数目
     */
    public int getNodesInCircel(double radiu, double x, double y, List<Node> influenceNodes) {

        influenceNodes.clear();
        for (NodeBucket bucket : buckets) {
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

    public double getSupportNodes(double x, double y, List<Node> supportNodes) {
        supportNodes.clear();
        double maxRadiu = 0;
        for (NodeBucket bucket : buckets) {
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

    public void statusReport(String fileName) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            out.println("--------------------------------------------------------------");
            out.println("**************************************************************");
            out.println(Calendar.getInstance().getTime());
            out.println("buckets.size=" + buckets.size());
            int i = 0;
            for (NodeBucket b : buckets) {
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
            out.printf("%20s%20s%20s%20s%20s%20s","Index","x","y","radiu","err","type");
            for(i=0;i<status.outErrSupportNodes.size();i++){
                Node node= status.outErrSupportNodes.get(i);
                out.printf("%20d%20e%20e%20e%20e%20s%n",node.getIndex(),node.getX(),node.getY(),node.getInfRadius(),status.outErrSupportNodesMinErr.get(i),node.getType());                
            }
        } catch (IOException ex) {
            Logger.getLogger(NodesManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    public void generateNodesInfluence(double alphai, double maxErr,int maxIters) {
        double dc = sqrt((yMax - yMin) * (xMax - yMin)) / (sqrt(nodes.size()) - 1);
        double radiu = dc * alphai / 2;
        double minErrRadiu=0;
        double result;
        double err = 0;
        double minErr=0;
        int n;
        int itersCount=0;
        double x, y;

        for (Node node : nodes) {

            x = node.getX();
            y = node.getY();


            //计算结点Node的影响域尺寸
            itersCount=0;
            do {
                n = 0;
                minErr=0;
                for (NodeBucket bucket : buckets) {
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
                
                if(minErr==0||err<minErr){
                    minErr=err;
                    minErrRadiu=radiu;
                }
                if (abs(err) >= abs(maxErr)) {
                    if(itersCount>=maxIters){
                        radiu=minErrRadiu;
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
            for (NodeBucket bucket : buckets) {
                if (bucket.isSqureIntersected(x, y, radiu)) {
                    bucket.addSupportNodes(node);
                }
            }
        }
    }
}

