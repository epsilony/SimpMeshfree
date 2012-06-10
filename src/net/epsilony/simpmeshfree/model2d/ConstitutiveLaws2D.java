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
    
    
    public static DenseMatrix getPlanseStrain(double E,double nu){
        return getPlaneStress(E/(1-nu*nu),nu/(1-nu));
    }
    
    /**
     * 
     * @param E elastic module
     * @param nu Poisson's ratio
     * @return Plane Stress
     */
    public static DenseMatrix getPlaneStress(double E,double nu){
        double factor=E/(1-nu*nu);
        DenseMatrix result=new DenseMatrix(3,3);
        result.set(0,0,factor);
        result.set(1,1,factor);
        result.set(2,2,(1-nu)/2*factor);
        result.set(0,1,nu*factor);
        result.set(1,0,nu*factor);
        return result;
    }
}
