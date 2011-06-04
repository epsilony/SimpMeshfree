/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.shapefun;

import java.util.List;
import net.epsilony.math.polynomial.BivariateBinomials;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.Node;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmDenseMatrix;
import no.uib.cipr.matrix.Vector;

/**
 * <br> 本类用于生成径向基＋多项式的形函数RPIM向量 </br>
 * <br> 本类的测试工程为RPIMTest </br>
 * <p> <bold> Changelist </bold>
 * <br> 0.11 增加了对一阶、二阶偏导数向量的支持 </br>
 * <br> 0.10 新建，支持形函数 </br>
 * @version 0.11 haven't been tested
 * @author M.Yuan J.-J.Chen
 */
public class RadialPolynomialShapeFunction implements ShapeFunction {

    RadialBasisFunction radialBasisFunction;
    int power;

    /**
     * 构造一个RPIM函数
     * @param radialBasisFunction 径向基函数
     * @param power 多项式的最高阶数
     */
    public RadialPolynomialShapeFunction(RadialBasisFunction radialFun, int power) {
        this.radialBasisFunction = radialFun;
        this.power = power;
    }

    /**
     * <br>计算点(x,y)的形函数向量:</br>
     * <br>&phi;<sub>1</sub>,&phi;<sub>2</sub>,...,&phi;<sub>k</sub></br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return 形函数向量其值为：{&phi;<sub>1</sub>,&phi;<sub>2</sub>,...,&phi;<sub>k</sub>}
     */
    @Override
    public Vector shapeValues(List<Node> nodes, double x, double y) {
        int m = (power * power + 3 * power + 2) / 2;
        UpperSymmDenseMatrix gMat = new UpperSymmDenseMatrix(m + nodes.size());
        int i, j;
        double[] binomials = new double[m];
        Vector resultVec = new DenseVector(m + nodes.size());
        double nodex, nodey;
        for (i = 0; i < nodes.size(); i++) {
            nodex = nodes.get(i).getX();
            nodey = nodes.get(i).getY();
            radialBasisFunction.setCenter(nodex, nodey);
            for (j = i; j < nodes.size(); j++) {
                gMat.set(i, j, radialBasisFunction.value(nodes.get(j).getX(), nodes.get(j).getY()));
            }
            BivariateBinomials.getBinomials(power, nodex, nodey, binomials);
            for (j = 0; j < m; j++) {
                gMat.set(i, j + nodes.size(), binomials[j]);
            }
        }
        Vector b = new DenseVector(resultVec.size());
        for (i = 0; i < nodes.size(); i++) {
            radialBasisFunction.setCenter(nodes.get(i).getX(), nodes.get(i).getY());
            b.set(i, radialBasisFunction.value(x, y));
        }
        BivariateBinomials.getBinomials(power, x, y, binomials);
        for (i = nodes.size(); i < b.size(); i++) {
            b.set(i, binomials[i - nodes.size()]);
        }
//        if(DenseLU.factorize(gMat).isSingular()){
//            for(Node node:nodes){
//                System.out.println(node);
//            }
//        }
        return gMat.solve(b, resultVec);
    }

    /**
     * <br>计算点(x,y)的形函数偏导数向量</br>
     * <br> &part;&phi;<sub>1</sub>/&part;x,&part;&phi;<sub>2</sub>/&part;x,...,&part;&phi;<sub>2</sub>/&part;x</br>
     * <br> &part;&phi;<sub>1</sub>/&part;y,&part;&phi;<sub>2</sub>/&part;y,...,&part;&phi;<sub>2</sub>/&part;y</br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return <br>形函数向量组成的数组其值为：
     * <br>{&part;&phi;<sub>1</sub>/&part;x,&part;&phi;<sub>2</sub>/&part;x,...,&part;&phi;<sub>2</sub>/&part;x;</br>
     * <br> &part;&phi;<sub>1</sub>/&part;y,&part;&phi;<sub>2</sub>/&part;y,...,&part;&phi;<sub>2</sub>/&part;y}</br>
     */
    @Override
    public Vector[] shapePartialValues(List<Node> nodes, double x, double y) {
        int m = (power * power + 3 * power + 2) / 2;
        Vector resultVecPx = new DenseVector(m + nodes.size());
        Vector resultVecPy = new DenseVector(m + nodes.size());
        double[] partResults = new double[2];
        UpperSymmDenseMatrix gMat = new UpperSymmDenseMatrix(m + nodes.size());
        int i, j;
        double[] binomials = new double[m];
//        Vector resultVec = new DenseVector((power * power + 3 * power + 2) / 2 + nodes.size());
        double nodex, nodey;
        for (i = 0; i < nodes.size(); i++) {
            nodex = nodes.get(i).getX();
            nodey = nodes.get(i).getY();
            radialBasisFunction.setCenter(nodex, nodey);
            for (j = i; j < nodes.size(); j++) {
                gMat.set(i, j, radialBasisFunction.value(nodes.get(j).getX(), nodes.get(j).getY()));
            }
            BivariateBinomials.getBinomials(power, nodex, nodey, binomials);
            for (j = 0; j < m; j++) {
                gMat.set(i, j + nodes.size(), binomials[j]);
            }
        }
        Vector bPx = new DenseVector(resultVecPx.size());
        Vector bPy = new DenseVector(resultVecPy.size());
        for (i = 0; i < nodes.size(); i++) {
            radialBasisFunction.setCenter(nodes.get(i).getX(), nodes.get(i).getY());
            radialBasisFunction.partialDifferential(x, y, partResults);
            bPx.set(i, partResults[0]);
            bPy.set(i, partResults[1]);
        }
        double[] pXBinomials = new double[m];
        double[] pYBinomials = new double[m];
        BivariateBinomials.getPxBinomials(power, x, y, pXBinomials);
        BivariateBinomials.getPyBinomials(power, x, y, pYBinomials);
        for (i = nodes.size(); i < bPx.size(); i++) {
            bPx.set(i, pXBinomials[i - nodes.size()]);
            bPy.set(i, pYBinomials[i - nodes.size()]);
        }
        Vector[] result = new Vector[2];
        result[0] = gMat.solve(bPx, resultVecPx);
        result[1] = gMat.solve(bPy, resultVecPy);
        return result;
    }

    /**
     * <br>计算点(x,y)的形函数二阶偏导数向量</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;x&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&sup2;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&part;y,&part;&sup2;&phi;<sub>2</sub>/&part;x&part;y,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&part;y</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;y&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;y&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;y&sup2;</br>
     * @param nodes 支持域内的节点list n<sub>1</sub>,n<sub>2</sub>,...,n<sub>k</sub>
     * @param x
     * @param y
     * @return <br>形函数向量组成的数组,其值为：</br>
     * <br> {&part;&sup2;&phi;<sub>1</sub>/&part;x&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;x&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&sup2;;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;x&part;y,&part;&sup2;&phi;<sub>2</sub>/&part;x&part;y,...,&part;&sup2;&phi;<sub>k</sub>/&part;x&part;y;</br>
     * <br> &part;&sup2;&phi;<sub>1</sub>/&part;y&sup2;,&part;&sup2;&phi;<sub>2</sub>/&part;y&sup2;,...,&part;&sup2;&phi;<sub>k</sub>/&part;y&sup2;}</br>
     */
    @Override
    public Vector[] shapeQuadPartialValues(List<Node> nodes, double x, double y) {
        int m = (power * power + 3 * power + 2) / 2;
        UpperSymmDenseMatrix gMat = new UpperSymmDenseMatrix((power * power + 3 * power + 2) / 2 + nodes.size());
        int i, j;
        double[] binomials = new double[m];
        double nodex, nodey;
        for (i = 0; i < nodes.size(); i++) {
            nodex = nodes.get(i).getX();
            nodey = nodes.get(i).getY();
            radialBasisFunction.setCenter(nodex, nodey);
            for (j = i; j < nodes.size(); j++) {
                gMat.set(i, j, radialBasisFunction.value(nodes.get(j).getX(), nodes.get(j).getY()));
            }
            BivariateBinomials.getBinomials(power, nodex, nodey, binomials);
            for (j = 0; j < m; j++) {
                gMat.set(i, j + nodes.size(), binomials[j]);
            }
        }
        Vector resultVecPxx = new DenseVector(m + nodes.size());
        Vector resultVecPxy = new DenseVector(m + nodes.size());
        Vector resultVecPyy = new DenseVector(m + nodes.size());

        Vector bPxx = new DenseVector(resultVecPxx.size());
        Vector bPxy = new DenseVector(resultVecPxy.size());
        Vector bPyy = new DenseVector(resultVecPyy.size());
        double[] partResults = new double[3];
        for (i = 0; i < nodes.size(); i++) {
            radialBasisFunction.setCenter(nodes.get(i).getX(), nodes.get(i).getY());
            radialBasisFunction.quadPartialDifferential(x, y, partResults);
            bPxx.set(i, partResults[0]);
            bPxy.set(i, partResults[1]);
            bPyy.set(i, partResults[2]);
        }
        double[] pxxBinomials = new double[m];
        double[] pxyBinomials = new double[m];
        double[] pyyBinomials = new double[m];

        BivariateBinomials.getPxxBinomials(power, x, y, pxxBinomials);
        BivariateBinomials.getPxyBinomials(power, x, y, pxyBinomials);
        BivariateBinomials.getPyyBinomials(power, x, y, pyyBinomials);
        for (i = nodes.size(); i < bPxx.size(); i++) {
            bPxx.set(i, pxxBinomials[i - nodes.size()]);
            bPxy.set(i, pxyBinomials[i - nodes.size()]);
            bPyy.set(i, pyyBinomials[i - nodes.size()]);
        }
        Vector[] result = new Vector[3];
        result[0] = gMat.solve(bPxx, resultVecPxx);
        result[1] = gMat.solve(bPxy, resultVecPxy);
        result[2] = gMat.solve(bPyy, resultVecPyy);
        return result;
    }

    @Override
    public ShapeFunction CopyOf(boolean deep) {
        if (deep) {
            return new RadialPolynomialShapeFunction(radialBasisFunction.CopyOf(deep), power);
        } else {
            return new RadialPolynomialShapeFunction(null, power);
        }
    }

    @Override
    public RadialBasisFunction getRadialBasisFunction() {
        return radialBasisFunction;
    }

    @Override
    public void setRadialBasisFunction(RadialBasisFunction radialBasisFunction) {
        this.radialBasisFunction = radialBasisFunction;
    }
}
