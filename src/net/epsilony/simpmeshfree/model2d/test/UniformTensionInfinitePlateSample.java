/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d.test;

import net.epsilony.simpmeshfree.model.GeomUtils;
import net.epsilony.simpmeshfree.model.SupportDomainCritierion;
import net.epsilony.simpmeshfree.model.WeakformAssemblier;
import net.epsilony.simpmeshfree.model.WeakformProblem;
import net.epsilony.simpmeshfree.model.WeakformProcessor;
import net.epsilony.simpmeshfree.model2d.ConstitutiveLaws2D;
import net.epsilony.simpmeshfree.model2d.LagrangeAssemblier2D;
import net.epsilony.simpmeshfree.model2d.SimpAssemblier2D;
import net.epsilony.simpmeshfree.model2d.UniformTensionInfinitePlate;
import net.epsilony.simpmeshfree.sfun2d.MLS;
import net.epsilony.utils.SomeFactory;
import net.epsilony.utils.math.EquationSolver;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.MatrixUtils;
import net.epsilony.utils.spfun.InfluenceDomainSizer;
import net.epsilony.utils.spfun.InfluenceDomainSizers;
import net.epsilony.utils.spfun.ShapeFunction;
import no.uib.cipr.matrix.DenseMatrix;

/**
 *
 * @author epsilon
 */
public class UniformTensionInfinitePlateSample {

    public static UniformTensionInfinitePlate simpSample() {
        return new UniformTensionInfinitePlate(30, 100, 2000, 200, 0.3, 15, 10, 1.1);
    }

    public static WeakformProcessor genSampleProcessor() {
        boolean isSimpAsm = false;
        boolean iterativeServer = false;
        int baseOrder = 2;
        int power = 4;
        final int minNdNum=15;
        final int initRad=30;
        UniformTensionInfinitePlate utip = simpSample();
        double penalty = 1e8;
        DenseMatrix constitutiveLaw = ConstitutiveLaws2D.getPlaneStress(utip.getE(), utip.getMu());
        WeakformProblem workProblem = utip.getWeakformProblem(power);


        final GeomUtils geomUtils = new GeomUtils(utip.getBoundaries(), utip.getSpaceNodes(), 2);
        SomeFactory<ShapeFunction> mlsFactory = MLS.genFactory(baseOrder);


        int ndsSize = geomUtils.allNodes.size();
        WeakformAssemblier assemblier;
        if (isSimpAsm) {
            assemblier = new SimpAssemblier2D(constitutiveLaw, penalty, ndsSize);
        } else {
            assemblier = new LagrangeAssemblier2D(constitutiveLaw, ndsSize, workProblem.dirichletNodes().size());
        }
        EquationSolver eqSolver;
        if (iterativeServer) {
            eqSolver = new EquationSolvers.SparseIterative(true);
        } else {
            eqSolver = new EquationSolvers.FlexCompRowBand(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
        }


        final SomeFactory<InfluenceDomainSizer> infSizerFactory = new SomeFactory<InfluenceDomainSizer>() {
            @Override
            public InfluenceDomainSizer produce() {
                return InfluenceDomainSizers.bySupportDomainSizer(geomUtils.allNodes, geomUtils.new NearestKVisibleDomainSizer(minNdNum, initRad));
            }
        };
        
        final SomeFactory<SupportDomainCritierion> critierionFactory = new SomeFactory<SupportDomainCritierion>() {
            @Override
            public SupportDomainCritierion produce() {
                return geomUtils.new VisibleCritieron(infSizerFactory.produce());
            }
        };


        return new WeakformProcessor(critierionFactory, infSizerFactory, mlsFactory, assemblier, workProblem, eqSolver);
    }
}
