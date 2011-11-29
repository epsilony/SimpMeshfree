/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

/**
 * 由于小数组与Object的申请速度与调用一个函数相当，因此，这个Cache对于50以内的Matrix作用不大。
 * @author epsilonyuan@gmail.com
 */
public class MatrixCache {

    int from, to;
    ArrayList<Matrix> linearCache;

    public MatrixCache(int from, int to, Matrix t) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        linearCache = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            linearCache.add(factory(t, i));
        }
        this.from = from;
        this.to = to;
    }

    public static Matrix factory(Matrix t, int size) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

//        if(t.getClass()==DenseMatrix.class){
//            return new DenseMatrix(size,size);
//        }
//        return null;
        return new DenseMatrix(size,size);

    }

    public Matrix getMatrix(int size) {
        if (size < to && size >= from) {
            return linearCache.get(size - from);
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        int testMin = 5;
        int testMax = 50;
        int testNum = 10000;

        long start, end;

        int[] sizesSample = new int[testNum];
        for (int i = 0; i < testNum; i++) {
            sizesSample[i] = new Random().nextInt(testMax - testMin) + testMin;
        }

        start = System.nanoTime();
        MatrixCache cache = new MatrixCache(testMin, testMax, new DenseMatrix(1, 1));
        end = System.nanoTime();
        long timeCacheBuild=end-start;
        for (int i = 0; i < testNum; i++) {
            DenseMatrix t=(DenseMatrix) cache.getMatrix(sizesSample[i]);//.set(0, 0, 2.0);
        }
        end = System.nanoTime();
        long timeCache = end - start;

        start = System.nanoTime();
        for (int i = 0; i < testNum; i++) {
            int size = sizesSample[i];
            DenseMatrix t = new DenseMatrix(size, size);
            //t.set(0, 0, 2.0);
        }
        end = System.nanoTime();
        long timeNotCached = end - start;
        
        System.out.println("timeCacheBuild = " + timeCacheBuild);
        System.out.println("timeCacheBuild/testNum = " + timeCacheBuild/testNum);
        System.out.println("timeCache = " + timeCache);
        System.out.println("timeCache/testNum = " + timeCache / testNum);
        System.out.println("timeNotCached = " + timeNotCached);
        System.out.println("timeNotCached/testNum = " + timeNotCached / testNum);

        //System.out.println(Arrays.toString(sizesSample));

    }
}
