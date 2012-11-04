/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.WithId;
import java.io.Serializable;
import javax.imageio.spi.ServiceRegistry;
import net.epsilony.utils.geom.Coordinate;

/**
 * <p><code>Boundary</code> can compose a solid model.</p>
 * <p> This project adapts a three-level geometry model:
 * <ul>
 * <li> {@link Node}: As edge vertes,  nodes can form a <code>Boundary</code>.</li>
 * <li> {@link Boundar}: Formed by {@link Node}, boundaries can form shells of solids.</li>
 * <li> Solid: implicitly formed by boundaries and their neighborhood relationships. There isn't
 * an explicit class that represents solid ojbects but {@link GeomUtils} provides nessessary logical
 * assistance.</li>
 * </ul>
 * <p>This class is designed as a common interface of {@link LineBoundary line} (in 2D) and 
 * {@link TriangleBoundary triangle} (in 3D) that are simples in their dimensions. 
 * Polyhedral implements may be support in the future if it is really necessary</p>
 * <p>In math, a <code>Boundary</code> can seperate geometric space into outside and inside halfs, and
 * {@link #outNormal(net.epsilony.utils.geom.Coordinate) outNormal} desides which
 * half space is outside. Several <code>Boundary</code> instances may form a 
 * closed shell and the union of inside points of each boundary make up a solid 
 * object for consequent meshfree analysis.
 * <strong>!NOTE!</strong> Take care to the points that are just on a boundary.
 * Whether these points are inside or outside the boundary is according to the situation.
 * One of the circuitous method, which can dodge the on-boundary point problem, 
 * is just pulling the point outside the boundary according to the normal vector.
 * {@link GeomUtils#bndPtTrans(net.epsilony.utils.geom.Coordinate, net.epsilony.simpmeshfree.model.Boundary, double, net.epsilony.utils.geom.Coordinate, int) GeomUtils.bndPtTrans}
 * provides an example</p>
 * <p> The out normal of a <code>Boundary</code> depends on its {@link Node}s according to righthanded rule.
 * <ul>
 * <lo> {@link LineBoundary}: Only support for 2D, the rightside of vector n0->n1 is the outside. Where n<i>i</i> is the Node from {@link #getNode(int) getNode(i)}. The 3D edition out normal of a line needs a extra noral vector and is realized by {@link BoundaryUtils#normal(net.epsilony.simpmeshfree.model.LineBoundary, net.epsilony.utils.geom.Coordinate, net.epsilony.utils.geom.Coordinate)} </lo>
 * <lo> {@link TriangleBoundary}: Out normal is righthand thumb of n0, n1 and n2.</lo>
 * </ul>
 * </p>

 * @see LineBoundary
 * @see TriangleBoundary
 * @see GeomUtils
 * @author epsilonyuan@gmail.com
 */
public interface Boundary extends WithId,Serializable{

    /**
     * @param index in the range of [0,num())
     * @return the reference of indexth node
     */
    Node getNode(int index);

    /**
     * This Boundary and getNeighbor(i) (if exists) has common nodes that are getNode[i],getNode[(i+1)%num()]
     * @param index in the range of [0,num())
     * @return ref of bnd
     */
    Boundary getNeighbor(int index);

    /**
     * Gets number of nodes which is equals to number of neighbors
     * @return 
     */
    int num();
    
    /**
     * Calculate the cirum sphere of this boundary.
     * @param outputCenter for output, the center of cirum sphere. Cannot be null.
     * @return The radius of cirum sphere
     */
    double circum(Coordinate outputCenter);
    
    /**
     * Determines whether there are common points betwean this boundary and the shpere with {@code center} and {@code radius} given.
     * @param center
     * @param radius
     * @return 
     */
    boolean isIntersect(Coordinate center,double radius);
    
    /**
     * Calculates unit out normal 
     * @param result can be null
     * @return {@code result} or a new instance of {@code Coordinate} if {@code result} is null
     */
    Coordinate outNormal(Coordinate result);
//    
//    /**
//     * The length square of line all the max length square of boundary edges
//     * @return 
//     */
//    double sizeSquare();

////    /**
////     * 边界的几何中点，Optional</br> 主要用来做{@link CenterSearcher}的实现， 被应用于{@link WeakFormProcessor2D#process()}中过滤离计算点效远的边界
////     *
////     * @param result
////     * @return result
////     */
////    Coordinate center(Coordinate result);
////    
//    /**
//     * 通这空间上一点的参数获得边界上一点的空间坐标
//     *
//     * @param par 参数
//     * @param result 边界上的点坐标
//     * @return result
//     */
//    Coordinate valueByParameter(Coordinate par, Coordinate result);
//
//    /**
//     * 对2D问题，边界为一参数曲线:</br> <img
//     * src="http://epsilony.net/cgi-bin/mathtex.cgi?f=(x(t),y(t))"
//     * alt="f=(x(t),y(t))"></br> 则反回:</br> <img
//     * src="http://epsilony.net/cgi-bin/mathtex.cgi?\left(\frac{\partial{}x}{\partial{}t},\frac{\partial{}y}{\partial{}t}\right)"
//     * alt="\left(\frac{\partial{}x}{\partial{}t},\frac{\partial{}y}{\partial{}t}\right)"></br>
//     *
//     * @param par 2D: (t,0,0) 3D: (u,v)
//     * @param results 2D
//     * @return results
//     */
//    Coordinate[] valuePartialByParameter(Coordinate par, Coordinate results[]);
//
//    public static class CenterPointOnlyBoundary implements Boundary {
//
//        Coordinate centerPoint;
//
//        @Override
//        public Coordinate getPoint(int index) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Node getNode(int index) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Boundary getNeighbor(int index) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public int num() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Coordinate center(Coordinate result) {
//            return centerPoint;
//        }
//
//        @Override
//        public Coordinate valueByParameter(Coordinate par, Coordinate result) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public int getId() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void setId(int id) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public double sizeSquare() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
}