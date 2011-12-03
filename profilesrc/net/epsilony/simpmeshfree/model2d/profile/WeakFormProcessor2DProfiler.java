/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.profile;

import net.epsilony.math.util.EquationSolver;
import net.epsilony.math.util.EquationSolvers;
import net.epsilony.math.util.MatrixUtils;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizers;
import net.epsilony.simpmeshfree.model.ShapeFunction;
import net.epsilony.simpmeshfree.model.ShapeFunctionFactory;
import net.epsilony.simpmeshfree.model.WeakFormAssemblier;
import net.epsilony.simpmeshfree.model.WeakFormProblem;
import net.epsilony.simpmeshfree.model2d.BoundaryBasedCriterions2D;
import net.epsilony.simpmeshfree.model2d.ConstitutiveLaws2D;
import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
import net.epsilony.simpmeshfree.model2d.WeakFormAssembliers2D;
import net.epsilony.simpmeshfree.model2d.WeakFormProblems2D;
import net.epsilony.simpmeshfree.model2d.WeakFormProcessor2D;
import net.epsilony.simpmeshfree.model2d.WeightFunctions2D;
import net.epsilony.simpmeshfree.utils.BivariateArrayFunction;
import net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial;

/**
 *
 * @author epsilon
 */
public class WeakFormProcessor2DProfiler {
    /**
     * not for accuracy but speed
     */
    
    public void testHeavyAssemblyTimoshenkoBeam() {
        
        double nodesGap = 2;
        final double supportDomainRadiu = 6;
        double width = 480;
        double height = 120;
        double E = 3e7;
        double v = 0.3;
        double P = 100000;

        final WeakFormProblem workProblem = WeakFormProblems2D.timoshenkoCantilevel(nodesGap, supportDomainRadiu, width, height, E, v, P);
        ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
            
            @Override
            public ShapeFunction factory() {
                return new ShapeFunctions2D.MLS(
                        new WeightFunctions2D.SimpPower(2),
                        new BivariateArrayFunction[]{
                            BivariateCompletePolynomial.factory(2),
                            BivariateCompletePolynomial.partialXFactory(2),
                            BivariateCompletePolynomial.partialYFactory(2)
                        },
                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
            }
        };

        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);

        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.SimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());

        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);

        weakFormProcessor2D.process();

    }
    
    public void testHeavyAssemblyTimoshenkoBeamWithApacheSparse() {
        
        double nodesGap = 2;
        final double supportDomainRadiu = 6;
        double width = 480;
        double height = 120;
        double E = 3e7;
        double v = 0.3;
        double P = 100000;

        final WeakFormProblem workProblem = WeakFormProblems2D.timoshenkoCantilevel(nodesGap, supportDomainRadiu, width, height, E, v, P);
        ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
            
            @Override
            public ShapeFunction factory() {
                return new ShapeFunctions2D.MLS(
                        new WeightFunctions2D.SimpPower(2),
                        new BivariateArrayFunction[]{
                            BivariateCompletePolynomial.factory(2),
                            BivariateCompletePolynomial.partialXFactory(2),
                            BivariateCompletePolynomial.partialYFactory(2)
                        },
                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
            }
        };

        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);

        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.ApacheSpareSimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());

        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);

        weakFormProcessor2D.process();

    }
    
    public static void main(String[] args) {
        WeakFormProcessor2DProfiler profiler=new WeakFormProcessor2DProfiler();
        profiler.testHeavyAssemblyTimoshenkoBeamWithApacheSparse();
        profiler.testHeavyAssemblyTimoshenkoBeam();
        
    }
}
