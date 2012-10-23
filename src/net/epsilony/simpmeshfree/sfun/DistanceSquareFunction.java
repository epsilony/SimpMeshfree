/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.utils.PartDiffOrdered;
import net.epsilony.utils.geom.Coordinate;

/**
 * {@code DistanceSquareFunction} is a key component of a {@WeightFunction} implementation. The main idea of setting up such interface
 * comes from the concept of <i>criterion of unconvex support domains</i>, such as {@link http://adsabs.harvard.edu/abs/1996CompM..18..225O transparancy or diffraction criterions}. 
 * The designed pattern of using DistanceSquareFunction is based on adapting {@link SupportDomainCritierion} and {@link WeightFunction}, a schematic example is shown below:
 * <pre>
 * {@code 
 * SupportDomainCriterion criterion=someCriterion;
 * DistanceSquareFunction distFun=criterion.distanceFunction();
 * WeightFunction weightFunction=WeightFunctions.factory(someCoreFunction,distFun);
 * 
 * rad =criterion.setCenter(center,centerBnd,outputNds);    //set center point of distFun here( call distFun.setCenter(center) inside.
 * //distFun.setCenter(center);                            //normally need not directly call setCenter of a DistanceSquareFunction instance
 * TDoubleArrayList[] weightsVals=weightFunction.sqValues(outputNds,null);    //call distFun.sqValues(outputNds,null) inside
 * 
 * }
 * </pre>
 * 
 * For testing or debuging, a minimum pattern for a complete DistanceSquareFunction utilization is shown below:
 * <pre>
 * {@code
 * DistanceSquareFunction dist=new SomeKindOfDistanceFunction(someargs...);
 * dist.setDiffOrder(diffOrder);                                 //line1
 * dist.setCenter(center);                                      //line2
 * TDoubleArrayList[] vals=dist.sqValues(pts,null);             //line3
 * }
 * </pre>
 * For someone who wonder why there is a {@link #sqValues(java.util.List, gnu.trove.list.array.TDoubleArrayList[]) distance square method} but not distance, see also {@link http://epsilony.net/mywiki/SimpMeshfree/DistanceSquareOrNot here}
 * @author epsilon
 */
public interface DistanceSquareFunction extends PartDiffOrdered{
    TDoubleArrayList[] sqValues(List<? extends Coordinate> pts,TDoubleArrayList[] results);
    
    void setCenter(Coordinate center);
}
