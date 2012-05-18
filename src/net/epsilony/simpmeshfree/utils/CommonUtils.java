/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.ArrayList;
import no.uib.cipr.matrix.DenseVector;
import org.apache.commons.math.util.MathUtils;

/**
 *
 * @author epsilon
 */
public class CommonUtils {
    public static int len2DBase(int order){
        return (order+2)*(order+1)/2;
    }
    
    public static int len3DBase(int order){
         int result=0;
         for(int i=0;i<=order;i++){
             result+=MathUtils.binomialCoefficient(2+i, 2);
         }
         return result;
    }
    
    public static int lenBase(int dim,int order){
        switch(dim){
            case 2:
                return len2DBase(order);
            case 3:
                return len3DBase(order);
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    public static void vectorMultTDoubleArrayLists(DenseVector gamma,ArrayList<TDoubleArrayList> B,DenseVector result){
        for(int i=0;i<B.get(0).size();i++){
            double t=0;
            for(int j=0;j<gamma.size();j++){
                t+=gamma.get(j)*B.get(j).getQuick(i);
            }
            result.set(i,t);
        }
    }
}
