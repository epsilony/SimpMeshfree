/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import gnu.trove.list.array.TIntArrayList;
import java.util.*;
import net.epsilony.simpmeshfree.utils.Avatarable;
import net.epsilony.utils.CenterDistanceSearcher;
import net.epsilony.utils.LayeredRangeTree;
import net.epsilony.utils.geom.CenterDistanceComparator;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Coordinates;
import net.epsilony.utils.geom.GeometryMath;
import static net.epsilony.utils.geom.GeometryMath.*;

/**
 *
 * @author epsilon
 */
public class GeomUtils implements Avatarable<GeomUtils> {

    ArrayList<Boundary> boundaries;
    public ArrayList<Node> allNodes;
    ArrayList<Node> spaceNodes;
    ArrayList<Coordinate> bndNormals;
    int[] bndStatusCache;
    int[] nodeStatusCache;
    int bndNodeNum;
    public static final int MAX_BOUNDARY_NEIGHBORS = 3;
    public int defaultMaxNodeNumInSupportDomain = 50;
    String indexingSetting = "default";
    int dim = 3;
    ArrayList<Node> bndCenters;
    double[] bndRadius;
    double maxBndRad;
    Coordinate publicNormal = null;   //Only for 2D lines;
    CenterDistanceSearcher<Coordinate, Boundary> boundarySearcher;
    CenterDistanceSearcher<Coordinate, Node> spaceNodeSearcher;
    CenterDistanceSearcher<Coordinate, Node> nodeSearcher;
    CenterDistanceSearcher<Coordinate, Node> boundaryNodeSearcher;
    TIntArrayList intCache1 = new TIntArrayList(defaultMaxNodeNumInSupportDomain);
    TIntArrayList intCache2 = new TIntArrayList(defaultMaxNodeNumInSupportDomain);
    double initSearchRadius;

    void fillNodeStatusCache(Collection<Node> nodes, int value) {
        if (null == nodes) {
            Arrays.fill(nodeStatusCache, value);
            return;
        }
        for (Node nd : nodes) {
            nodeStatusCache[nd.id] = value;
        }
    }

    void fillBndStatusCache(Collection<Boundary> bnds, int value) {
        if (null == bnds) {
            Arrays.fill(bndStatusCache, value);
        }
        for (Boundary bnd : bnds) {
            bndStatusCache[bnd.getId()] = value;
        }
    }

    /**
     * Searches the boundaries which has common points with a shpere.
     *
     * @param center sphere center
     * @param radius
     * @param results
     * @return results
     */
    public List<Boundary> searchBoundary(Coordinate center, double radius, List<Boundary> results) {
        return boundarySearcher.search(center, radius, results);
    }

    public List<Node> searchSpaceNodes(Coordinate center, double radius, List<Node> results) {
        return spaceNodeSearcher.search(center, radius, results);
    }

    public List<Node> searchNodes(Coordinate center, double radius, List<Node> results) {
        return nodeSearcher.search(center, radius, results);
    }

    /**
     * Gets the nodes at the border of
     * <code>bnds</code>.</br>
     * <code>bnds</code> can tiles out several pieces of shells with closed
     * borders. Even only one {@link LineBoundary line} has a border with two
     * nodesTree.
     *
     * @param bnds
     * @return {@link Node nodesTree} on borders without duplication. The sort
     * of these nodesTree is not definate.
     */
    public LinkedList<Node> getBorderNodes(Collection<Boundary> bnds) {

        LinkedList<Node> result = new LinkedList<>();


        fillBndStatusCache(bnds, 1);
        int ndNum = 0;
        for (Boundary bnd : bnds) {
            for (int i = 0; i < bnd.num(); i++) {
                if (bndStatusCache[bnd.getId()] != 1) {
                    Node n1 = bnd.getNode(i);
                    Node n2 = bnd.getNode((i + 1) % bnd.num());
                    nodeStatusCache[n1.id] = n2.id;
                    if (result.isEmpty()) {
                        result.add(n1);
                    }
                    ndNum++;
                }
            }
        }
        if (result.isEmpty()) {
            return result;
        }
        Node start = result.getLast();
        Node nd = start;
        Node ndNext;
        do {
            ndNext = allNodes.get(nodeStatusCache[nd.id]);
            result.add(ndNext);
            nd = ndNext;
        } while (nd != start);
        if (ndNum != result.size()) {
            throw new IllegalStateException();
        }
        return result;

    }

    /**
     * Established for {@link Avatarable} implementation.
     */
    private GeomUtils() {
    }

    public GeomUtils(List<? extends Boundary> bounds, List<Node> spacesNodes, int dim) {
        this.boundaries = new ArrayList<>(bounds);
        this.spaceNodes = new ArrayList<>(spacesNodes);
        this.dim = dim;
        indexingNodes();
        indexingBoundaries();

    }

    @Override
    public GeomUtils avatorInstance() {
        GeomUtils avator = new GeomUtils();

        avator.bndNodeNum = bndNodeNum;
        avator.allNodes = allNodes;
        avator.bndCenters = bndCenters;
        avator.bndNormals = bndNormals;
        avator.bndRadius = bndRadius;
        avator.bndStatusCache = new int[bndStatusCache.length];
        avator.fillBndStatusCache(null, -1);
        avator.boundaries = boundaries;
        avator.boundaryNodeSearcher = boundaryNodeSearcher;
        avator.boundarySearcher = boundarySearcher;
        avator.defaultMaxNodeNumInSupportDomain = defaultMaxNodeNumInSupportDomain;
        avator.dim = dim;
        
        avator.indexingSetting = indexingSetting;
        //intCache1,intCache2 will initiated by themselves

        avator.maxBndRad = maxBndRad;
        avator.nodeSearcher = nodeSearcher;
        avator.nodeStatusCache = new int[nodeStatusCache.length];
        avator.fillNodeStatusCache(null, -1);
        avator.publicNormal = publicNormal;
        avator.spaceNodeSearcher = spaceNodeSearcher;
        avator.spaceNodes = spaceNodes;

        return avator;
    }


    public static class LayeredRangeTreeNodeSearcher implements CenterDistanceSearcher<Coordinate, Node> {

        int dim;
        LayeredRangeTree<Node> nodesTree;

        public LayeredRangeTreeNodeSearcher(int dim, List<Node> nodes) {
            this.dim = dim;
            nodesTree = new LayeredRangeTree<>(nodes, Coordinates.genComparators(dim, new Node()));
        }

        @Override
        public List<Node> search(Coordinate center, double radius, List<Node> results) {
            Node from = new Node();
            Node to = new Node();
            for (int i = 0; i < dim; i++) {
                double centerDim = center.getDim(i);
                from.setDim(i, centerDim - radius);
                to.setDim(i, centerDim + radius);
            }
            LinkedList<Node> tlist = new LinkedList<>();
            nodesTree.search(tlist, from, to);
            if (null == results) {
                results = new LinkedList<>();
            } else {
                results.clear();
            }
            double radiusSq = radius * radius;
            for (Node nd : tlist) {
                double distSq = distanceSquare(center, nd);
                if (distSq > radiusSq) {
                    continue;
                } else {
                    results.add(nd);
                }
            }
            return results;
        }
    }

    class LayeredRangeTreeBoundarySearcher implements CenterDistanceSearcher<Coordinate, Boundary> {

        LayeredRangeTree<Node> boundaryTree;

        @Override
        public List<Boundary> search(Coordinate center, double radius, List<Boundary> results) {
            Node from = new Node();
            Node to = new Node();
            for (int i = 0; i < dim; i++) {
                double centerDim = center.getDim(i);
                from.setDim(i, centerDim - radius - maxBndRad);
                to.setDim(i, centerDim + radius + maxBndRad);
            }
            LinkedList<Node> centerResults = new LinkedList<>();
            boundaryTree.search(centerResults, from, to);
            if (null == results) {
                results = new LinkedList<>();
            } else {
                results.clear();
            }
            for (Node bndCenter : centerResults) {
                int centerId = bndCenter.id;
                Boundary bnd = boundaries.get(centerId);
                double realDist = distance(bndCenter, center);
                if (realDist - bndRadius[centerId] <= radius && BoundaryUtils.isBoundarySphereIntersect(bnd, center, radius)) {
                    results.add(bnd);
                }
            }
            return results;
        }

        public LayeredRangeTreeBoundarySearcher() {
            boundaryTree = new LayeredRangeTree<>(bndCenters, Coordinates.genComparators(dim, new Node()));
        }
    }

    private void indexingNodes() {
        for (Boundary bnd : boundaries) {
            for (int i = 0; i < bnd.num(); i++) {
                Node node = bnd.getNode(i);
                node.id = -1;
            }
        }
        LinkedList<Node> tNodes = new LinkedList<>();
        for (Boundary bnd : boundaries) {
            for (int i = 0; i < bnd.num(); i++) {
                Node node = bnd.getNode(i);
                if (node.id == -1) {
                    node.id = tNodes.size();
                    tNodes.add(node);
                }
            }
        }
        bndNodeNum = tNodes.size();

        for (int i = 0; i < spaceNodes.size(); i++) {
            Node node = spaceNodes.get(i);
            node.id = tNodes.size();
            tNodes.add(node);
        }
        allNodes = new ArrayList<>(tNodes);
        nodeStatusCache = new int[tNodes.size()];
        Arrays.fill(nodeStatusCache, -1);

        spaceNodeSearcher = new LayeredRangeTreeNodeSearcher(dim, spaceNodes);
        nodeSearcher = new LayeredRangeTreeNodeSearcher(dim, allNodes);

    }

    private void indexingBoundaries() {
        bndStatusCache = new int[boundaries.size()];
        Arrays.fill(nodeStatusCache, -1);
        bndCenters = new ArrayList<>(boundaries.size());
        bndRadius = new double[boundaries.size()];
        bndNormals = new ArrayList<>(boundaries.size());
        double maxR = 0;
        for (Boundary bnd : boundaries) {
            int i = bndCenters.size();
            bnd.setId(i);
            Node center = new Node();
            center.id = i;
            double r = bnd.circum(center);
            bndCenters.add(center);
            bndRadius[i] = r;
            if (maxR < r) {
                maxR = r;
            }

            Coordinate normal = new Coordinate();
            if (dim == 2) {
                BoundaryUtils.outNormal((LineBoundary) bnd, publicNormal, normal);
            } else {
                BoundaryUtils.outNormal((TriangleBoundary) bnd, normal);
            }
            bndNormals.add(normal);
        }
        maxBndRad = maxR;
        switch (indexingSetting.toLowerCase()) {
            case "layered3D":
            case "default":
            default:
                boundarySearcher = new LayeredRangeTreeBoundarySearcher();
        }
    }

    /**
     * Given a set of boundaries (bnds), divide the set into sub sets (output)
     * by neighborhoods that means: the Union of output is bnds, for any j
     * output.get(j).get(neighbor)(any) is not in output.get(any but not j).
     *
     * @param bnds input bnds
     * @param output
     * @param bndToOutputIds
     */
    void divideContinueSets(ArrayList<Boundary> bnds, ArrayList<LinkedList<Boundary>> output, TIntArrayList bndToOutputIds) {
        //At here the this.boundaryStatusCache should abey these rules:
        //TRUE: boundaryStatusCache.num()>=max(bnd.num for bnd in bnds)
        //TRUE: id == -1 for id in boundaryStatusCache 

        // boundaryStatusCache.get(bnd.getId())==-1 means bnd is not in bnds 
        output.clear();
        output.ensureCapacity(bnds.size());
        bndToOutputIds.ensureCapacity(bnds.size());

        TIntArrayList actNeighborSetIds = new TIntArrayList(MAX_BOUNDARY_NEIGHBORS);
        for (Boundary bnd : bnds) {
            actNeighborSetIds.reset();
            for (int i = 0; i < bnd.num(); i++) {
                Boundary neighbor = bnd.getNeighbor(i);
                if (null == neighbor) {
                    continue;
                }
                int setIdOfNeighbor = bndStatusCache[neighbor.getId()];
                if (setIdOfNeighbor != -1) {
                    actNeighborSetIds.add(setIdOfNeighbor);
                }
            }
            if (actNeighborSetIds.isEmpty()) {
                int newSetId = output.size();
                LinkedList<Boundary> newSetList = new LinkedList<>();
                newSetList.add(bnd);
                output.add(newSetList);
                bndStatusCache[bnd.getId()] = newSetId;
            } else {
                int mergyAimId = actNeighborSetIds.get(0);
                LinkedList<Boundary> mergyAimList = output.get(mergyAimId);
                mergyAimList.add(bnd);
                bndStatusCache[bnd.getId()] = mergyAimId;

                for (int i = 1; i < actNeighborSetIds.size(); i++) {
                    int toBeMergiedId = actNeighborSetIds.get(i);
                    if (toBeMergiedId == mergyAimId) {
                        continue;
                    }

                    LinkedList<Boundary> toBeMergiedList = output.get(toBeMergiedId);

                    if (toBeMergiedList.size() > mergyAimList.size()) {
                        LinkedList<Boundary> t = mergyAimList;
                        mergyAimList = toBeMergiedList;
                        toBeMergiedList = t;
                        int ti = mergyAimId;
                        mergyAimId = toBeMergiedId;
                        toBeMergiedId = ti;
                    }

                    for (Boundary tBnds : toBeMergiedList) {
                        bndStatusCache[tBnds.getId()] = mergyAimId;
                    }
                    mergyAimList.addAll(toBeMergiedList);
                    output.set(toBeMergiedId, null);
                }
            }
        }

        //the output is not compact, with some null in it, remove the null items and reset the bnd set ids.
        int realId = 0;
        //borrow the memory of bndToOutputIds temporarily.
        TIntArrayList oldIdToRealId = bndToOutputIds;

        oldIdToRealId.reset();
        LinkedList<LinkedList<Boundary>> tList = new LinkedList<>();
        for (int i = 0; i < output.size(); i++) {
            oldIdToRealId.add(realId);
            LinkedList<Boundary> get_i = output.get(i);
            if (get_i != null) {
                tList.add(get_i);
                realId++;
            }
        }
        output.clear();
        output.addAll(tList);

        for (Boundary bnd : bnds) {
            int oriSetId = bndStatusCache[bnd.getId()];
            bndStatusCache[bnd.getId()] = oldIdToRealId.getQuick(oriSetId);
        }

        bndToOutputIds.reset();
        for (Boundary bnd : bnds) {
            int bndSetId = bndStatusCache[bnd.getId()];
            bndToOutputIds.add(bndSetId);
            bndStatusCache[bnd.getId()] = -1;
        }
    }

    /**
     *
     * @param bnds
     * @param pt
     * @param results
     * @return
     */
    public List<Boundary> filterOutsideBnds(List<Boundary> bnds, Coordinate pt, List<Boundary> results) {
        if (null == results) {
            results = new LinkedList<>();
        } else {
            results.clear();
        }

        Coordinate tc = new Coordinate();
        for (Boundary bnd : bnds) {
            if (isPtInSideBnd(bnd, pt, tc)) {
                results.add(bnd);
            }
        }

        return results;
    }

    /**
     * Determines whether pt is inside of bnd
     *
     * @param bnd
     * @param pt
     * @param tCache for accelarating cache, can be null
     * @return
     */
    public boolean isPtInSideBnd(Boundary bnd, Coordinate pt, Coordinate tCache) {
        if (null == tCache) {
            tCache = new Coordinate();
        }
        int id = bnd.getId();
        Coordinate normal = bndNormals.get(id);
        Node minDisNode = BoundaryUtils.nearestBoundaryNode(pt, bnd);
        double d = dot(minus(pt, minDisNode, tCache), normal);
        if (d < 0) {
            return true;
        } else {
            return false;
        }
    }

    public void compVisibleFilter(Coordinate center, ArrayList<Node> spaceNds, ArrayList<Boundary> bnds, TIntArrayList nodeBlockNums, TIntArrayList nodeBlockBndIdx, ArrayList<Node> outNds) {
        List<Node> bndNds = getBndsNodes(bnds, null);
        int nodeSize = bndNds.size() + spaceNds.size();
        if (outNds == null) {
            outNds = new ArrayList<>(nodeSize);
        } else {
            outNds.clear();
            outNds.ensureCapacity(nodeSize);
        }


        nodeBlockNums.reset();
        nodeBlockNums.ensureCapacity(nodeSize);
        nodeBlockBndIdx.reset();
        nodeBlockBndIdx.ensureCapacity(nodeSize);

        intCache1.reset();
        intCache1.ensureCapacity(bndNds.size());
        intCache2.reset();
        intCache2.ensureCapacity(bndNds.size());

        visibleStatus(center, bndNds, true, bnds, nodeBlockNums, nodeBlockBndIdx);
        visibleStatus(center, spaceNds, false, bnds, intCache1, intCache2);

        outNds.addAll(bndNds);
        outNds.addAll(spaceNds);
        nodeBlockNums.addAll(intCache1);
        nodeBlockBndIdx.addAll(intCache2);
    }

    /**
     * Determines the visiability of
     * <code>nds</code> from
     * <code>center</code>. A boundary in
     * <code>bnds</code> may blocks the visiability betwean
     * <code>center</code> and
     * <code>nds</code> A boundary node will not blocked by the boundaries that
     * the node belongs to.
     *
     * @param center
     * @param nds should all be space nodes or should all be boundary nodes
     * @param bnds
     * @param nodeBlockNums There is
     * <code>nodeBlockNums.get(i)</code> boundaries blocked betwean
     * <code>nds.get(i)</code> and
     * <code>center</code>. For acceleration, if the
     * <code>nds.get(i)</code> is blocked by twice, it won't be considered
     * later. So that the max item in this will not > 2.
     * @param nodeBlockBndIdx The boundary which
     * <code>getId</code> is
     * <code>nodeBlockBndIdx</code> blocks betwean
     * <code>center</code> and
     * <code>nds.get(i)</code>. </br> If there are not only one boundary blocks
     * betwean
     * <code>center</code> and
     * <code>nds.get(i)</code> , that is
     * <code>nodeBlockNums</code> >=2 , only one of the blocking boundary's id
     * will be randomly recorded here.
     * @param isBoundaryNode is the
     * <code>nds</code> are all boundary nodes or are all not
     */
    public void visibleStatus(Coordinate center, List<Node> nds, boolean isBoundaryNode, List<Boundary> bnds, TIntArrayList nodeBlockNums, TIntArrayList nodeBlockBndIdx) {
        Coordinate t = new Coordinate();
        nodeBlockNums.resetQuick();
        nodeBlockNums.ensureCapacity(nds.size());
        nodeBlockBndIdx.resetQuick();
        nodeBlockNums.ensureCapacity(nds.size());
        nodeBlockNums.fill(0, nds.size(), 0);
        nodeBlockBndIdx.fill(0, nds.size(), -1);

        int bIdx = 0;
        for (Boundary bnd : bnds) {
            boolean isCenterInside = isPtInSideBnd(bnd, center, t);
            if (!isBoundaryNode && !isCenterInside) {
                bIdx++;
                continue;
            }
            int nIdx = 0;

            for (Node nd : nds) {

                if (nodeBlockNums.get(nIdx) > 1) {
                    nIdx++;
                    continue;
                }
                if (isBoundaryNode) {
                    boolean needContinue = false;
                    for (int i = 0; i < bnd.num(); i++) {
                        if (nd == bnd.getNode(i)) {
                            needContinue = true;
                            break;
                        }
                    }
                    if (needContinue) {
                        nIdx++;
                        continue;
                    }
                }

                boolean isIntersect;
                switch (dim) {
                    case 2:
                        LineBoundary line = (LineBoundary) bnd;
                        isIntersect = BoundaryUtils.isLine2DLineBoundaryIntersect(center, nd, line);
                        break;
                    case 3:
                        TriangleBoundary tri = (TriangleBoundary) bnd;
                        isIntersect = BoundaryUtils.isLineTriangleSegmentIntersect(center, nd, tri, bndNormals.get(tri.getId()));
                        break;
                    default:
                        throw new IllegalStateException();
                }
                if (isIntersect) {
                    int blockNum = nodeBlockNums.get(nIdx) + 1;
                    nodeBlockNums.setQuick(nIdx, blockNum);
                    nodeBlockBndIdx.setQuick(nIdx, bIdx);
                }
                nIdx++;
            }
            bIdx++;
        }
    }

    /**
     * Gets the nodes of
     * <code>bnds</code>
     *
     * @param bnds
     * @param result can be null
     * @return nodesTree without duplication
     */
    public List<Node> getBndsNodes(Collection<Boundary> bnds, List<Node> result) {
        if (result == null) {
            result = new LinkedList<>();
        } else {
            result.clear();
        }
        for (Boundary bnd : bnds) {
            for (int i = 0; i < bnd.num(); i++) {
                Node node = bnd.getNode(i);
                if (nodeStatusCache[node.id] == -1) {
                    nodeStatusCache[node.id] = 1;
                    result.add(node);
                }
            }
        }
        for (Node nd : result) {
            nodeStatusCache[nd.id] = -1;
        }
        return result;
    }

    /**
     * Calculates a position translate from
     * <code>pt</code> into the inside half space of
     * <code>bnd</code> a little. The translation is along the out normal of
     * <code>bnd</code> in opposite direction.
     *
     * @param pt translation begining
     * @param bnd
     * @param ratio the translation distance / the circument raidus of
     * <code>bnd</code>
     * @param result can be null
     * @return result
     */
    public Coordinate bndPtTrans(Coordinate pt, Boundary bnd, double ratio, Coordinate result) {
        if (null == result) {
            result = new Coordinate();
        }
        Coordinate normal = bndNormals.get(bnd.getId());
        double transDist = -bndRadius[bnd.getId()] * ratio;
        GeometryMath.scale(normal, transDist, result);
        result.x += pt.x;
        result.y += pt.y;
        result.z += pt.z;
        return result;
    }

    public class VisibleCritieron implements SupportDomainCritierion {

        NearestKVisibleDomainSizer domainSizer;
        public int nodeNumMin, nodeNumMax;
        public double radiusEnlargeFactor = 1.2,
                radiusSafeFactor = 1.1,
                centerTranserDistanceRatio = 0.01;    //Center at Boundary Needs to transfer against boundary normal temprorily to ensure visibleFilter algorithm's robust

        public VisibleCritieron(int nodeNumMin, int nodeNumMax) {
            this.nodeNumMin = nodeNumMin;
            this.nodeNumMax = nodeNumMax;
            initSearchRadius = Math.sqrt(nodeNumMin / 3.0) * 1.5 * maxBndRad;
            domainSizer = new NearestKVisibleDomainSizer(nodeNumMin, initSearchRadius);
        }
        DistanceSquareFunction distFun = new DistanceSquareFunctions.Common();

        @Override
        public double setCenter(Coordinate center, Boundary centerBnd, List<Node> outputNodes) {
            distFun.setCenter(center);
            Coordinate actCenter;
            if (centerBnd != null) {
                actCenter = new Coordinate();
                bndPtTrans(center, centerBnd, centerTranserDistanceRatio, actCenter);
            } else {
                actCenter = center;
            }

            return domainSizer.domain(actCenter, outputNodes);
        }

        @Override
        public DistanceSquareFunction getDistanceSquareFunction() {
            return distFun;
        }

        @Override
        public SupportDomainCritierion avatorInstance() {
            return new VisibleCritieron(nodeNumMin, nodeNumMax);
        }
    }

    public class NearestKVisibleDomainSizer implements SupportDomainSizer {

        int k;
        double rInit;
        double enlargeRatio = 1.42;
        int maxIter = 4;
        CenterDistanceComparator<Coordinate> comp = Coordinates.inverseCenterDistanceComparator();
        PriorityQueue<Node> pq = new PriorityQueue<>(defaultMaxNodeNumInSupportDomain, comp);
        LinkedList<Boundary> bnds = new LinkedList<>();
        LinkedList<Node> nds = new LinkedList<>();
        LinkedList<Node> bndNds = new LinkedList<>();
        TIntArrayList blockedNum = new TIntArrayList(defaultMaxNodeNumInSupportDomain);
        TIntArrayList blockedBndId = new TIntArrayList(defaultMaxNodeNumInSupportDomain);

        public NearestKVisibleDomainSizer(int k, double rInit) {
            this.k = k;
            this.rInit = rInit;
        }

        @Override
        public double domain(Coordinate center, List<Node> outputs) {
            double rSearch = rInit;
            int iter = 1;
            if (null == outputs) {
                outputs = new LinkedList<>();
            }
            do {
                searchSpaceNodes(center, rSearch, nds);
                searchBoundary(center, rSearch, bnds);
                visibleStatus(center, nds, false, bnds, blockedNum, blockedBndId);
                outputs.clear();
                int idx = 0;
                for (Node nd : nds) {
                    if (blockedNum.getQuick(idx) < 1) {
                        outputs.add(nd);
                    }
                    idx++;
                }
                getBndsNodes(bnds, bndNds);
                visibleStatus(center, bndNds, true, bnds, blockedNum, blockedBndId);
                idx = 0;
                for (Node nd : bndNds) {
                    if (blockedNum.getQuick(idx) < 1) {
                        outputs.add(nd);
                    }
                    idx++;
                }
                rSearch *= enlargeRatio;
                iter++;
            } while (outputs.size() < k && iter <= maxIter);

            if (outputs.size() < k) {
                throw new IllegalStateException();
            }

            pq.clear();
            comp.setCenter(center);
            for (Node nd : outputs) {
                pq.add(nd);
                if (pq.size() > k) {
                    pq.poll();
                }
            }

            Node nd = pq.peek();
            double radius = distance(center, nd);
            outputs.clear();
            outputs.addAll(pq);
            return radius;
        }
    }
}
