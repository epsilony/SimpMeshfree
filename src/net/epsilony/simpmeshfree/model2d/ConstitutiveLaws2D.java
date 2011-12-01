/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import no.uib.cipr.matrix.DenseMatrix;

/**
 * 平面本构模型
 * @author epsilonyuan@gmail.com
 */
public class ConstitutiveLaws2D {

    protected ConstitutiveLaws2D() {
    }
    
    
    public static DenseMatrix getPlanseStrain(double E,double v){
        return getPlaneStress(E/(1-v*v),v/(1-v));
    }
    
    /**
     * 
     * @param E elastic module
     * @param v Poisson's ratio
     * @return Plane Stress
     */
    public static DenseMatrix getPlaneStress(double E,double v){
        double factor=E/(1-v*v);
        DenseMatrix result=new DenseMatrix(3,3);
        result.set(0,0,factor);
        result.set(1,1,factor);
        result.set(2,2,(1-v)/2*factor);
        result.set(0,1,v*factor);
        result.set(1,0,v*factor);
        return result;
    }
}
