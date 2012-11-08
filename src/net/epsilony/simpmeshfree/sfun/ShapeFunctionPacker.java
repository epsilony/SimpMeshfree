/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.SupportDomainCritierion;
import net.epsilony.spfun.CommonUtils;
import net.epsilony.spfun.InfluenceDomainSizer;
import net.epsilony.spfun.InfluenceDomainSizers;
import net.epsilony.spfun.ShapeFunction;
import net.epsilony.utils.PartDiffOrdered;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Node;

/**
 *
 * @author epsilon
 */
public class ShapeFunctionPacker implements PartDiffOrdered{
    public ShapeFunction shapeFun;
    public SupportDomainCritierion critierion;
    public InfluenceDomainSizer infSizer;
    TDoubleArrayList infRads;
    TDoubleArrayList[] distSqs,shapeFunVals;
    public final int dim;
    private final int maxSupportNodesGuess;
    private int diffOrder;

    public ShapeFunctionPacker(ShapeFunction shapeFun, SupportDomainCritierion critierion, InfluenceDomainSizer infSizer, int dim,int maxSupportNodesGuess) {
        this.shapeFun = shapeFun;
        this.critierion = critierion;
        this.infSizer = infSizer;
        this.dim = dim;
        this.maxSupportNodesGuess = maxSupportNodesGuess;
        init();
    }
    
    private void init(){
        infRads=new TDoubleArrayList(maxSupportNodesGuess);
        int base=CommonUtils.lenBase(dim, getDiffOrder());
        distSqs=new TDoubleArrayList[base];
        shapeFunVals=new TDoubleArrayList[base];
        for(int i=0;i<base;i++){
            distSqs[i]=new TDoubleArrayList(maxSupportNodesGuess);
            shapeFunVals[i]=new TDoubleArrayList(maxSupportNodesGuess);
        }
    }
    
    
    public TDoubleArrayList[] values(Coordinate point,Boundary bnd,List<Node> nodesOutput){
            critierion.getSupports(point, bnd, nodesOutput, distSqs);
            InfluenceDomainSizers.getInfRadius(nodesOutput, infSizer, infRads);
            shapeFun.values(point, nodesOutput, distSqs, infRads, shapeFunVals);
            return shapeFunVals;
    }

    @Override
    public void setDiffOrder(int order) {
        this.diffOrder=order;
        shapeFun.setDiffOrder(order);
        critierion.setDiffOrder(order);
        init();
    }

    @Override
    public int getDiffOrder() {
        return diffOrder;
    }
}
