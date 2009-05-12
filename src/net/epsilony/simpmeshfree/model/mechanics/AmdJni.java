/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.Arrays;
import java.util.List;
import net.epsilony.simpmeshfree.model.geometry.Node;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSPDBandMatrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 *
 * @author epsilon
 */
public class AmdJni {

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

        return P;

    }

    public int bandWidth() {
        int bandWidth = 0;
        int start, end, max, t;
        for (int i = 0; i < n; i++) {
            start = Ap[i];
            end = Ap[i + 1];
            max = i;
            for (int j = start + 1; j < end; j++) {
                t = P[Ai[j]];
                if (max < t) {
                    max = t;
                }

            }
            if (bandWidth < max - i) {
                bandWidth = max - i;
            }
        }
        return bandWidth;
    }

    public UpperSPDBandMatrix complile(FlexCompRowMatrix m, Vector b){//,List<Node> nodes) {
        amdOrder(m);
        int bw = bandWidth();
        UpperSPDBandMatrix result = new UpperSPDBandMatrix(m.numRows(), bw);
        SparseVector rowVect;
        int row, col;
        DenseVector tv = new DenseVector(b);
        for (int i = 0; i < m.numRows(); i++) {
            rowVect = m.getRow(i);
            row = P[i];
            for (VectorEntry ve : rowVect) {
                col = P[ve.index()];
                if (col >= row) {
                    result.set(row, col, ve.get());
                }
            }
            b.set(row, tv.get(i));
        }
//        int i=0;
//        for(Node n:nodes){
//            n.setMatrixIndex(P[i]);
//            i=i+1;
//        }
        return result;
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
