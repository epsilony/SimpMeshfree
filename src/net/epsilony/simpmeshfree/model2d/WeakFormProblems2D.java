/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.utils.BCQuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePoint;
import net.epsilony.simpmeshfree.utils.QuadraturePointIterables;
import net.epsilony.utils.ArrayUtils;
import net.epsilony.utils.CenterSearcher;
import net.epsilony.utils.LayeredRangeTree;
import net.epsilony.utils.geom.Coordinate;
import net.epsilony.utils.geom.Quadrangle;
import net.epsilony.utils.geom.Triangle;
import net.epsilony.utils.math.TriangleSymmetricQuadrature;
import no.uib.cipr.matrix.DenseMatrix;

/**
 * Same standard mechanical sample problems, including</br> <ul> <li>{@link #timoshenkoCantilevel(double, double, double, double, double, double, double)  Timoshenko's exact cantilevel}
 * </li> <li>{@link #tensionBarHorizontal(double, double, double, double, double, double, double) horizontal tension bar}</li> <li>{@link #tensionBarVertical(double, double, double, double, double, double, double) vertical tension bar}</li> <li>{@link #displacementTensionBar(double, double, double, double, double, double, double) tension bar by displacement (Neumann boundary conditions only)}</li>
 * </ul>
 *
 * @author epsilonyuan@gmail.com
 */
public class WeakFormProblems2D {

    private WeakFormProblems2D() {
    }

    /**
     * 根据输入的数组创建一个{@link WeakFormWorkProblem} Only support for these
     * situations:</br> <ul> <li>constant constitutive law</li> <li>constant
     * nodes list</li> <li>distributed continous boundary conditions (not
     * concentrated singluar bcs) </li> <li>triangle and quadrangle quadrature
     * subdomain, for {@link QuadraturePointIterables iterable wrapping}
     * only</li> <li>constant node support domain sizer or a given general
     * support domain sizer</li> </ul>
     *
     * @see NodeSupportDomainSizer
     * @see NodeSupportDomainSizers
     */
    public static class ByArrays implements WeakFormProblem {

        public Node[] nodes;
        double[] NodeSupportDomainRadiums;
        private final double constantDomainRadium;
        LineBoundary[] boundaries;
        Triangle[] triangleQuadratureDomains;
        Quadrangle[] quadrangleQuadratureDomains;
        VolumeCondition volumeCondition;
        BoundaryCondition[] neumannBCs, dirichletBCs;
        LayeredRangeTree<Node> nodesTree;
        LayeredRangeTree<LineBoundary> boundariesTree;
        int boundaryConditionValueDim;
        double longestBoundaryLength;
        double maxNodeSupportDomainRadium;
        DenseMatrix constitutiveLaw;

        /**
         * @param nodes 所有的结点，作用{@link Node#id}不得重复，其值在区间[0,nodes.num())里
         * @param nodeSupportDomainRadiums 用于{@link #nodeSupportDomainSizer()}，若为null则所有的结点支持域半径为constantDomainRadium;
         * 若非null,一个Node nd所对应的支持域半径应为nodesSupportDomainRadiums[nd.id]
         * @param constantDomainRadium 用于{@link #nodeSupportDomainSizer()}，当nodeSupportDomainRadiums==null时起作用。
         * @param boundaries 所有的边界
         * @param triangleQuadratureDomains 用于{@link #volumeIterable(int)
         * }所有的三角形积分子域 可以为null
         * @param quadrangleQuadratureDomains 所有的四边形积分子域 可以为null
         * @param volumeCondition 体积力条件
         * @param neumannBCs 所有的Neumann边界条件
         * @param dirichletBCs 所有的Dirichlet边界条件
         * @param boundaryConditionValueDim 边界条件的维数，对于二维力学:2
         * @param constitutiveLaw 本构模型 Stress=constitutiveLaw*Strain
         */
        public ByArrays(Node[] nodes, double[] nodeSupportDomainRadiums, double constantDomainRadium, LineBoundary[] boundaries, Triangle[] triangleQuadratureDomains, Quadrangle[] quadrangleQuadratureDomains, VolumeCondition volumeCondition, BoundaryCondition[] neumannBCs, BoundaryCondition[] dirichletBCs, int boundaryConditionValueDim, DenseMatrix constitutiveLaw) {
            this.nodes = nodes;
            this.NodeSupportDomainRadiums = nodeSupportDomainRadiums;
            this.constantDomainRadium = constantDomainRadium;
            this.boundaries = boundaries;
            this.triangleQuadratureDomains = triangleQuadratureDomains;
            this.quadrangleQuadratureDomains = quadrangleQuadratureDomains;
            this.volumeCondition = volumeCondition;
            this.neumannBCs = neumannBCs;
            this.dirichletBCs = dirichletBCs;
            this.boundaryConditionValueDim = boundaryConditionValueDim;
            nodesTree = new LayeredRangeTree<>(Arrays.asList(nodes), Arrays.asList(Node.comparatorByDim(0), Node.comparatorByDim(1)));
            boundariesTree = new LayeredRangeTree<>(Arrays.asList(boundaries), Arrays.asList(LineBoundary.comparatorByDim(0), LineBoundary.comparatorByDim(1)));
            longestBoundaryLength = LineBoundary.longestLength(boundaries);
            if (null != nodeSupportDomainRadiums) {
                maxNodeSupportDomainRadium = ArrayUtils.max(nodeSupportDomainRadiums);
            } else {
                maxNodeSupportDomainRadium = constantDomainRadium;
            }
            this.constitutiveLaw = constitutiveLaw;
        }

        @Override
        public Iterable<QuadraturePoint> volumeIterable(int power) {
            LinkedList<Iterable<QuadraturePoint>> iters = new LinkedList<>();
            if (null != triangleQuadratureDomains) {
                iters.add(new QuadraturePointIterables.TriangleArrayIterable(power, triangleQuadratureDomains, false));
            }
            if (null != quadrangleQuadratureDomains) {
                iters.add(new QuadraturePointIterables.QuadrangleArrayIterable(power, quadrangleQuadratureDomains));
            }
            return new QuadraturePointIterables.IterablesWrapper(iters);
        }

        @Override
        public Iterable<BCQuadraturePoint> neumannIterable(int power) {
            return new BCQuadratureIterables2D.ArrayIterable(boundaryConditionValueDim, power, dirichletBCs);
        }

        @Override
        public Iterable<BCQuadraturePoint> dirichletIterable(int power) {
            return new BCQuadratureIterables2D.ArrayIterable(boundaryConditionValueDim, power, neumannBCs);
        }

        @Override
        public NodeSupportDomainSizer nodeSupportDomainSizer() {
            if (null != NodeSupportDomainRadiums) {
                return NodeSupportDomainSizers.arraySizerByRadiums(NodeSupportDomainRadiums);
            } else {
                return new NodeSupportDomainSizers.ConstantSizer(constantDomainRadium);
            }
        }

        @Override
        public List<Node> getNodes() {
            return Arrays.asList(nodes);
        }

        @Override
        public List<Boundary> getBoundaries() {
            List<LineBoundary> list = Arrays.asList(boundaries);
            return new ArrayList<Boundary>(list);
        }

        @Override
        public int dirichletQuadraturePointsNum(int power) {
            int numPoint = (int) Math.ceil((power + 1) / 2.0);
            return dirichletBCs.length * numPoint;
        }

        @Override
        public int neumannQudaraturePointsNum(int power) {
            int numPoint = (int) Math.ceil((power + 1) / 2.0);
            return neumannBCs.length * numPoint;
        }

        @Override
        public int balanceQuadraturePointsNum(int power) {
            int sum = 0;
            int pointNum = (int) Math.ceil((power + 1) / 2.0);
            pointNum *= pointNum;
            if (null != triangleQuadratureDomains) {
                sum += triangleQuadratureDomains.length * TriangleSymmetricQuadrature.getNumPoints(power);
            }
            if (null != quadrangleQuadratureDomains) {
                sum += quadrangleQuadratureDomains.length * pointNum;
            }
            return sum;
        }

        /**
         * a conformal node searcher that can search all the possible nodes of
         * which support domain includes the search center. The search results
         * may contain some nodes that should be filtered later.</br> The
         * backgroup algorithm is a layered range tree.
         *
         * @see LayeredRangeTree
         */
        public class NodeSearcher implements CenterSearcher<Coordinate, Node> {

            Node fromNode = new Node();
            Node toNode = new Node();
            Coordinate from = fromNode;
            Coordinate to = toNode;
            double distance;

            /**
             * @param projection should be the max support domain radiu of all
             * the nodes
             */
            protected NodeSearcher(double distance) {
                this.distance = distance;
            }

            /**
             * @param center the center of the search window
             * @param results in rectangle window with border length 2*(max node
             * support domain radiu) (border inclusive)
             * @return results, all the possible support nodes
             */
            @Override
            public List<Node> search(Coordinate center, List<Node> results) {
                if (null == results) {
                    results = new LinkedList<>();
                }

                double cx = center.x;
                double cy = center.y;
                double dist = distance;
                from.x = cx - dist;
                from.y = cy - dist;
                to.x = cx + dist;
                to.y = cy + dist;
                nodesTree.search(results, fromNode, toNode);
                return results;
            }
        }

        /**
         * a conforming boundary searcher by layered range tree.</br> In fact,
         * it seams that the layered range tree may be not a good choice.
         * Monotone line string based algorithm may be better in most
         * situations.
         *
         */
        public class BoundarySearcher implements CenterSearcher<Coordinate, Boundary> {

            LineBoundary fromBoundary = new LineBoundary();
            LineBoundary toBoundary = new LineBoundary();
            Node from = new Node();
            Node to = new Node();
            double distance;

            public BoundarySearcher(double distance) {

                fromBoundary.end = from;
                fromBoundary.start = from;
                toBoundary.end = to;
                toBoundary.start = to;
                this.distance = distance;
            }

            @Override
            public List<Boundary> search(Coordinate center, List<Boundary> results) {
                from.x = center.x - distance;
                from.y = center.y - distance;
                to.x = center.x + distance;
                to.y = center.y + distance;
                boundariesTree.search(results, fromBoundary, toBoundary);
                return results;
            }
        }

        @Override
        public CenterSearcher<Coordinate, Node> nodeSearcher() {
            return new NodeSearcher(maxNodeSupportDomainRadium);
        }

        @Override
        public CenterSearcher<Coordinate, Boundary> boundarySearcher() {
            return new BoundarySearcher(maxNodeSupportDomainRadium + 0.5 * longestBoundaryLength);
        }

        @Override
        public VolumeCondition getVolumeCondition() {
            return volumeCondition;
        }
    }

    /**
     * 构造一个标准Timoshenko闭合解的结点均匀分布的算例，该算例为一中轴在x轴上的矩型结构， 矩型左端与y轴共线，为Neumann边界。The
     * right side of the rectangle is at line x=width, and the dirichlet
     * boundary condition is applied on the right border
     *
     * @param nodesGap the maximum x or y projection betwean a node and its
     * nearest neigbor
     * @param supportDomainRadiu the support domain radiu of all the nodes
     * @param width the width of the cantilever
     * @param height the height of the cantilever
     * @param E Young's module
     * @param v Possion's ratio
     * @param P the total force applied on the right border &lt;0: upward &gt;0:
     * downward
     * @return a new instance of {@link ByArrays}
     */
    public static WeakFormProblem timoshenkoCantilevel(double nodesGap, double supportDomainRadiu, double width, double height, final double E, final double v, final double P) {
        TimoshenkoExactBeam exact = new TimoshenkoExactBeam(width, height, E, v, P);
        DenseMatrix constitutiveLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        int nodeColsNum = (int) Math.ceil(width / nodesGap) + 1;
        int nodeRowsNum = (int) Math.ceil(height / nodesGap) + 1;
        double rowsGap = height / (nodeRowsNum - 1);
        double colsGap = width / (nodeColsNum - 1);

        Node[] neumannNodes = new Node[nodeRowsNum];
        for (int i = 0; i < neumannNodes.length; i++) {
            neumannNodes[i] = new Node(0, height / 2 - rowsGap * i);
        }

        LinkedList<LineBoundary> boundaries = new LinkedList<>();
        LinkedList<BoundaryCondition> neumannBcs = new LinkedList<>();
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            LineBoundary bound = new LineBoundary(neumannNodes[i], neumannNodes[i + 1]);
            boundaries.add(bound);
            neumannBcs.add(exact.new NeumannBoundaryCondition(bound));
        }

        Node[] dirichletNodes = new Node[nodeRowsNum];
        LinkedList<BoundaryCondition> dirichletBCs = new LinkedList<>();
        for (int i = 0; i < dirichletNodes.length - 1; i++) {
            dirichletNodes[i] = new Node(width, -height / 2 + rowsGap * i);
        }
        dirichletNodes[dirichletNodes.length - 1] = new Node(width, height / 2);
        for (int i = 0; i < dirichletNodes.length - 1; i++) {
            LineBoundary bound = new LineBoundary(dirichletNodes[i], dirichletNodes[i + 1]);
            boundaries.add(bound);
            dirichletBCs.add(exact.new DirichletBoundaryCondition(bound));
        }

        Node[] upBoundaryNodes = new Node[nodeColsNum - 2];
        Node[] lowBoundaryNodes = new Node[nodeColsNum - 2];
        for (int i = 0; i < upBoundaryNodes.length; i++) {
            upBoundaryNodes[i] = new Node(width - (i + 1) * colsGap, height / 2);
            lowBoundaryNodes[i] = new Node((i + 1) * colsGap, -height / 2);
        }
        for (int i = 0; i < upBoundaryNodes.length - 1; i++) {
            boundaries.add(new LineBoundary(upBoundaryNodes[i], upBoundaryNodes[i + 1]));
            boundaries.add(new LineBoundary(lowBoundaryNodes[i], lowBoundaryNodes[i + 1]));
        }
        boundaries.add(new LineBoundary(neumannNodes[neumannNodes.length - 1], lowBoundaryNodes[0]));
        boundaries.add(new LineBoundary(lowBoundaryNodes[lowBoundaryNodes.length - 1], dirichletNodes[0]));
        boundaries.add(new LineBoundary(dirichletNodes[dirichletNodes.length - 1], upBoundaryNodes[0]));
        boundaries.add(new LineBoundary(upBoundaryNodes[upBoundaryNodes.length - 1], neumannNodes[0]));

        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(Arrays.asList(neumannNodes));
        nodes.addAll(Arrays.asList(dirichletNodes));
        nodes.addAll(Arrays.asList(upBoundaryNodes));
        nodes.addAll(Arrays.asList(lowBoundaryNodes));

        for (int i = 1; i < nodeRowsNum - 1; i++) {
            double y = -height / 2 + rowsGap * i;
            for (int j = 1; j < nodeColsNum - 1; j++) {
                double x = colsGap * j;
                nodes.add(new Node(x, y));
            }
        }

        int id = 0;
        for (Node nd : nodes) {
            nd.id = id++;
        }

        LinkedList<Quadrangle> quadrangles = new LinkedList<>();
        for (int i = 0; i < nodeRowsNum - 1; i++) {
            double y1, y2, y3, y4;
            y1 = y2 = -height / 2 + i * rowsGap;
            y3 = y4 = y1 + rowsGap;
            for (int j = 0; j < nodeColsNum - 1; j++) {
                double x1, x2, x3, x4;
                x1 = x4 = colsGap * j;
                x2 = x3 = x1 + colsGap;
                quadrangles.add(new Quadrangle(x1, y1, x2, y2, x3, y3, x4, y4));
            }
        }

        return new ByArrays(nodes.toArray(new Node[0]), null, supportDomainRadiu, boundaries.toArray(new LineBoundary[0]), null, quadrangles.toArray(new Quadrangle[0]), null, neumannBcs.toArray(new BoundaryCondition[0]), dirichletBCs.toArray(new BoundaryCondition[0]), 2, constitutiveLaw);
    }

    /**
     * A bar be tensed. The bar's axis is aligned to x axis. The bars left side
     * is aligned to y axis. The Neumann B.C. is simply fixing x and y freedom
     * as 0, at the y axis. The Dirichlet B.C. is simply a horizontal
     * distributing force applyed on the right side of the bar (x=widith)
     *
     * @param nodesGap the max projection betwean a node and its nearest
     * neighbor must be no bigger than nodesGap
     * @param supportDomainRadiu the constant support domain num of all the
     * nodes
     * @param width bar's width, commonly means 'length'
     * @param height the max and min y dimension inside the bar is height/2 and
     * -height/2
     * @param E Young's modulus
     * @param v Possion's ratio
     * @param P the distributing force that are applied on the right side of the
     * bar, the total force is P*height
     * @return A new instance.
     */
    public static WeakFormProblem tensionBarHorizontal(double nodesGap, double supportDomainRadiu, double width, double height, final double E, final double v, final double P) {
        DenseMatrix constitutiveLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        int nodeColsNum = (int) Math.ceil(width / nodesGap) + 1;
        int nodeRowsNum = (int) Math.ceil(height / nodesGap) + 1;
        double rowsGap = height / (nodeRowsNum - 1);
        double colsGap = width / (nodeColsNum - 1);

        Node[] neumannNodes = new Node[nodeRowsNum];
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            neumannNodes[i] = new Node(0, height / 2 - rowsGap * i);
        }
        neumannNodes[neumannNodes.length - 1] = new Node(0, -height / 2);
        LinkedList<LineBoundary> boundaries = new LinkedList<>();
        LinkedList<BoundaryCondition> neumannBcs = new LinkedList<>();
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(neumannNodes[i], neumannNodes[i + 1]);
            boundaries.add(bound);
            neumannBcs.add(new BoundaryCondition() {

                final boolean[] b1 = new boolean[]{true, false};
                final boolean[] b2 = new boolean[]{true, true};

                /**
                 * 目前还不支持点状的边界条件
                 */
                @Override
                public boolean[] values(Coordinate coord, double[] results) {
                    results[0] = 0;
                    results[1] = 0;
                    return b1;
                }

                @Override
                public boolean isByCoordinate() {
                    return true;
                }

                @Override
                public Boundary getBoundary() {
                    return bound;
                }
            });
        }

        Node[] dirichletNodes = new Node[nodeRowsNum];
        LinkedList<BoundaryCondition> dirichletBCs = new LinkedList<>();
        for (int i = 0; i < dirichletNodes.length - 1; i++) {
            dirichletNodes[i] = new Node(width, -height / 2 + rowsGap * i);
        }
        dirichletNodes[dirichletNodes.length - 1] = new Node(width, height / 2);
        for (int i = 0; i < dirichletNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(dirichletNodes[i], dirichletNodes[i + 1]);
            boundaries.add(bound);
            dirichletBCs.add(new BoundaryCondition() {

                final boolean[] b1 = new boolean[]{true, false};

                @Override
                public boolean[] values(Coordinate parameter, double[] results) {
                    results[0] = P;
                    results[1] = 0;
                    return b1;
                }

                @Override
                public boolean isByCoordinate() {
                    return false;
                }

                @Override
                public Boundary getBoundary() {
                    return bound;
                }
            });
        }

        Node[] upBoundaryNodes = new Node[nodeColsNum - 2];
        Node[] lowBoundaryNodes = new Node[nodeColsNum - 2];
        for (int i = 0; i < upBoundaryNodes.length; i++) {
            upBoundaryNodes[i] = new Node(width - (i + 1) * colsGap, height / 2);
            lowBoundaryNodes[i] = new Node((i + 1) * colsGap, -height / 2);
        }
        for (int i = 0; i < upBoundaryNodes.length - 1; i++) {
            boundaries.add(new LineBoundary(upBoundaryNodes[i], upBoundaryNodes[i + 1]));
            boundaries.add(new LineBoundary(lowBoundaryNodes[i], lowBoundaryNodes[i + 1]));
        }
        boundaries.add(new LineBoundary(neumannNodes[neumannNodes.length - 1], lowBoundaryNodes[0]));
        boundaries.add(new LineBoundary(lowBoundaryNodes[lowBoundaryNodes.length - 1], dirichletNodes[0]));
        boundaries.add(new LineBoundary(dirichletNodes[dirichletNodes.length - 1], upBoundaryNodes[0]));
        boundaries.add(new LineBoundary(upBoundaryNodes[upBoundaryNodes.length - 1], neumannNodes[0]));

        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(Arrays.asList(neumannNodes));
        nodes.addAll(Arrays.asList(dirichletNodes));
        nodes.addAll(Arrays.asList(upBoundaryNodes));
        nodes.addAll(Arrays.asList(lowBoundaryNodes));

        for (int i = 1; i < nodeRowsNum - 1; i++) {
            double y = -height / 2 + rowsGap * i;
            for (int j = 1; j < nodeColsNum - 1; j++) {
                double x = colsGap * j;
                nodes.add(new Node(x, y));
            }
        }

        int id = 0;
        for (Node nd : nodes) {
            nd.id = id++;
        }

        LinkedList<Quadrangle> quadrangles = new LinkedList<>();
        for (int i = 0; i < nodeRowsNum - 1; i++) {
            double y1, y2, y3, y4;
            y1 = y2 = -height / 2 + i * rowsGap;
            y3 = y4 = y1 + rowsGap;
            for (int j = 0; j < nodeColsNum - 1; j++) {
                double x1, x2, x3, x4;
                x1 = x4 = colsGap * j;
                x2 = x3 = x1 + colsGap;
                quadrangles.add(new Quadrangle(x1, y1, x2, y2, x3, y3, x4, y4));
            }
        }

        return new ByArrays(nodes.toArray(new Node[0]), null, supportDomainRadiu, boundaries.toArray(new LineBoundary[0]), null, quadrangles.toArray(new Quadrangle[0]), null, neumannBcs.toArray(new BoundaryCondition[0]), dirichletBCs.toArray(new BoundaryCondition[0]), 2, constitutiveLaw);
    }

    /**
     * A bar be tensed. The bar's axis is aligned to y axis. The bars down side
     * is aligned to x axis. The Neumann B.C. is simply fixing x and y freedom
     * as 0, at the x axis. The Dirichlet B.C. is simply a vertical distributing
     * force applyed on the top side of the bar (y=height)
     *
     * @param nodesGap the max projection betwean a node and its nearest
     * neighbor must be no bigger than nodesGap
     * @param supportDomainRadiu the constant support domain num of all the
     * nodes
     * @param width the max and min x dimension inside the bar is -width/2 and
     * width/2
     * @param height commonly means 'length' of the bar
     * @param E Young's modulus
     * @param v Possion's ratio
     * @param P the distributing force that are applied on the top side of the
     * bar, the total force is P*width
     * @return A new instance.
     */
    public static WeakFormProblem tensionBarVertical(double nodesGap, double supportDomainRadiu, double width, double height, final double E, final double v, final double P) {
        DenseMatrix constitutiveLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        int nodeColsNum = (int) Math.ceil(width / nodesGap) + 1;
        int nodeRowsNum = (int) Math.ceil(height / nodesGap) + 1;
        double rowsGap = height / (nodeRowsNum - 1);
        double colsGap = width / (nodeColsNum - 1);

        Node[] neumannNodes = new Node[nodeColsNum];
        for (int i = 0; i < neumannNodes.length; i++) {
            neumannNodes[i] = new Node(-width / 2 + colsGap * i, 0);
        }
        LinkedList<LineBoundary> boundaries = new LinkedList<>();
        LinkedList<BoundaryCondition> neumannBcs = new LinkedList<>();
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(neumannNodes[i], neumannNodes[i + 1]);
            boundaries.add(bound);
            neumannBcs.add(new BoundaryCondition() {

                final boolean[] b2 = new boolean[]{true, true};

                @Override
                public boolean[] values(Coordinate coord, double[] results) {
                    results[0] = 0;
                    results[1] = 0;
                    return b2;
                }

                @Override
                public boolean isByCoordinate() {
                    return true;
                }

                @Override
                public Boundary getBoundary() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }

        Node[] dirichletNodes = new Node[nodeColsNum];
        LinkedList<BoundaryCondition> dirichletBCs = new LinkedList<>();
        for (int i = 0; i < dirichletNodes.length; i++) {
            dirichletNodes[i] = new Node(width / 2 - colsGap * i, height);
        }
        for (int i = 0; i < dirichletNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(dirichletNodes[i], dirichletNodes[i + 1]);
            boundaries.add(bound);
            dirichletBCs.add(new BoundaryCondition() {

                final boolean[] b1 = new boolean[]{false, true};

                @Override
                public boolean[] values(Coordinate parameter, double[] results) {
                    results[0] = 0;
                    results[1] = P;
                    return b1;
                }

                @Override
                public boolean isByCoordinate() {
                    return true;
                }

                @Override
                public Boundary getBoundary() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }

        Node[] leftBoundaryNodes = new Node[nodeRowsNum - 2];
        Node[] rightBoundaryNodes = new Node[nodeRowsNum - 2];
        for (int i = 0; i < leftBoundaryNodes.length; i++) {
            leftBoundaryNodes[i] = new Node(-width / 2, height - (i + 1) * rowsGap);
            rightBoundaryNodes[i] = new Node(width / 2, (i + 1) * rowsGap);
        }
        for (int i = 0; i < leftBoundaryNodes.length - 1; i++) {
            boundaries.add(new LineBoundary(leftBoundaryNodes[i], leftBoundaryNodes[i + 1]));
            boundaries.add(new LineBoundary(rightBoundaryNodes[i], rightBoundaryNodes[i + 1]));
        }
        boundaries.add(new LineBoundary(neumannNodes[neumannNodes.length - 1], rightBoundaryNodes[0]));
        boundaries.add(new LineBoundary(rightBoundaryNodes[rightBoundaryNodes.length - 1], dirichletNodes[0]));
        boundaries.add(new LineBoundary(dirichletNodes[dirichletNodes.length - 1], leftBoundaryNodes[0]));
        boundaries.add(new LineBoundary(leftBoundaryNodes[leftBoundaryNodes.length - 1], neumannNodes[0]));

        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(Arrays.asList(neumannNodes));
        nodes.addAll(Arrays.asList(dirichletNodes));
        nodes.addAll(Arrays.asList(leftBoundaryNodes));
        nodes.addAll(Arrays.asList(rightBoundaryNodes));

        for (int i = 1; i < nodeRowsNum - 1; i++) {
            double y = 0 + rowsGap * i;
            for (int j = 1; j < nodeColsNum - 1; j++) {
                double x = -width / 2 + colsGap * j;
                nodes.add(new Node(x, y));
            }
        }

        int id = 0;
        for (Node nd : nodes) {
            nd.id = id++;
        }

        LinkedList<Quadrangle> quadrangles = new LinkedList<>();
        for (int i = 0; i < nodeRowsNum - 1; i++) {
            double y1, y2, y3, y4;
            y1 = y2 = i * rowsGap;
            y3 = y4 = y1 + rowsGap;
            for (int j = 0; j < nodeColsNum - 1; j++) {
                double x1, x2, x3, x4;
                x1 = x4 = -width / 2 + colsGap * j;
                x2 = x3 = x1 + colsGap;
                quadrangles.add(new Quadrangle(x1, y1, x2, y2, x3, y3, x4, y4));
            }
        }

        return new ByArrays(nodes.toArray(new Node[0]), null, supportDomainRadiu, boundaries.toArray(new LineBoundary[0]), null, quadrangles.toArray(new Quadrangle[0]), null, neumannBcs.toArray(new BoundaryCondition[0]), dirichletBCs.toArray(new BoundaryCondition[0]), 2, constitutiveLaw);
    }

    /**
     * A bar be tensed by only Neumann B.C. . The bar's axis is aligned to x
     * axis. The bars left side is aligned to y axis. One Neumann B.C. is simply
     * fixing x and y freedom as 0, at the y axis. The othe Neumann B.C. is
     * simply a horizontal displacement of y freedom applyed on the right side
     * of the bar (x=widith)
     *
     * @param nodesGap the max projection betwean a node and its nearest
     * neighbor must be no bigger than nodesGap
     * @param supportDomainRadiu the constant support domain num of all the
     * nodes
     * @param width bar's width, commonly means 'length'
     * @param height the max and min y dimension inside the bar is height/2 and
     * -height/2
     * @param E Young's modulus
     * @param v Possion's ratio
     * @param P the displacement that are applied on the right side of the bar.
     * @return A new instance.
     */
    public static WeakFormProblem displacementTensionBar(double nodesGap, double supportDomainRadiu, final double width, double height, final double E,
            final double v,
            final double displace) {
        DenseMatrix constitutiveLaw = ConstitutiveLaws2D.getPlaneStress(E, v);
        int nodeColsNum = (int) Math.ceil(width / nodesGap) + 1;
        int nodeRowsNum = (int) Math.ceil(height / nodesGap) + 1;
        double rowsGap = height / (nodeRowsNum - 1);
        double colsGap = width / (nodeColsNum - 1);

        Node[] neumannNodes = new Node[nodeRowsNum];
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            neumannNodes[i] = new Node(0, height / 2 - rowsGap * i);
        }
        neumannNodes[neumannNodes.length - 1] = new Node(0, -height / 2);
        LinkedList<LineBoundary> boundaries = new LinkedList<>();
        LinkedList<BoundaryCondition> neumannBcs = new LinkedList<>();
        for (int i = 0; i < neumannNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(neumannNodes[i], neumannNodes[i + 1]);
            boundaries.add(bound);
            neumannBcs.add(new BoundaryCondition() {

              
                final boolean[] b1 = new boolean[]{true, false};
                final boolean[] b2 = new boolean[]{true, true};

                @Override
                public boolean[] values(Coordinate coord, double[] results) {
                    results[0] = 0;
                    results[1] = 0;

                    return b2;

                }

                @Override
                public boolean isByCoordinate() {
                    return true;
                }

                @Override
                public Boundary getBoundary() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

            });
        }

        Node[] neumannRightNodes = new Node[nodeRowsNum];
        LinkedList<BoundaryCondition> neumannRightBCs = new LinkedList<>();
        for (int i = 0; i < neumannRightNodes.length - 1; i++) {
            neumannRightNodes[i] = new Node(width, -height / 2 + rowsGap * i);
        }
        neumannRightNodes[neumannRightNodes.length - 1] = new Node(width, height / 2);
        for (int i = 0; i < neumannRightNodes.length - 1; i++) {
            final LineBoundary bound = new LineBoundary(neumannRightNodes[i], neumannRightNodes[i + 1]);
            boundaries.add(bound);
            neumannRightBCs.add(new BoundaryCondition() {

                final boolean[] b1 = new boolean[]{true, false};
                final boolean[] b2 = new boolean[]{true, true};

                @Override
                public boolean[] values(Coordinate coord, double[] results) {
                    results[0] = displace;
                    results[1] = 0;
                    if (coord.y == 0) {
                        return b2;
                    } else {
                        return b1;
                    }
                }

                @Override
                public boolean isByCoordinate() {
                    return true;
                }

                @Override
                public Boundary getBoundary() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }

        Node[] upBoundaryNodes = new Node[nodeColsNum - 2];
        Node[] lowBoundaryNodes = new Node[nodeColsNum - 2];
        for (int i = 0; i < upBoundaryNodes.length; i++) {
            upBoundaryNodes[i] = new Node(width - (i + 1) * colsGap, height / 2);
            lowBoundaryNodes[i] = new Node((i + 1) * colsGap, -height / 2);
        }
        for (int i = 0; i < upBoundaryNodes.length - 1; i++) {
            boundaries.add(new LineBoundary(upBoundaryNodes[i], upBoundaryNodes[i + 1]));
            boundaries.add(new LineBoundary(lowBoundaryNodes[i], lowBoundaryNodes[i + 1]));
        }
        boundaries.add(new LineBoundary(neumannNodes[neumannNodes.length - 1], lowBoundaryNodes[0]));
        boundaries.add(new LineBoundary(lowBoundaryNodes[lowBoundaryNodes.length - 1], neumannRightNodes[0]));
        boundaries.add(new LineBoundary(neumannRightNodes[neumannRightNodes.length - 1], upBoundaryNodes[0]));
        boundaries.add(new LineBoundary(upBoundaryNodes[upBoundaryNodes.length - 1], neumannNodes[0]));

        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(Arrays.asList(neumannNodes));
        nodes.addAll(Arrays.asList(neumannRightNodes));
        nodes.addAll(Arrays.asList(upBoundaryNodes));
        nodes.addAll(Arrays.asList(lowBoundaryNodes));

        for (int i = 1; i < nodeRowsNum - 1; i++) {
            double y = -height / 2 + rowsGap * i;
            for (int j = 1; j < nodeColsNum - 1; j++) {
                double x = colsGap * j;
                nodes.add(new Node(x, y));
            }
        }

        int id = 0;
        for (Node nd : nodes) {
            nd.id = id++;
        }

        LinkedList<Quadrangle> quadrangles = new LinkedList<>();
        for (int i = 0; i < nodeRowsNum - 1; i++) {
            double y1, y2, y3, y4;
            y1 = y2 = -height / 2 + i * rowsGap;
            y3 = y4 = y1 + rowsGap;
            for (int j = 0; j < nodeColsNum - 1; j++) {
                double x1, x2, x3, x4;
                x1 = x4 = colsGap * j;
                x2 = x3 = x1 + colsGap;
                quadrangles.add(new Quadrangle(x1, y1, x2, y2, x3, y3, x4, y4));
            }
        }

        neumannBcs.addAll(neumannRightBCs);
        return new ByArrays(
                nodes.toArray(new Node[0]),
                null,
                supportDomainRadiu,
                boundaries.toArray(new LineBoundary[0]),
                null,
                quadrangles.toArray(new Quadrangle[0]),
                null,
                neumannBcs.toArray(new BoundaryCondition[0]),
                null,
                2,
                constitutiveLaw);
    }
}