/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.GaussLegendreQuadratureUtils;
import net.epsilony.utils.math.TriangleSymmetricQuadrature;

/**
 * used for quadrature a field not more complex than 3D
 * @author epsilonyuan@gmail.com
 */
public class QuadraturePoint{
    

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
    public Coordinate coordinate;
    public QuadraturePoint(){
        coordinate=new Coordinate();
    }
}
