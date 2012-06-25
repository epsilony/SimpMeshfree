/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.test;

import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.model2d.*;
import net.epsilony.simpmeshfree.utils.BasesFunction;
import net.epsilony.simpmeshfree.utils.Complete2DPolynomialBases;
import net.epsilony.utils.math.EquationSolver;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.MatrixUtils;
import no.uib.cipr.matrix.DenseMatrix;

/**
 *
 * @author epsilon
 */
public class WeakformProcessor2DDemoUtils {

    public static class MLSShapeFunctionFactory implements ShapeFunctionFactory {

        GeomUtils geomUtils;
        WeightFunctionCore coreFun;
        int baseOrder;
        int minNdNum, maxNdNum;

        private Object[] genArgs() {
            SupportDomainCritierion critierion = geomUtils.new VisibleCritieron(minNdNum, maxNdNum);
//            SupportDomainCritierion critierion = SupportDomainUtils.simpCriterion(4.5, geomUtils.allNodes);
            WeightFunction weightFunction = WeightFunctions.factory(coreFun.avatorInstance(), critierion.getDistanceSquareFunction());
            BasesFunction baseFun = Complete2DPolynomialBases.complete2DPolynomialBase(baseOrder);
            Object[] results = new Object[]{weightFunction, baseFun, critierion};
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
            Object[] args = genArgs();
            return new ShapeFunctions2D.MLS((WeightFunction) args[0], (BasesFunction) args[1], (SupportDomainCritierion) args[2]);
        }
    }

    public static MLSShapeFunctionFactory genShapeFunctionFactory(GeomUtils geomUtils, WeightFunctionCore coreFunc, int baseOrder, int minNdNum, int maxNdNum) {
        return new MLSShapeFunctionFactory(geomUtils, coreFunc, baseOrder, minNdNum, maxNdNum);
    }

    public static WeakformProcessor weakformProcessor(GeomUtils geomUtils, WeightFunctionCore coreFunc, int baseOrder, int minNdNum, int maxNdNum, DenseMatrix constitutiveLaw, double penalty, WeakformProblem workProblem) {
        return weakformProcessor(geomUtils, coreFunc, baseOrder, minNdNum, maxNdNum, constitutiveLaw, penalty, workProblem, false,false);
    }

    public static WeakformProcessor weakformProcessor(GeomUtils geomUtils, WeightFunctionCore coreFunc, int baseOrder, int minNdNum, int maxNdNum, DenseMatrix constitutiveLaw, double penalty, WeakformProblem workProblem, boolean iterativeServer, boolean isSimpAsm) {
        MLSShapeFunctionFactory shapeFunFactory = genShapeFunctionFactory(geomUtils, coreFunc, baseOrder, minNdNum, maxNdNum);
        int ndsSize = geomUtils.allNodes.size();
        WeakformAssemblier assemblier;
        if (isSimpAsm) {
            assemblier = new WeakformAssembliers2D.Simp(constitutiveLaw, penalty, ndsSize);
        } else {
            assemblier = new WeakformAssembliers2D.Lagrange(constitutiveLaw, ndsSize, workProblem.dirichletNodes().size());
        }
        EquationSolver eqSolver;
        if (iterativeServer) {
            eqSolver = new EquationSolvers.SparseIterative(true);
        } else {
            eqSolver = new EquationSolvers.FlexCompRowBand(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
        }
        return new WeakformProcessor(shapeFunFactory, assemblier, workProblem, eqSolver);
    }

    public static WeakformProcessor timoshenkoBeam(double width, double height, double P, double E, double v, double lineSize, double spaceNdsDis, double penalty, Pipe pipe) {
        return timoshenkoBeam(width, height, P, E, v, lineSize, spaceNdsDis, penalty, pipe, false,false);
    }

    public static WeakformProcessor timoshenkoBeam(double width, double height, double P, double E, double v, double lineSize, double spaceNdsDis, double penalty, Pipe pipe, boolean iterativeSolver,boolean isSimpAsm) {
        int power = 4;
        int baseOrder = 2;
        int minNdNum = 15;
        int maxNdNum = 20;
        DenseMatrix conLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        WeightFunctionCore coreFun = new WeightFunctionCores.TriSpline();
        RectangleModel rectModel = new RectangleModel(width, height, lineSize, spaceNdsDis);
        ArrayList<LineBoundary> bnds = rectModel.boundaries();
        GeomUtils geomUtils = new GeomUtils(bnds, rectModel.spaceNodes(), 2);
        WeakformProblem workProblem = new WeakformProblems2D.TimoshenkoExactBeamProblem(width, height, E, v, P, bnds, null, rectModel.quadrangles(), power);
        if (null != pipe) {
            pipe.set(conLaw, coreFun, rectModel, geomUtils, workProblem);
        }
        return weakformProcessor(geomUtils, coreFun, baseOrder, minNdNum, maxNdNum, conLaw, penalty, workProblem,iterativeSolver,isSimpAsm);
    }

    public static WeakformProcessor timoshenkoBeam(Pipe pipe, boolean iterativeSolver,boolean isSimpAsm) {
        return timoshenkoBeam(48, 12, -1000, 3e7, 0.3, 2, 2, 3e7 * 1e7, pipe, iterativeSolver,isSimpAsm);
    }

    public static WeakformProcessor timoshenkoBeam(Pipe pipe) {
        return timoshenkoBeam(pipe, false,false);
    }

    public static class Pipe {

        DenseMatrix conLaw;
        WeightFunctionCore coreFun;
        RectangleModel rectModel;
        GeomUtils geomUtils;
        WeakformProblem workProblem;

        public final void set(DenseMatrix conLaw, WeightFunctionCore coreFun, RectangleModel rectModel, GeomUtils geomUtils, WeakformProblem workProblem) {
            this.conLaw = conLaw;
            this.coreFun = coreFun;
            this.rectModel = rectModel;
            this.geomUtils = geomUtils;
            this.workProblem = workProblem;
        }

        public Pipe(DenseMatrix conLaw, WeightFunctionCore coreFun, RectangleModel rectModel, GeomUtils geomUtils, WeakformProblem workProblem) {
            set(conLaw, coreFun, rectModel, geomUtils, workProblem);
        }

        public Pipe() {
        }
    }

    public static Pipe newPipe() {
        return new Pipe();
    }

    public static void main(String[] args) {
        Pipe pipe = new Pipe();
        WeakformProcessor processor = timoshenkoBeam(pipe, true,true);
        processor.process();
        processor.solveEquation();

    }
}
