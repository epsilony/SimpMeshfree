/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.utils;

import no.uib.cipr.matrix.UpperSymmDenseMatrix;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class Constitutives {
    public static UpperSymmDenseMatrix planeStressMatrix(double E,double nu){
        double t=E/(1-nu*nu);
        UpperSymmDenseMatrix result=new UpperSymmDenseMatrix(3);
        result.set(0, 0, t);
        result.set(0,1,nu*t);
        result.set(1,1,t);
        result.set(2,2,(1-nu)/2*t);
        return result;
    }
}
