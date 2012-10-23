/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import java.util.ArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.model.InfluenceDomainSizer;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.utils.CommonUtils;
import net.epsilony.simpmeshfree.utils.PartDiffOrdered;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.GeometryMath;
import no.uib.cipr.matrix.BandLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;

/**
 *
 * @author epsilon
 */
public class RBF implements PartDiffOrdered{

    ArrayList<Node> nodes;
    InfluenceDomainSizer infSizer;
    WeightFunction radialBase;
    int diffOrder;
    int dim = 2;
    boolean compactlySupported;
    DenseMatrix A;
    DenseVector a;
    DenseVector u;
    DenseVector[] phis;
    double[] distSqsCache;
    double[] radialBaseCache;
    private int baseLen;
    DistanceSquareFunctionCore commonDistSqFunCore;
    
    
    @Override
    public void setDiffOrder(int order) {
        this.diffOrder=order;
        radialBase.setDiffOrder(order);
        baseLen=CommonUtils.lenBase(dim, order);
        commonDistSqFunCore.setDiffOrder(order);
        distSqsCache=new double[baseLen];
        radialBaseCache=new double[baseLen];
    }

    @Override
    public int getDiffOrder() {
        return diffOrder;
    }

    class CS {

        BandLU bandedALU, bandedA_xLU, bandedA_yLU, bandedA_zLU;
        int[][] bandAPerms, bandA_xPerms, bandB_yPerms, bandB_zPerms;
        //        bandAPerms = RcmMatrixSolver.rcm(matA, RcmMatrixSolver.UNSYMMETRIC);
//        BandMatrix bandedMatA = RcmMatrixSolver.getBandedMatrixByRcmResult(matA, bandAPerms);
//        bandedALU = new BandLU(bandedMatA.numRows(), bandedMatA.numSubDiagonals(), bandedMatA.numSubDiagonals());
//        bandedALU.factor(bandedMatA, true);
    }

    public void RBF(int dim, List<Node> nodes,InfluenceDomainSizer infSizer,WeightFunctionCore radialBaseCore) {
        this.dim=dim;
        this.infSizer=infSizer;
        commonDistSqFunCore=DistanceSquareFunctionCores.common(dim);
        this.nodes=new ArrayList<>(nodes);
        
        radialBase=WeightFunctions.weightFunction(radialBaseCore, dim);
        A = new DenseMatrix(nodes.size(), nodes.size());

        int sz=1;
        radialBase.setDiffOrder(0);
        distSqsCache=new double[sz];
        double[] radialBaseResult = new double[sz];

        for (int i = 0; i < nodes.size(); i++) {
            Node pt = nodes.get(i);
            for (int j = 0; j < nodes.size(); j++) {
                Node nd = nodes.get(j);
                double infRad = infSizer.getSize(nd);
                distSqsCache[0] = GeometryMath.distanceSquare(nd, pt);
                radialBase.value(distSqsCache, infRad, radialBaseResult);                
                    A.set(i, j, radialBaseResult[0]);
            }
        }
        a=(DenseVector) A.solve(u, new DenseVector(nodes.size()));
    }
    
    double[] initOutput(double[] ori){
        if(null==ori){
            return new double[baseLen];
        }
        return ori;
    }
    
    
    double[] values(Coordinate pt,double[] output){
        output=initOutput(output);
        for(int i=0;i<nodes.size();i++){
            Node nd=nodes.get(i);
            commonDistSqFunCore.value(pt, nd, distSqsCache);
            double infRad=infSizer.getSize(nd);
            radialBase.value(distSqsCache, infRad, radialBaseCache);
            for(int bs=0;bs<baseLen;bs++){
                phis[bs].set(bs,radialBaseCache[bs]);
            }
        }
        
        for(int bs=0;bs<baseLen;bs++){
            output[bs]=phis[bs].dot(a);
        }
        return output;
    }
}
