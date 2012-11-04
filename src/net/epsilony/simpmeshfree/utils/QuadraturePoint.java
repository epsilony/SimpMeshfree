/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.io.Serializable;
import java.util.Arrays;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.utils.WithId;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.quadrature.GaussLegendreQuadratureUtils;
import net.epsilony.utils.math.quadrature.TriangleSymmetricQuadrature;

/**
 * used for quadrature a field not more complex than 3D
 * @author epsilonyuan@gmail.com
 */
public class QuadraturePoint implements WithId,Serializable{
    /**
     * the weight for quadrature</br>
     * when quadrature with the help of {@link TriangleSymmetricQuadrature} 
     * weigth=standard weight*area of triangle domain</br>
     * when quadrature with the help of {@link GaussLegendreQuadratureUtils} 
     * weight=weight_u*weight_v*determinat(Jaccobi((x,y)/(u,v)))
     */
    public double weight; 
    
    /**
     * the coordinate of quadrature point in the common 1D 2D or 3D space
     */
    public Coordinate coordinate=new Coordinate();
    
    public Boundary boundary;
    
    public double[] values=new double[3];
    
    public boolean[] validities=new boolean[3];
    public int id;
    public QuadraturePoint(){
        
    }
    
    public QuadraturePoint(QuadraturePoint qp){
        set(qp,true);
    }
    
    private void set(QuadraturePoint qp,boolean deep){
        if(deep){
           coordinate.set(qp.coordinate);
           values=Arrays.copyOf(values, values.length);
           validities=Arrays.copyOf(validities, validities.length);
        }else{
            coordinate=qp.coordinate;
            values=qp.values;
            validities=qp.validities;
        }
        boundary=qp.boundary;
        id=qp.id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }
}
