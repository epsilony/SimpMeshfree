/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.test;

import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.model2d.*;
import net.epsilony.simpmeshfree.model2d.WeakformAssembliers2D.SimpAssemblier;
import net.epsilony.simpmeshfree.utils.Complete2DPolynomialBase;
import net.epsilony.simpmeshfree.utils.CoordinatePartDiffArrayFunction;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.EquationSolvers.FlexCompRowMatrixSolver;
import net.epsilony.utils.math.MatrixUtils;
import no.uib.cipr.matrix.DenseMatrix;

/**
 *
 * @author epsilon
 */
public class WeakformProcessor2DDemoUtils {
    public static class MLSShapeFunctionFactory implements ShapeFunctionFactory{
        GeomUtils geomUtils;
        WeightFunctionCore coreFun;
        int baseOrder;
        int minNdNum,maxNdNum;
        private Object[] genArgs(){       
            SupportDomainCritierion critierion=geomUtils.new VisibleCritieron(minNdNum,maxNdNum);
            WeightFunction weightFunction=WeightFunctions.factory(coreFun, critierion.getDistanceSquareFunction());
            CoordinatePartDiffArrayFunction baseFun=Complete2DPolynomialBase.complete2DPolynomialBase(baseOrder);
            Object[] results=new Object[]{weightFunction,baseFun,critierion};
            return results;
        }

        public MLSShapeFunctionFactory(GeomUtils geomUtils, WeightFunctionCore coreFun, int baseOrder, int minNdNum, int maxNdNum) {
            this.geomUtils = geomUtils;
            this.coreFun = coreFun;
            this.baseOrder = baseOrder;
            this.minNdNum = minNdNum;
            this.maxNdNum = maxNdNum;
        }
        
        
        @Override
        public ShapeFunction factory() {
            Object[] args=genArgs();
            return new ShapeFunctions2D.MLS((WeightFunction)args[0],(CoordinatePartDiffArrayFunction)args[1],(SupportDomainCritierion)args[2]);
        }
    }
    
    public static MLSShapeFunctionFactory genShapeFunctionFactory(GeomUtils geomUtils, WeightFunctionCore coreFunc, int baseOrder, int minNdNum,int maxNdNum){
        return new MLSShapeFunctionFactory(geomUtils, coreFunc, baseOrder, minNdNum, maxNdNum);
    }
    
    public static WeakformProcessor2D weakformProcessor(GeomUtils geomUtils, WeightFunctionCore coreFunc, int baseOrder, int minNdNum,int maxNdNum,DenseMatrix constitutiveLaw,double penalty,WeakformProblem workProblem){
        MLSShapeFunctionFactory shapeFunFactory = genShapeFunctionFactory(geomUtils, coreFunc, baseOrder, minNdNum, maxNdNum);
        int ndsSize=geomUtils.allNodes.size();
        SimpAssemblier assemblier = new WeakformAssembliers2D.SimpAssemblier(constitutiveLaw, penalty, ndsSize);
        FlexCompRowMatrixSolver eqSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.SPD);
        return new WeakformProcessor2D(shapeFunFactory, assemblier,workProblem,eqSolver);
    }
    
    public static WeakformProcessor2D timoshenkoBeam(double width,double height,double P,double E,double v,double lineSize,double spaceNdsDis,double penalty){
        int power=2;
        int baseOrder=2;
        int minNdNum=15;
        int maxNdNum=20;
        DenseMatrix conLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        WeightFunctionCore coreFun=new WeightFunctionCores.TriSpline();
        RectangleModel rectModel=new RectangleModel(width, height, lineSize, spaceNdsDis);
        ArrayList<LineBoundary> bnds = rectModel.boundaries();
        GeomUtils geomUtils=new GeomUtils(bnds, rectModel.spaceNodes(), 2);
        WeakformProblem workProblem=new WeakformProblems2D.TimoshenkoExactBeamProblem(width, height, E, v, P, bnds, null, rectModel.quadrangles(), power);
        return weakformProcessor(geomUtils, coreFun, baseOrder, minNdNum, maxNdNum, conLaw, penalty, workProblem);
    }
    
    public static WeakformProcessor2D timoshenkoBeam(){
        return timoshenkoBeam(48, 12, -1000, 3e7, 0.3, 2, 2, 1e8);
    }
    
    public static void main(String[] args) {
        WeakformProcessor2D processor=timoshenkoBeam();
        processor.process(1);
        
    }
}
