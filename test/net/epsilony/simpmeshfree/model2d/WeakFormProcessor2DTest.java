/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;
import net.epsilony.simpmeshfree.model.*;
//import net.epsilony.simpmeshfree.model2d.ui.SimpPanel;
import net.epsilony.simpmeshfree.utils.BivariateArrayFunction;
import net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial;
//import net.epsilony.simpmeshfree.utils.PartDiffOrd;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.math.EquationSolver;
import net.epsilony.utils.math.EquationSolvers;
import net.epsilony.utils.math.MatrixUtils;
import no.uib.cipr.matrix.DenseVector;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 目前，该算例还没有使用标准的assert机制
 * @author epsilonyuan@gmail.com
 */
public class WeakFormProcessor2DTest {
//1
//    public WeakFormProcessor2DTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    public static class TempWeightFunction implements WeightFunction {
//
//        double supportRad;
//        double supportRadSquare;
//
//        public TempWeightFunction(double supportRad) {
//            this.supportRad = supportRad;
//            this.supportRadSquare = supportRad * supportRad;
//        }
//
//        @Override
//        public double[] values(Node node, Coordinate point, double supportRad, double[] results) {
//            Coordinate coord = node.coordinate;
//            double xn = coord.x;
//            double yn = coord.y;
//            double x = point.x;
//            double y = point.y;
//            double dx = x - xn;
//            double dy = y - yn;
//            double rSq = dx * dx + dy * dy;
//            double srs = supportRadSquare;
//            switch (opt) {
//                case 1:
//                    if (rSq >= srs) {
//                        results[0] = 0;
//                    } else {
//                        double t = (rSq / srs - 1);
//                        results[0] = t * t;
//                    }
//                    break;
//
//                case 7:
//                    if (rSq >= srs) {
//                        results[oriI] = 0;
//                        results[xI] = 0;
//                        results[yI] = 0;
//                    } else {
//                        double t = (rSq / srs - 1);
//                        results[oriI] = t * t;
//                        results[xI] = 4 * t * dx / srs;
//                        results[yI] = 4 * t * dy / srs;
//                    }
//                    break;
//                default:
//                    throw new IllegalArgumentException();
//            }
//
//
//            return results;
//        }
//        int oriI = -1;
//        int xI = -1;
//        int yI = -1;
//        int opt;
//
//        @Override
//        public void setOrders(PartDiffOrd[] types) {
//            oriI = -1;
//            xI = -1;
//            yI = -1;
//            for (int i = 0; i < types.length; i++) {
//                PartDiffOrd type = types[i];
//                switch (type.sumOrder()) {
//                    case 0:
//                        oriI = i;
//                        break;
//                    case 1:
//                        switch (type.respectDimension(0)) {
//                            case 0:
//                                xI = i;
//                                break;
//                            case 1:
//                                yI = i;
//                                break;
//                            default:
//                                throw new IllegalArgumentException("Dimension Index over Range exp <=1");
//                        }
//                        break;
//                    default:
//                        throw new IllegalArgumentException("Partial oder over range exp: <=1");
//                }
//            }
//
//            opt = 0;
//            if (oriI != -1) {
//                opt += 1;
//            }
//            if (xI != -1) {
//                opt += 2;
//            }
//
//            if (yI != -1) {
//                opt += 4;
//            }
//        }
//    }
//
//    /**
//     * the test has very high accuracy as Relative error is less than 1%, absolute error is less than 4e-6;
//     */
//    @Test
//    public void testTimoshenkoBeam() {
//        double releps = 0.01; //relative error assert can be approved
//        double abeps = 4e-6;  //absolute
//
//        double nodesGap = 2;
//        final double supportDomainRadiu = 6;
//        double width = 48;
//        double height = 12;
//        double E = 3e7;
//        double v = 0.3;
//        double P = 1000;
//
//        final WeakFormProblem workProblem = WeakFormProblems2D.timoshenkoCantilevel(nodesGap, supportDomainRadiu, width, height, E, v, P);
//        ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
//            
//            @Override
//            public ShapeFunction factory() {
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
//            }
//        };
//
//        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
//
//        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.SimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());
//
//        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);
//
//        weakFormProcessor2D.process();
//
//        weakFormProcessor2D.solveEquation();
//
//        DenseVector result = weakFormProcessor2D.getNodesResult();
//
//        TimoshenkoExactBeam exact = new TimoshenkoExactBeam(width, height, E, v, P);
//
//        List<Node> nodes = workProblem.getNodes();
//
//        Collections.sort(nodes, new Comparator<Node>() {
//
//            @Override
//            public int compare(Node o1, Node o2) {
//                double t = o1.coordinate.x - o2.coordinate.x;
//                if (t < 0) {
//                    return -1;
//                }
//                if (t > 0) {
//                    return 1;
//                }
//                return (int) Math.signum(o1.coordinate.y - o2.coordinate.y);
//            }
//        });
//
//        LinkedList<Coordinate> coords = new LinkedList<>();
//        for (Node nd : nodes) {
//            coords.add(nd.coordinate);
//        }
//        List<double[]> resultsAtNodes = weakFormProcessor2D.result(coords);
//
//        Iterator<double[]> resultsIter = resultsAtNodes.iterator();
//
//        for (Node nd : nodes) {
//            double x = nd.coordinate.x;
//            double y = nd.coordinate.y;
//            double[] actResult = resultsIter.next();
//            double[] expU = exact.getDisplacement(x, y, new double[2]);
//            System.out.println(String.format("(% 5f,% 5f):n(% 5f,% 5f):act(% 5f,% 5f):exp(% 5f,% 5f)", x, y, result.get(nd.id * 2), result.get(nd.id * 2 + 1), actResult[0], actResult[1], expU[0], expU[1]));
//            assertEquals(expU[0], actResult[0], (expU[0] * releps > abeps ? expU[0] * releps : abeps));
//            assertEquals(expU[1], actResult[1], (expU[1] * releps > abeps ? expU[1] * releps : abeps));
//        }
//    }
//    
//    
//
////    @Test
//    public void testTensionBar() {
//        double nodesGap = 2;
//        final double supportDomainRadiu = 6;
//        double width = 100;
//        double height = 8;
//        double E = 200e7;
//        double v = 0.3;
//        double P = 1000;
//        final WeakFormProblem workProblem = WeakFormProblems2D.tensionBarHorizontal(nodesGap, supportDomainRadiu, width, height, E, v, P);
//
//        ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
//            
//            @Override
//            public ShapeFunction factory() {
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
//            }
//        };
//
//        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
//
//        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.SimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());
//
//        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);
//
//        weakFormProcessor2D.process();
//
//        weakFormProcessor2D.solveEquation();
//
//        List<Node> nodes = workProblem.getNodes();
//        Collections.sort(nodes, new Comparator<Node>() {
//
//            @Override
//            public int compare(Node o1, Node o2) {
//                double t = o1.coordinate.x - o2.coordinate.x;
//                if (t < 0) {
//                    return -1;
//                }
//                if (t > 0) {
//                    return 1;
//                }
//                return (int) Math.signum(o1.coordinate.y - o2.coordinate.y);
//
//            }
//        });
//        LinkedList<Coordinate> coords = new LinkedList<>();
//        for (Node nd : nodes) {
//            coords.add(nd.coordinate);
//        }
//        List<double[]> resultsAtNodes = weakFormProcessor2D.result(coords);
//
//        Iterator<double[]> resultsIter = resultsAtNodes.iterator();
//        for (Node nd : nodes) {
//            double x = nd.coordinate.x;
//            double y = nd.coordinate.y;
//            double[] actResult = resultsIter.next();
//            System.out.println(String.format("(% 3.0f,% 3.0f):act(%5e,%5e)", x, y, actResult[0], actResult[1]));
//        }
//    }
//
////    @Test
//    public void testTensionBar2() {
//        double nodesGap = 2;
//        final double supportDomainRadiu = 6;
//        double width = 8;
//        double height = 100;
//        double E = 200e7;
//        double v = 0.3;
//        double P = 1000;
//        final WeakFormProblem workProblem = WeakFormProblems2D.tensionBarVertical(nodesGap, supportDomainRadiu, width, height, E, v, P);
//
//           ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
//            
//            @Override
//            public ShapeFunction factory() {
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
//            }
//        };
//
//        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
//
//        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.SimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());
//
//        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);
//
//        weakFormProcessor2D.process();
//
//        weakFormProcessor2D.solveEquation();
//
//        List<Node> nodes = workProblem.getNodes();
//        Collections.sort(nodes, new Comparator<Node>() {
//
//            @Override
//            public int compare(Node o1, Node o2) {
//                double t = o1.coordinate.y - o2.coordinate.y;
//                if (t < 0) {
//                    return -1;
//                }
//                if (t > 0) {
//                    return 1;
//                }
//                return (int) Math.signum(o1.coordinate.x - o2.coordinate.x);
//
//            }
//        });
//
//
//        LinkedList<Coordinate> coords = new LinkedList<>();
//        for (Node nd : nodes) {
//            coords.add(nd.coordinate);
//        }
//
//
//        List<double[]> resultsAtNodes = weakFormProcessor2D.result(coords);
//
//        Iterator<double[]> resultsIter = resultsAtNodes.iterator();
//        for (Node nd : nodes) {
//            double x = nd.coordinate.x;
//            double y = nd.coordinate.y;
//            double[] actResult = resultsIter.next();
//            System.out.println(String.format("(% 3.0f,% 3.0f):act(%5e,%5e)", x, y, actResult[0], actResult[1]));
//        }
//    }
//
////    @Test
//    public void testDisplacementTensionBar() {
//        boolean visible = false;  //设为true则可见位移图   
//
//        double nodesGap = 10;
//        final double supportDomainRadiu = 25;
//        double width = 100;
//        double height = 40;
//        double E = 200;
//        double v = 0.3;
//        double displace = 10;
//        final WeakFormProblem workProblem = WeakFormProblems2D.displacementTensionBar(nodesGap, supportDomainRadiu, width, height, E, v, displace);
//
//         ShapeFunctionFactory shapeFunFactory = new ShapeFunctionFactory() {
//            
//            @Override
//            public ShapeFunction factory() {
//                return new ShapeFunctions2D.MLS(
//                        new WeightFunctions2D.SimpPower(2),
//                        new BivariateArrayFunction[]{
//                            BivariateCompletePolynomial.factory(2),
//                            BivariateCompletePolynomial.partialXFactory(2),
//                            BivariateCompletePolynomial.partialYFactory(2)
//                        },
//                        new BoundaryBasedCriterions2D.Visible(workProblem.nodeSupportDomainSizer()),
//                        new NodeSupportDomainSizers.ConstantSizer(supportDomainRadiu));
//            }
//        };
//
//        EquationSolver equationSolver = new EquationSolvers.FlexCompRowMatrixSolver(MatrixUtils.UNSYMMETRICAL_BUT_MIRROR_FROM_UP_HALF);
//
//        WeakFormAssemblier weakFromAssemblier = new WeakFormAssembliers2D.SimpAssemblier(ConstitutiveLaws2D.getPlaneStress(E, v), E * 1e7, workProblem.getNodes().size());
//
//        WeakFormProcessor2D weakFormProcessor2D = new WeakFormProcessor2D(shapeFunFactory, weakFromAssemblier, workProblem, 3, equationSolver);
//
//        weakFormProcessor2D.process();
//
//        weakFormProcessor2D.solveEquation();
//
//        List<Node> nodes = workProblem.getNodes();
//        Collections.sort(nodes, new Comparator<Node>() {
//
//            @Override
//            public int compare(Node o1, Node o2) {
//                double t = o1.coordinate.x - o2.coordinate.x;
//                if (t < 0) {
//                    return -1;
//                }
//                if (t > 0) {
//                    return 1;
//                }
//                return (int) Math.signum(o1.coordinate.y - o2.coordinate.y);
//            }
//        });
//
//        final LinkedList<Coordinate> coords = new LinkedList<>();
//        for (Node nd : nodes) {
//            coords.add(nd.coordinate);
//        }
//        final List<double[]> resultsAtNodes = weakFormProcessor2D.result(coords);
//
//        DenseVector result = weakFormProcessor2D.getNodesResult();
//        Iterator<double[]> resultsIter = resultsAtNodes.iterator();
//        for (Node nd : nodes) {
//            double x = nd.coordinate.x;
//            double y = nd.coordinate.y;
//            double[] actResult = resultsIter.next();
//            int index = nd.id * 2;
//
//            System.out.println(String.format("(% 3.5f,% 3.5f):act(% 3.5f,% 3.5f):nd(% 3.5f,% 3.5f)", x, y, actResult[0], actResult[1], result.get(index), result.get(index + 1)));
//        }
//
//        if (visible == false) {
//            return;
//        }
//        resultsIter = resultsAtNodes.iterator();
//
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                JFrame f = new JFrame("displacement tension bar");
//
//                JPanel jPanel = new JPanel();
//                LayerUI<JPanel> layerUI = new SimpPanel(workProblem.getNodes(), workProblem.getBoundaries(), coords, resultsAtNodes);
//
//                JLayer<JPanel> jLayer = new JLayer<>(jPanel, layerUI);
//                f.add(jLayer);
//
//                f.setSize(800, 600);
//                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                f.setLocationRelativeTo(null);
//                f.setVisible(true);
//            }
//        });
//        try {
//            EventQueue.invokeAndWait(new Runnable() {
//
//                @Override
//                public void run() {
//                    edt = Thread.currentThread();
//                }
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            ex.printStackTrace();
//        }
//        try {
//            edt.join();
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
//
//    }
//    public static Thread edt;
}
