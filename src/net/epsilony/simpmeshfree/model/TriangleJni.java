/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class TriangleJni {

    double[] pointsXYIn;// = new double[]{80, 0, 100, 50, 0, 100, -100, 50, -80, 0, -100, -50, 0, -100, 100, -50, 0, -90, 80, -50, 0, -10, -80, -50, -70, 50, -60, 30, -10, 55, -40, 55, 70, 50, 60, 30, 10, 55, 40, 55, -10, 25, -20, -10, 10, 25, 20, -10, -50, 0, 50, 0};
    int pointsXYSizeIn;// = 26;
    int[] pointsMarkerIn;// = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 7, 7, 0, 0};
    boolean hasPointsMarkerIn;// = true;
    double[] pointsXYOut;
    int pointsXYSizeOut;
    int[] pointsMarkerOut;
    boolean hasPointsMarkerOut;
    int[] segmentsIn;// = new int[]{1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 1, 9, 10, 10, 11, 11, 12, 12, 9, 13, 14, 14, 15, 15, 16, 16, 13, 17, 18, 18, 19, 19, 20, 20, 17, 21, 22, 23, 24};
    int segmentsSizeIn;// = 22;
    int[] segmentsMarkerIn;// = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6};
    boolean hasSegmentsMarkerIn;// = true;
    int[] segmentsOut;
    int segmentsSizeOut;
    boolean hasSegmentsMarkerOut;
    int[] segmentsMarkerOut;
    double[] holesXYIn;// = new double[]{0, -50, -50, 50, 50, 50};
    int holesXYSizeIn;//=3;
    int[] trianglesOut;
    int trianglesSizeOut;
    int[] neighborsOut;
    boolean hasNeighorsOut;
    /**
     * the switch shouldn't include 'z'!!! or it will get a bad result by triangleFun();
     */
    String s = new String();// = "pq0";

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
        System.out.println("arch = " + arch);
        System.out.println("name = " + name);
        if (arch.equals("i386")) {
            if (name.equals("Linux")) {
                System.load(System.getProperty("user.dir") + "/TriangleJni.so");
            } else {
                throw new UnsupportedOperationException();
            //System.load(System.getProperty("user.dir")+"\\TriangleJni.dll");
            }
        } else if (arch.equals("amd64")) {
            if (name.equals("Linux")) {
                System.load(System.getProperty("user.dir") + "/TriangleJniAmd64.so");
            }else{
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
        System.out.println(jni.pointsXYSizeOut);
    }
}