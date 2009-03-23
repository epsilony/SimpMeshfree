/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author epsilon
 */
public class TriangleJni {

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

    public void setHoles(double[] holesXYIn, int holesXYSizeIn) {
        this.holesXYSizeIn = holesXYSizeIn;
        this.holesXYIn = Arrays.copyOf(holesXYIn, holesXYSizeIn * 2);
    }

    public void setSwitchs(String s) {
        this.s = s;
    }

    private ArrayList<ApproximatePoint> approximatePoints=new ArrayList<ApproximatePoint>();
    public void setPointsSegments(List<ApproximatePoint> aprxPts, List<Point> pts) {
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
        for (ApproximatePoint ap : aprxPts) {
            pointsXYIn[2 * i] = ap.getX();
            pointsXYIn[2 * i + 1] = ap.getY();
            pointsMarkerIn[i] = ap.segment.index;
            approximatePointToPointIndex.put(ap, i);
            i++;
        }
        i = 0;
        for (ApproximatePoint ap : aprxPts) {
            segmentsIn[2 * i] = approximatePointToPointIndex.get(ap) + 1;
            segmentsIn[2 * i + 1] = approximatePointToPointIndex.get(ap.r) + 1;
            segmentsMarkerIn[i] = ap.segment.index;
            i++;
        }

    }

    public ArrayList<Node> getNodesTriangles(ArrayList<Node> nodes, ArrayList<Triangle> trs) {
        nodes.clear();
        nodes.ensureCapacity(pointsXYSizeOut);
//        System.out.println("pointsXYSizeOut = " + pointsXYSizeOut);
        for(int i = 0;i<approximatePoints.size();i++){
            nodes.add(new BoundaryNode(approximatePoints.get(i)));
        }

        for (int i = approximatePoints.size(); i < pointsXYSizeOut; i++) {
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
        jni.s = "pq0nQ";
        jni.triangleFun();
//        System.out.println(jni.pointsXYSizeOut);
    }
}