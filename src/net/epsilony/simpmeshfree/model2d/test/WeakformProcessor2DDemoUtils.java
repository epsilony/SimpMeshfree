/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.test;

import java.util.ArrayList;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.model2d.*;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D.MLSFactory;
import net.epsilony.utils.math.EquationSolver;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.MatrixUtils;
import no.uib.cipr.matrix.DenseMatrix;

/**
 *
 * @author epsilon
 */
public class WeakformProcessor2DDemoUtils {


    public static WeakformProcessor weakformProcessor(GeomUtils geomUtils,int baseOrder, int minNdNum,double initRad, DenseMatrix constitutiveLaw, double penalty, WeakformProblem workProblem, boolean iterativeServer, boolean isSimpAsm) {
        MLSFactory mlsFactory = ShapeFunctions2D.createMLSFactory(geomUtils, minNdNum, initRad);
        mlsFactory.setComplete2DPolynomialBasesFactory(baseOrder);
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
        return new WeakformProcessor(mlsFactory, assemblier, workProblem, eqSolver);
    }

    public static WeakformProcessor timoshenkoBeam(double width, double height, double P, double E, double v, double lineSize, double spaceNdsDis, double penalty, Pipe pipe) {
        return timoshenkoBeam(width, height, P, E, v, lineSize, spaceNdsDis, penalty, pipe, false,false);
    }

    public static WeakformProcessor timoshenkoBeam(double width, double height, double P, double E, double v, double lineSize, double spaceNdsDis, double penalty, Pipe pipe, boolean iterativeSolver,boolean isSimpAsm) {
        int power = 4;
        int baseOrder = 2;
        int minNdNum = 15;
        double initRad=(Math.sqrt(minNdNum)-1)*lineSize;
        DenseMatrix conLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        WeightFunctionCore coreFun = new WeightFunctionCores.TriSpline();
        RectangleModel rectModel = new RectangleModel(width, height, lineSize, spaceNdsDis);
        ArrayList<LineBoundary> bnds = rectModel.boundaries();
        GeomUtils geomUtils = new GeomUtils(bnds, rectModel.spaceNodes(), 2);
//        WeakformProblem workProblem = new WeakformProblems2D.TimoshenkoExactBeamProblem(width, height, E, v, P, bnds, null, rectModel.quadrangles(), power);
        WeakformProblem workProblem = new WeakformProblems2D.TimoshenkoExactBeamProblem(width, height, E, v, P, bnds, rectModel.triangles(),null, power);

        if (null != pipe) {
            pipe.set(conLaw, coreFun, rectModel, geomUtils, workProblem);
        }
        return weakformProcessor(geomUtils, baseOrder, minNdNum, initRad, conLaw, penalty, workProblem,iterativeSolver,isSimpAsm);
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
        WeakformProcessor processor = timoshenkoBeam(pipe);
        processor.process();
        processor.solveEquation();

    }
}
