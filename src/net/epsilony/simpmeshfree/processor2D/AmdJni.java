/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.processor2D;

import java.util.Arrays;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class AmdJni {

    transient static Logger log = Logger.getLogger(AmdJni.class);
    int n;
    int[] Ap;
    int[] Ai;
    int[] P;

    public int[] amdOrder(FlexCompRowMatrix m) {
        n = m.numRows();
        Ap = new int[n + 1];
        P = new int[n];
        m.compact();
        SparseVector row;
        int sumSz = 0;
        Ap[0] = 0;
        for (int i = 0; i < n; i++) {
            row = m.getRow(i);
            sumSz += row.getUsed();
            Ap[i + 1] = sumSz;
        }
        Ai = new int[sumSz];
        int[] rowIndex;
        sumSz = 0;
        for (int i = 0; i < n; i++) {
            row = m.getRow(i);
            rowIndex = row.getIndex();
            for (int j = 0; j < rowIndex.length; j++) {
                Ai[sumSz] = rowIndex[j];
                sumSz++;
            }
        }

        AmdFun();
        int[] tds=new int[P.length];
        for(int i=0;i<tds.length;i++){
            tds[P[i]]=i;
        }

        P=tds;

        return P;

    }

    public int bandWidth() {
        log.info("Start bandwidth");
        int bandWidth = 0;
        int start, end;
        int[] furthest = new int[n];
        int row, col;
        for (int i = 0; i < n; i++) {
            start = Ap[i];
            end = Ap[i + 1];
            row = P[Ai[start]];
            for (int j = start + 1; j < end; j++) {
                col = P[Ai[j]];
                if (row > col) {
                    if (furthest[col] < row) {
                        furthest[col] = row;
                    }
                } else {
                    if (furthest[row] < col) {
                        furthest[row] = col;
                    }
                }
            }

        }


        bandWidth = 0;
        for (int i = 0; i < n; i++) {
            if (bandWidth < furthest[i] - i) {
                bandWidth = furthest[i] - i;
            }
        }
        log.info("end of bandWith:"+bandWidth);
        return bandWidth;
    }

    public int oriBandWidth(){
         log.info("Start oriBandWidth");
        int bandwidth = 0;
        int start, end;
        for (int i = 0; i < n; i++) {
            start = Ap[i];
            end = Ap[i + 1];
            for (int j = start + 1; j < end; j++) {
                if(Ai[j]-i>bandwidth){
                    bandwidth=Ai[j]-i;
                }
            }
        }
        log.info("End of oriBandWidth:"+bandwidth);
        return bandwidth;
         
    }

    public Object[] compile(FlexCompRowMatrix m, Vector b) {//,List<Node> nodes) {
        log.info("Start complile");
        log.info("Start amdOrder jni");
        amdOrder(m);
        oriBandWidth();
        log.info("Finished: amdOrder jni");

        int bw = bandWidth();

        UpperSymmBandMatrix result = new UpperSymmBandMatrix(m.numRows(), bw);
        SparseVector rowVect;
        int row, col;
        DenseVector resultv = new DenseVector(b.size());
        for (int i = 0; i < m.numRows(); i++) {
            rowVect = m.getRow(i);
            row = P[i];
            for (VectorEntry ve : rowVect) {
                col = P[ve.index()];
                if (col >= row) {
                    result.set(row, col, ve.get());
                } else {
                    result.set(col, row, ve.get());
                }
            }
            resultv.set(row, b.get(i));
        }

        log.info("End of compile()");
        return new Object[]{result,resultv};
    }

    public native void AmdFun();

    public static void main(String[] args) {
        AmdJni ajni = new AmdJni();
        ajni.n = 5;
        ajni.Ap = new int[]{0, 2, 5, 8, 9, 10};
        ajni.Ai = new int[]{0, 1, 1, 2, 4, 2, 3, 4, 3, 4};
        ajni.P = new int[5];
        ajni.AmdFun();
        System.out.println(Arrays.toString(ajni.P));
        FlexCompRowMatrix m = new FlexCompRowMatrix(5, 5);
        m.set(0, 0, 1);
        m.set(0, 1, 1);
        m.set(1, 1, 1);
        m.set(1, 2, 1);
        m.set(1, 4, 1);
        m.set(2, 2, 1);
        m.set(2, 3, 1);
        m.set(2, 4, 1);
        m.set(3, 3, 1);
        m.set(4, 4, 1);
        System.out.println(Arrays.toString(ajni.amdOrder(m)));
        System.out.println("ajni.bandWidth() = " + ajni.bandWidth());
        UpperSymmBandMatrix mm = new UpperSymmBandMatrix(4, 2);
        mm.set(0, 2, 1);


    }


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
                System.load("/usr/lib32/AMD_Native.so");
            } else {
                throw new UnsupportedOperationException();
            //System.load(System.getProperty("user.dir")+"\\TriangleJni.dll");
            }
        } else if (arch.equals("amd64")) {
            if (name.equals("Linux")) {
//                System.load(System.getProperty("user.dir") + "/TriangleJniAmd64.so");
                System.load("/usr/lib64/AMD_Native.so");
            } else {
                throw new UnsupportedOperationException();
            }
        }
    //normal mode
    //System.loadLibrary("TriangleJni");
    }
}
