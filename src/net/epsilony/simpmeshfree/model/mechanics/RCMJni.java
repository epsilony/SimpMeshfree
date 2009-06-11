/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.util.Arrays;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.model.geometry.Node;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class RCMJni {

    static Logger log = Logger.getLogger(RCMJni.class);
    int n;
    int[] Ap;
    int[] Ai;
    int[] P;
    int[] PInv;
    int bandWidth;
    int pBandWidth;

    public int[] rcmOrder(FlexCompRowMatrix m) {
        n = m.numRows();
        Ap = new int[n + 1];
        P = new int[n];
        m.compact();
        SparseVector row;
        Ap[0] = 0;
        LinkedList[] indes=new LinkedList[n];
        for(int i=0;i<n;i++){
            indes[i]=new LinkedList<Integer>();
        }
        for(int i=0;i<n;i++){
            row=m.getRow(i);
            int[] index=row.getIndex();
            double[] data=row.getData();
            int col;
            for(int j=0;j<index.length;j++){
                col=index[j];
                if(col<=i||data[j]==0){
                    continue;
                }else{
                    indes[i].add(col+1);
                    indes[col].add(i+1);
                }
            }
        }
        int start=1;
        for (int i = 0; i < n; i++) {
           Ap[i]=start;
           start=start+indes[i].size();

        }
        Ap[n]=start;
        Ai = new int[start-1];
        int sum=0;
        for(int i=0;i<n;i++){
            for(Object iObj:indes[i]){
                Ai[sum]=((Integer)iObj).intValue();
                sum++;
            }
        }
        rcmfun();
//        int[] tds=new int[P.length];
//        for(int i=0;i<tds.length;i++){
//            tds[P[i]]=i;
//        }
//
//        P=tds;

        return P;

    }


    public Object[] compile(FlexCompRowMatrix m, DenseVector b) {//,List<Node> nodes) {
        log.info("Start complile");

        rcmOrder(m);
//        Matrix denseMatrix=m;
//        StringBuilder sb=new StringBuilder();
//        for(int i=0;i<denseMatrix.numRows();i++){
//            for(int j=0;j<denseMatrix.numRows();j++){
//                if(0!=denseMatrix.get(i, j)){
//                    if(i!=j){
//                    sb.append("X");
//                    }else{
//                        sb.append("I");
//                    }
//                }else{
//                    sb.append(" ");
//                }
//            }
//            sb.append(String.format("|%n"));
//        }
//        System.out.println(sb);
        log.info("Former Half Band Width:"+bandWidth/2);
        log.info("Optimized Half Band Width:"+pBandWidth/2);


        UpperSymmBandMatrix result = new UpperSymmBandMatrix(m.numRows(), pBandWidth/2);
        SparseVector rowVect;
        int row, col;
        DenseVector resultv = new DenseVector(b.size());
        for (int i = 0; i < m.numRows(); i++) {
            rowVect = m.getRow(i);
            row = PInv[i]-1;

            for (VectorEntry ve : rowVect) {
                if(0==ve.get()){
                        continue;
                    }
                col = PInv[ve.index()]-1;
                
                if (col >= row) {
                    result.set(row, col, ve.get());
                } else {
                    result.set(col, row, ve.get());
                }
            }
            resultv.set(row, b.get(i));
        }

        log.info("End of compile()");
//        denseMatrix=new DenseMatrix(result);
//        sb=new StringBuilder();
//         for(int i=0;i<denseMatrix.numRows();i++){
//            for(int j=0;j<denseMatrix.numRows();j++){
//                if(0!=denseMatrix.get(i, j)){
//                    if(i!=j){
//                    sb.append("X");
//                    }else{
//                        sb.append("I");
//                    }
//                }else{
//                    sb.append(" ");
//                }
//            }
//            sb.append(String.format("|%n"));
//        }
//        System.out.println(sb);

        return new Object[]{result,resultv};
    }

    public static void main(String[] args) {
        RCMJni rjni = new RCMJni();
        rjni.n = 10;
        rjni.Ap = new int[]{
    1, 3, 7, 10, 14, 17, 21, 25, 27, 28, 29
  };

        rjni.Ai = new int[] {
    6, 4,
    3, 10, 7, 5,
    2, 4, 5,
    1, 3, 6, 9,
    2, 3, 7,
    1, 4, 7, 8,
    2, 5, 6, 8,
    6, 7,
    4,
    2 };
        rjni.P = new int[10];
        rjni.rcmfun();
        System.out.println(Arrays.toString(rjni.P));
        System.out.println(Arrays.toString(rjni.PInv));
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
        System.out.println(Arrays.toString(rjni.rcmOrder(m)));
        System.out.println("!!!!rjni.bandWidth= " + rjni.bandWidth);
        System.out.println("!!!!rjni.pBandWidth = " + rjni.pBandWidth);
        UpperSymmBandMatrix mm = new UpperSymmBandMatrix(4, 2);
        mm.set(0, 2, 1);

        FlexCompRowMatrix test=new FlexCompRowMatrix(4, 4);
        test.set(0,0,1);
        test.set(0,1,1);
        test.set(0,2,1);
        test.set(0,0,0);
        test.compact();
        System.out.println(Arrays.toString(test.getRow(0).getIndex()));
        System.out.println(Arrays.toString(test.getRow(0).getData()));
        for(VectorEntry ve:test.getRow(0)){
            System.out.println("ve.index() = " + ve.index());
            System.out.println("ve.get() = " + ve.get());
        }

    }

    public void fillDisplacement(DenseVector xVector,LinkedList<Node> nodes){
                int index1,index2;
        log.info("edit the nodes ux uy data");
        for (Node node : nodes) {
            index1 = PInv[node.getMatrixIndex()*2] -1;
            index2 = PInv[node.getMatrixIndex()*2+1] -1;
            node.setUx(xVector.get(index1));
            node.setUy(xVector.get(index2));
        }

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
                System.load("/usr/lib32/RCM_Native.so");
            } else {
                throw new UnsupportedOperationException();
            //System.load(System.getProperty("user.dir")+"\\TriangleJni.dll");
            }
        } else if (arch.equals("amd64")) {
            if (name.equals("Linux")) {
//                System.load(System.getProperty("user.dir") + "/TriangleJniAmd64.so");
                System.load("/usr/lib64/RCM_Native.so");
            } else {
                throw new UnsupportedOperationException();
            }
        }
    //normal mode
    //System.loadLibrary("TriangleJni");
    }
    native public void rcmfun();

}
