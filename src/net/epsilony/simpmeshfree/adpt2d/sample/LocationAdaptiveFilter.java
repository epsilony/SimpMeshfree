/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d.sample;

import java.util.LinkedList;
import net.epsilony.simpmeshfree.adpt2d.AdaptiveFilter;
import net.epsilony.simpmeshfree.adpt2d.QuadPixcell;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;

/**
 *
 * @author epsilon
 */
public class LocationAdaptiveFilter implements AdaptiveFilter{

    public LinkedList<Coordinate> refinePts=new LinkedList<>();
    public LinkedList<Coordinate[]> mergeQuads=new LinkedList<>();
    
    
    @Override
    public boolean isNeedRefine(QuadPixcell px) {
        Coordinate[] vertes=px.nodes;
        for(Coordinate coord:refinePts){
            if(GeometryMath.isInsideQuadrangle(coord.x, coord.y, vertes)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNeedMerge(QuadPixcell px) {
        for(int i=0;i<px.nodes.length;i++){
            Node nd=px.nodes[i];
            for (Coordinate[] vertes:mergeQuads){
                if(GeometryMath.isInsideQuadrangle(nd.x, nd.y, vertes)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public void addRefinePt(double x,double y){
        refinePts.add(new Coordinate(x, y));
    }
    
    public void addMergeRect(double x0,double y0,double w,double h){
        double[] xys=new double[]{x0,y0,x0+w,y0,x0+w,y0+h,x0,y0+h};
        Coordinate[] vertes=new Coordinate[4];
        for(int i=0;i<vertes.length;i++){
            vertes[i]=new Coordinate(xys[i*2],xys[i*2+1]);
        }
        mergeQuads.add(vertes);
    }
}
