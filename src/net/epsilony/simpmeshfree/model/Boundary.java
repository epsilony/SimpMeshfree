/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.model2d.WeakFormProcessor2D;
import net.epsilony.utils.CenterSearcher;
import net.epsilony.utils.geom.Coordinate;

/**
 * 边界是由一系列的控制点张成的参数曲线或曲面</br>
 * 对于2D，若有边界的控制点和曲线的起点重合，则此起点的序号index为0，若有控制点为曲线段终点，
 * 则此控制点的index应为pointsSize()-1
 * @author epsilonyuan@gmail.com
 */
public interface Boundary {

    /**
     * 获取第index个控制点
     * @param index 近制点的序号 范围[0,pointsSize())
     * @return just get
     */
    Coordinate getPoint(int index);

    /**
     * 
     * @return 边界的控制点个数
     */
    int pointsSize();

    /**
     * 边界的几何中点，Optional</br>
     * 主要用来做{@link CenterSearcher}的实现，
     * 被应用于{@link WeakFormProcessor2D#process()}中过滤离计算点效远的边界
     * @param result
     * @return result
     */
    Coordinate centerPoint(Coordinate result);
    
    /**
     * 通这空间上一点的参数获得边界上一点的空间坐标
     * @param par 参数
     * @param result 边界上的点坐标
     * @return result
     */
    Coordinate valueByParameter(Coordinate par,Coordinate result);

    /**
     * 对2D问题，边界为一参数曲线:</br>
     * <img src="http://epsilony.net/cgi-bin/mathtex.cgi?f=(x(t),y(t))" alt="f=(x(t),y(t))"></br>
     * 则反回:</br>
     * <img src="http://epsilony.net/cgi-bin/mathtex.cgi?\left(\frac{\partial{}x}{\partial{}t},\frac{\partial{}y}{\partial{}t}\right)" alt="\left(\frac{\partial{}x}{\partial{}t},\frac{\partial{}y}{\partial{}t}\right)"></br>
     * @param par 2D: (t,0,0) 3D: (u,v)
     * @param results 2D 
     * @return results
     */
    Coordinate[] valuePartialByParameter(Coordinate par, Coordinate results[]);

    public static class CenterPointOnlyBoundary implements Boundary {

        Coordinate centerPoint;

        @Override
        public Coordinate getPoint(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int pointsSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Coordinate centerPoint(Coordinate result) {
            return centerPoint;
        }

        @Override
        public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Coordinate valueByParameter(Coordinate par,Coordinate result) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
