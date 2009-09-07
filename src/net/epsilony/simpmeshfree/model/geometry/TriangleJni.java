/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class TriangleJni {

    
    transient static Logger log = Logger.getLogger(TriangleJni.class);
    public double[] pointsXYIn;// = new double[]{80, 0, 100, 50, 0, 100, -100, 50, -80, 0, -100, -50, 0, -100, 100, -50, 0, -90, 80, -50, 0, -10, -80, -50, -70, 50, -60, 30, -10, 55, -40, 55, 70, 50, 60, 30, 10, 55, 40, 55, -10, 25, -20, -10, 10, 25, 20, -10, -50, 0, 50, 0};
    public int pointsXYSizeIn;// = 26;
    public int[] pointsMarkerIn;// = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 7, 7, 0, 0};
    public boolean hasPointsMarkerIn;// = true;
    public double[] pointsXYOut;
    public int pointsXYSizeOut;
    public int[] pointsMarkerOut;
    public boolean hasPointsMarkerOut;
    public int[] segmentsIn;// = new int[]{1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 1, 9, 10, 10, 11, 11, 12, 12, 9, 13, 14, 14, 15, 15, 16, 16, 13, 17, 18, 18, 19, 19, 20, 20, 17, 21, 22, 23, 24};
    public int segmentsSizeIn;// = 22;
    public int[] segmentsMarkerIn;// = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6};
    public boolean hasSegmentsMarkerIn;// = true;
    public int[] segmentsOut;
    public int segmentsSizeOut;
    public boolean hasSegmentsMarkerOut;
    public int[] segmentsMarkerOut;
    public double[] holesXYIn;// = new double[]{0, -50, -50, 50, 50, 50};
    public int holesXYSizeIn;//=3;
    public int[] trianglesOut;
    public int trianglesSizeOut;
    public int[] neighborsOut;
    public boolean hasNeighorsOut;
    /**
     * the switch shouldn't include 'z'!!! or it will get a bad result by triangleFun();
     */
    String s = new String();// = "pq0";

    /**
     * 设置孔洞中一点的位置
     * @param holesXYIn {x<sub>1</sub>, y<sub>1</sub>, x<sub>2</sub>, y<sub>2</sub>, ... , x<sub>n</sub>, y<sub>n</sub>}
     * @param holesXYSizeIn n:共有多少个孔洞
     */
    public void setHoles(double[] holesXYIn, int holesXYSizeIn) {
        this.holesXYSizeIn = holesXYSizeIn;
        this.holesXYIn = Arrays.copyOf(holesXYIn, holesXYSizeIn * 2);
    }

    /**
     * 设置triangle的switches
     * @param s
     */
    public void setSwitchs(String s) {
        this.s = s;
    }
    boolean needfit = true;

    /**
     * 依几何模型生成一个划分，本函数的调用一般在:
     * <br>{@link #setSwitchs(java.lang.String)}</br>
     * <br>{@link #setHoles(double[], int) }</br>
     * <br>{@link #setPointsSegments(java.util.List, java.util.List) }</br>
     * <br>之后</br>
     * @param gm {@link GeometryModel}
     * @param switchs
     */
    public void complie(Model gm, String switchs) {
        log.info("Start Complie");
        log.info("set Points Segments by GeometryModel's approximate Points");
        setPointsSegments(gm.getApproximatePoints(), new LinkedList<Point>());
        setSwitchs(switchs);
        log.info("set hole inside points");
        LinkedList<Point> holePoints = gm.getHolesXYs();
        double[] holesXYs = new double[holePoints.size() * 2];
        int i = 0;
        for (Point pt : holePoints) {
            holesXYs[i * 2] = pt.getX();
            holesXYs[i * 2 + 1] = pt.getY();
            i++;
        }

        setHoles(holesXYs, holePoints.size());
        log.info("jni the triangle");
        triangleFun();
        log.info("End of compile");
        needfit = true;
    }
    private ArrayList<ApproximatePoint> approximatePoints = new ArrayList<ApproximatePoint>();


    public void setPointsSegments(List<ApproximatePoint> aprxPts, List<Point> pts) {
        approximatePoints.clear();
        approximatePoints.ensureCapacity(aprxPts.size());
        approximatePoints.addAll(aprxPts);
        pointsXYSizeIn = aprxPts.size() + pts.size();
        pointsXYIn = new double[2 * pointsXYSizeIn];
        segmentsSizeIn = aprxPts.size();
        segmentsIn = new int[segmentsSizeIn * 2];
        pointsMarkerIn = new int[pointsXYSizeIn];
        hasPointsMarkerIn = true;
        segmentsMarkerIn = new int[segmentsSizeIn];
        hasSegmentsMarkerIn = true;
        int i = 0;
        TreeMap<ApproximatePoint, Integer> approximatePointToPointIndex = new TreeMap(new Comparator<ApproximatePoint>() {

            @Override
            public int compare(ApproximatePoint o1, ApproximatePoint o2) {
                return o1.index - o2.index;
            }
        });
        i = 0;
        for (ApproximatePoint ap : aprxPts) {
            pointsXYIn[2 * i] = ap.getX();
            pointsXYIn[2 * i + 1] = ap.getY();
            pointsMarkerIn[i] = i + 1;
//            pointsMarkerIn[i] = ap.segment.index;
            approximatePointToPointIndex.put(ap, i);
            i++;
        }
        i = 0;
        for (ApproximatePoint ap : aprxPts) {
            segmentsIn[2 * i] = approximatePointToPointIndex.get(ap) + 1;
            segmentsIn[2 * i + 1] = approximatePointToPointIndex.get(ap.front) + 1;
            segmentsMarkerIn[i] = i + 1;
//            segmentsMarkerIn[i] = ap.segment.index;
            i++;
        }

    }

    /**
     * 将triangle划开的点移动到几何边界上去
     */
    private void fitPointsAlignSegment() {
        if (needfit == false) {
            return;
        }
        boolean[] pointMeeted = new boolean[pointsXYSizeOut];
        double x, y, x2, y2;
        for (int i = 0; i < segmentsSizeOut * 2; i++) {
            int index = segmentsOut[i] - 1;
//            if (log.isDebugEnabled()) {
//
//                if (i % 2 == 0) {
//                    log.debug(String.format("segmentMarker[%d]=%d", i / 2, segmentsMarkerOut[i / 2]));
//
//                }
//                log.debug(String.format("pointsMarkerOut[%d]=[%d]", index, pointsMarkerOut[index]));
//            }
            if (!pointMeeted[index]) {
                if (index >= approximatePoints.size()) {
                    ApproximatePoint ap = approximatePoints.get(pointsMarkerOut[index] - 1);
                    ApproximatePoint apfr = ap.front;
                    x = pointsXYOut[index * 2];
                    y = pointsXYOut[index * 2 + 1];
                    double bigLen = Math.sqrt((apfr.x - ap.x) * (apfr.x - ap.x) + (apfr.y - ap.y) * (apfr.y - ap.y));
                    double smallLen = Math.sqrt((x - ap.x) * (x - ap.x) + (y - ap.y) * (y - ap.y));
                    double t1 = ap.segmentParm;
                    double t2 = apfr.segmentParm == 0 ? 1 : apfr.segmentParm;
                    double t = t1 + (t2 - t1) * smallLen / bigLen;
                    double[] tds = new double[2];
                    ap.segment.parameterPoint(t, tds);
                    x2 = tds[0];
                    y2 = tds[1];
                    pointsXYOut[index * 2] = x2;
                    pointsXYOut[index * 2+1] = y2;
                }
                pointMeeted[index] = true;
            }

        }
        needfit = false;
    }

    /**
     * 获取最近一次调用{@link #complie(net.epsilony.simpmeshfree.model.geometry.GeometryModel, java.lang.String) }后生成的三角形几何信息
     * @return {trangle<sub>1</sub>.x<sub>1</sub>, trangle<sub>1</sub>.y<sub>1</sub>, trangle<sub>1</sub>.x<sub>2</sub>, trangle<sub>1</sub>.y<sub>2</sub>, trangle<sub>1</sub>.x<sub>3</sub>, trangle<sub>1</sub>.y<sub>3</sub>, trangle<sub>2</sub>.x<sub>1</sub>, trangle<sub>2</sub>.y<sub>1</sub>, trangle<sub>2</sub>.x<sub>2</sub>, trangle<sub>2</sub>.y<sub>2</sub>, trangle<sub>2</sub>.x<sub>3</sub>, trangle<sub>2</sub>.y<sub>3</sub>, ..., trangle<sub>n</sub>.x<sub>1</sub>, trangle<sub>n</sub>.y<sub>1</sub>, trangle<sub>n</sub>.x<sub>2</sub>, trangle<sub>n</sub>.y<sub>2</sub>, trangle<sub>n</sub>.x<sub>3</sub>, trangle<sub>n</sub>.y<sub>3</sub>}
     */
    public LinkedList<double[]> getTriangleXYsList() {
        log.info("Start getTriangleXYsList()");
        LinkedList<double[]> triXYs = new LinkedList<double[]>();
        double[] triXY;
        fitPointsAlignSegment();
        for (int i = 0; i < trianglesSizeOut; i++) {
            triXY = new double[6];
            triXY[0] = pointsXYOut[(trianglesOut[i * 3] - 1) * 2];
            triXY[1] = pointsXYOut[(trianglesOut[i * 3] - 1) * 2 + 1];
            triXY[2] = pointsXYOut[(trianglesOut[i * 3 + 1] - 1) * 2];
            triXY[3] = pointsXYOut[(trianglesOut[i * 3 + 1] - 1) * 2 + 1];
            triXY[4] = pointsXYOut[(trianglesOut[i * 3 + 2] - 1) * 2];
            triXY[5] = pointsXYOut[(trianglesOut[i * 3 + 2] - 1) * 2 + 1];
            triXYs.add(triXY);
        }
        log.info(String.format("End of getTriangleXYsList%n result.size()=%d", triXYs.size()));
        return triXYs;
    }


    private BoundaryNode newSegmentBoundaryNode(ApproximatePoint ap, double x, double y) {
        ApproximatePoint apfr = ap.front;
        double bigLen = Math.sqrt((apfr.x - ap.x) * (apfr.x - ap.x) + (apfr.y - ap.y) * (apfr.y - ap.y));
        double smallLen = Math.sqrt((x - ap.x) * (x - ap.x) + (y - ap.y) * (y - ap.y));
        double t1 = ap.segmentParm;
        double t2 = apfr.segmentParm == 0 ? 1 : apfr.segmentParm;
        double t = t1 + (t2 - t1) * smallLen / bigLen;
        double[] tds = new double[2];
        ap.segment.parameterPoint(t, tds);
        x = tds[0];
        y = tds[1];
        BoundaryNode bd = new BoundaryNode(ap);
        bd.x = x;
        bd.y = y;
        bd.segmentParm = t;
        bd.segment=ap.segment;
        return bd;
    }

    /**
     * 获取最近一次调用{@link #complie(net.epsilony.simpmeshfree.model.geometry.GeometryModel, java.lang.String) }后由划分顶点构成的结点
     * @param needNeighbors 是否将结点的邻接结点信息写入结点
     * @return 包括边界结点{@link BoundaryNode}的结点列
     */
    public ArrayList<Node> getNodes(boolean needNeighbors) {
        log.info(String.format("start getNodes(%b)", needNeighbors));
//        if (log.isDebugEnabled()) {
//            log.debug("pointsXYSizeIn=" + pointsXYSizeIn);
//            log.debug("pointsXYSizeOut=" + pointsXYSizeOut);
//        }
        ArrayList<Node> nodes = new ArrayList<Node>(pointsXYSizeOut);

        int index;
        double x, y;
        BoundaryNode bd;
        boolean[] pointAdded = new boolean[pointsXYSizeOut];
        for (int i = 0; i < segmentsSizeOut * 2; i++) {
            index = segmentsOut[i] - 1;
//            if (log.isDebugEnabled()) {
//
//                if (i % 2 == 0) {
//                    log.debug(String.format("segmentMarker[%d]=%d", i / 2, segmentsMarkerOut[i / 2]));
//
//                }
//                log.debug(String.format("pointsMarkerOut[%d]=[%d]", index, pointsMarkerOut[index]));
//            }
            if (!pointAdded[index]) {
                if (index < approximatePoints.size()) {
                    nodes.add(new BoundaryNode(approximatePoints.get(index)));
                } else {
                    x = pointsXYOut[index * 2];
                    y = pointsXYOut[index * 2 + 1];
                    bd = newSegmentBoundaryNode(approximatePoints.get(pointsMarkerOut[index] - 1), x, y);
                    nodes.add(bd);
                    pointsXYOut[index * 2] = bd.x;
                    pointsXYOut[index * 2 + 1] = bd.y;
                }
                pointAdded[index] = true;

            }

        }
        needfit = false;

        for (int i = 0; i < pointsXYSizeOut; i++) {
            if (pointAdded[i]) {
                continue;
            }
            nodes.add(new Node(pointsXYOut[i * 2], pointsXYOut[i * 2 + 1]));
        }

        if (needNeighbors == false) {
            log.info("End of getNodes()");
            return nodes;
        }

        log.info("Start filling nodes' neighbors and triangles");
        TreeSet<Node>[] neighbors = new TreeSet[pointsXYSizeOut];
        Comparator<Node> nodeCmp = new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return o1.index - o2.index;
            }
        };
        for (int i = 0; i < pointsXYSizeOut; i++) {
            neighbors[i] = new TreeSet(nodeCmp);
        }

        //Triangles and Nodes neighbor and triangleset
        Node n1, n2, n3;

        Triangle tr;
        for (int i = 0; i < trianglesSizeOut; i++) {
            n1 = nodes.get(trianglesOut[i * 3] - 1);
            n2 = nodes.get(trianglesOut[i * 3 + 1] - 1);
            n3 = nodes.get(trianglesOut[i * 3 + 2] - 1);
            n1.neighbors.add(n2);
            n2.neighbors.add(n1);
            n2.neighbors.add(n3);
            n3.neighbors.add(n2);
            n3.neighbors.add(n1);
            n1.neighbors.add(n3);
            tr = new Triangle(n1, n2, n3);

            n1.triangles.add(tr);
            n2.triangles.add(tr);
            n3.triangles.add(tr);
        }
//        System.out.println("nodes.size()" + nodes.size());
        return nodes;
    }

    /**
     * 获取最近一次调用{@link #complie(net.epsilony.simpmeshfree.model.geometry.GeometryModel, java.lang.String) }后由划分顶点构成的结点 以及划分出来的三角形
     * @param nodes
     * @param trs
     * @return
     * @see Triangle
     */
    public ArrayList<Node> getNodesTriangles(ArrayList<Node> nodes, ArrayList<Triangle> trs) {
        log.info("Start getNodesTriangles");
        nodes.clear();
        nodes.ensureCapacity(pointsXYSizeOut);
        int index;
        double x, y;
        BoundaryNode bd;
        boolean[] pointAdded = new boolean[pointsXYSizeOut];
        for (int i = 0; i < segmentsSizeOut * 2; i++) {
            index = segmentsOut[i] - 1;
//            if (log.isDebugEnabled()) {
//
//                if (i % 2 == 0) {
//                    log.debug(String.format("segmentMarker[%d]=%d", i / 2, segmentsMarkerOut[i / 2]));
//
//                }
//                log.debug(String.format("pointsMarkerOut[%d]=[%d]", index, pointsMarkerOut[index]));
//            }
            if (!pointAdded[index]) {
                if (index < approximatePoints.size()) {
                    nodes.add(new BoundaryNode(pointsXYOut[index * 2], pointsXYOut[index * 2 + 1]));
                } else {
                    x = pointsXYOut[index * 2];
                    y = pointsXYOut[index * 2 + 1];
                    bd = newSegmentBoundaryNode(approximatePoints.get(pointsMarkerOut[index] - 1), x, y);
                    nodes.add(bd);
                    pointsXYOut[index * 2] = bd.x;
                    pointsXYOut[index * 2 + 1] = bd.y;
                }
                pointAdded[index] = true;

            }

        }
        needfit = false;

        for (int i = 0; i < pointsXYSizeOut; i++) {
            if (pointAdded[i]) {
                continue;
            }
            nodes.add(new Node(pointsXYOut[i * 2], pointsXYOut[i * 2 + 1]));
        }
//        System.out.println("segmentsSizeOut = " + segmentsSizeOut);
//        System.out.println("segmentsOut.length = " + segmentsOut.length);
        TreeSet<Node>[] neighbors = new TreeSet[pointsXYSizeOut];
        Comparator<Node> nodeCmp = new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return o1.index - o2.index;
            }
        };
        for (int i = 0; i < pointsXYSizeOut; i++) {
            neighbors[i] = new TreeSet(nodeCmp);
        }

        //Triangles and Nodes neighbor and triangleset
        Node n1, n2, n3;
        trs.ensureCapacity(trianglesSizeOut);
        Triangle tr;
        for (int i = 0; i < trianglesSizeOut; i++) {
            n1 = nodes.get(trianglesOut[i * 3] - 1);
            n2 = nodes.get(trianglesOut[i * 3 + 1] - 1);
            n3 = nodes.get(trianglesOut[i * 3 + 2] - 1);
            n1.neighbors.add(n2);
            n2.neighbors.add(n1);
            n2.neighbors.add(n3);
            n3.neighbors.add(n2);
            n3.neighbors.add(n1);
            n1.neighbors.add(n3);
            tr = new Triangle(n1, n2, n3);
            trs.add(tr);
            n1.triangles.add(tr);
            n2.triangles.add(tr);
            n3.triangles.add(tr);
        }
        log.info("End of getNodesTriangles()");
//        System.out.println("nodes.size()" + nodes.size());
        return nodes;
    }

    /**
     * 驱动triangle的jni函数，用与通过*In和*Out的类成员交换数据。
     */
    public native void triangleFun();

    //读入triangleFun有关的库


    static {
        //for debuging the native method
        //System.load("/home/epsilon/documents/4_java/javaProjects/SimpMeshfree/TriangleJni/TriangleJni.so");
        //System.out.println(System.getProperty("java.library.path"));
        //System.out.println(System.getProperty("user.dir"));
        String arch = System.getProperty("os.arch");
        String name = System.getProperty("os.name");
//        System.out.println("arch = " + arch);
//        System.out.println("name = " + name);
        if (arch.equals("i386")) {
            if (name.equals("Linux")) {
//                System.load(System.getProperty("user.dir") + "/TriangleJni.so");
                System.load("/usr/lib32/TriangleJni32.so");
            } else {
                throw new UnsupportedOperationException();
            //System.load(System.getProperty("user.dir")+"\\TriangleJni.dll");
            }
        } else if (arch.equals("amd64")) {
            if (name.equals("Linux")) {
//                System.load(System.getProperty("user.dir") + "/TriangleJniAmd64.so");
                System.load("/usr/lib64/TriangleJniAmd64.so");
            } else {
                throw new UnsupportedOperationException();
            }
        }
    //normal mode
    //System.loadLibrary("TriangleJni");
    }

    /**
     * a test for triangle -pq0nV
     * @param args
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        TriangleJni jni = new TriangleJni();
        jni.pointsXYIn = new double[]{80, 0, 100, 50, 0, 100, -100, 50, -80, 0, -100, -50, 0, -100, 100, -50, 0, -90, 80, -50, 0, -10, -80, -50, -70, 50, -60, 30, -10, 55, -40, 55, 70, 50, 60, 30, 10, 55, 40, 55, -10, 25, -20, -10, 10, 25, 20, -10, -50, 0, 50, 0};
        jni.pointsXYSizeIn = 26;
        jni.pointsMarkerIn = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 7, 7, 0, 0};
        jni.hasPointsMarkerIn = true;
        jni.segmentsIn = new int[]{1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 1, 9, 10, 10, 11, 11, 12, 12, 9, 13, 14, 14, 15, 15, 16, 16, 13, 17, 18, 18, 19, 19, 20, 20, 17, 21, 22, 23, 24};
        jni.segmentsSizeIn = 22;
        jni.segmentsMarkerIn = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6};
        jni.hasSegmentsMarkerIn = true;
        jni.holesXYIn = new double[]{0, -50, -50, 50, 50, 50};
        jni.holesXYSizeIn = 3;
        jni.s = "pq0nV";
        jni.triangleFun();
        System.out.println(jni.pointsXYSizeOut);
    }
}