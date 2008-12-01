/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import static java.lang.Math.*;
import java.util.List;
import java.util.LinkedList;
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

    List<NodeBucket> buckets = new LinkedList<NodeBucket>();
    int bucketCapacity; //每一个bucket的最大容量
    double xMin, yMin, xMax, yMax;

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

    void insertNode(Node n) throws NodeOutsideManagerDomainException {
        NodeBucket origBucket = null;
        for (NodeBucket nb : buckets) {
            if (nb.isLocationInside(n)) {
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
                if (!origBucket.isLocationInside(n)) {
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
        }
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
    int getInfluenceNodes(double dis, Node key, List<Node> influenceNodes) {
        int number = 0;
        double xLMin=key.getX()-dis;
        double yLMin=key.getY()-dis;
        double xLMax=key.getX()+dis;
        double yLMax=key.getY()+dis;
        double centerX=key.getX();
        double centerY=key.getY();
        influenceNodes.clear();
        for (NodeBucket bucket:buckets) {
            if (!(bucket.getXMax()<xLMin||bucket.getXMin()>xLMax||bucket.getYMax()<yLMin||bucket.getYMin()>yLMax)) {
                for (Node bucketNode:bucket.getNodes()) {
                    if ((bucketNode.getX() - centerX) * (bucketNode.getX() - centerX) + (bucketNode.getY() - centerY) * (bucketNode.getY() - centerY) <= dis*dis) {
                        influenceNodes.add(bucketNode);
                    }
                }
            }
        }
        return influenceNodes.size();
    }

    double supportDomain(double r, double zeroDimensionValue, double crit, Node core, List<Node> supportNode) {
        double result;
        double test = 0;
        int n;
        do {
            n = getInfluenceNodes(r, core, supportNode);
            result = sqrt(PI * r * r) / (sqrt(n) - 1) * zeroDimensionValue;
            test = (result - r) / r;
            if (test >= crit) {
                r = r * (1 + crit);
            } else {
                r = r * (1 - crit);
            }
        } while (abs(test) >= crit);
        return result;
    }
}

