/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.profile;

import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.model2d.*;
import net.epsilony.simpmeshfree.utils.BivariateArrayFunction;
import net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial;
import net.epsilony.utils.math.EquationSolver;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.MatrixUtils;

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
                throw new UnsupportedOperationException("Not supported yet.");
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
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
                throw new UnsupportedOperationException("Not supported yet.");
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
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
