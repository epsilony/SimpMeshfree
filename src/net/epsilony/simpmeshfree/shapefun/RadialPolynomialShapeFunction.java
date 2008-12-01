/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.shapefun;
import java.util.List;
import net.epsilony.math.polynomial.BivariateBinomials;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.Node;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmDenseMatrix;
import no.uib.cipr.matrix.Vector;

/**
 * 本类用于生成形函数向量
 * @version 0.10 haven't been tested
 * @author M.Yuan J.-J.Chen
 */
public class RadialPolynomialShapeFunction {
    RadialBasisFunction radialFun;
    int power;

    public RadialPolynomialShapeFunction(RadialBasisFunction radialFun, int power) {
        this.radialFun = radialFun;
        this.power = power;
    }
    
    public Vector shapeValues(List<Node> nodes,double x, double y){
        UpperSymmDenseMatrix gMat=new UpperSymmDenseMatrix((power*power+3*power+2)/2+nodes.size());
        int i,j;
        double [] binomials=new double [(power*power+3*power+2)/2];
        Vector resultVec=new DenseVector((power*power+3*power+2)/2+nodes.size());
        double nodex,nodey;
        for(i=0;i<nodes.size();i++)
        {
            nodex=nodes.get(i).getX();
            nodey=nodes.get(i).getY();
            radialFun.setCenter(nodes.get(i).getX(), nodes.get(i).getY());
            for(j=i;j<nodes.size();j++){
                gMat.set(i,j,radialFun.value(nodes.get(j).getX(), nodes.get(j).getY()));
            }
            BivariateBinomials.getBinomials(power, nodex,nodey,binomials);
            for(j=0;j<(power*power+3*power+2)/2;j++){
                gMat.set(i, j+nodes.size(), binomials[j]);
            }           
        }
        Vector b=new DenseVector(resultVec.size());
        for(i=0;i<nodes.size();i++){
            radialFun.setCenter(nodes.get(i).getX(), nodes.get(i).getY());
            b.set(i,radialFun.value(x, y));
        }
        BivariateBinomials.getBinomials(power, x, y, binomials);
        for(i=nodes.size();i<b.size();i++){
            b.set(i,binomials[i-nodes.size()]);
        }
        resultVec=gMat.solve(b, resultVec);
        return resultVec;
    }
//    public void getShapeFunValue(double rIni, double nonUnitValue, double criterion, Node center, List<Node> nodeInEffectDomain, double qVal, int p, Vector shapeFunValue, Vector deviXShapeFunValue, Vector deviYShapeFunValue) {
//        double averageNodeDis = supportDomain(rIni, nonUnitValue, criterion, center, nodeInEffectDomain) / nonUnitValue;
//        int num = nodeInEffectDomain.size();
//        int m = 0;
//        for (int i = 1; i <= p + 1; i++) {
//            m = m + i;
//        }
//        double[] objectPointBasicFunValue = new double[num + m];
//        double[][] polynomialArray = new double[num][m];
//        UpperSymmDenseMatrix rMatrix = new UpperSymmDenseMatrix(num);
//        UpperSymmDenseMatrix gUpperSymmMatrix = new UpperSymmDenseMatrix(num + m);
//        for (int i = 0; i < num; i++) {
//            for (int j = i; j < num; j++) {
//                rMatrix.add(i, j, getRadialBasicFunValue(nodeInEffectDomain.get(i).x, nodeInEffectDomain.get(j).x, nodeInEffectDomain.get(i).y, nodeInEffectDomain.get(j).y, qVal, nonUnitValue, averageNodeDis));
//            }
//        }
//        for (int i = 0; i < num; i++) {
//            polynomialArray[i] = getPolyBasicFunValue(nodeInEffectDomain.get(i).x, nodeInEffectDomain.get(i).y, p, m);
//        }
//        for (int i = 0; i < num; i++) {
//            for (int j = i; j < num; j++) {
//                gUpperSymmMatrix.add(i, j, rMatrix.get(i, j));
//            }
//            for (int j = num; j < num + m; j++) {
//                gUpperSymmMatrix.add(i, j, polynomialArray[i][j - num]);
//            }
//        }
//        for (int i = num; i < num + m; i++) {
//            for (int j = i; j < num + m; j++) {
//                gUpperSymmMatrix.add(i, j, 0);
//            }
//        }
//        for (int i = 0; i < num; i++) {
//            objectPointBasicFunValue[i] = getRadialBasicFunValue(center.x, nodeInEffectDomain.get(i).x, center.y, nodeInEffectDomain.get(i).y, qVal, nonUnitValue, averageNodeDis);
//        }
//        double[] middle = new double[m];
//        middle = getPolyBasicFunValue(center.x, center.y, p, m);
//        for (int i = num; i < num + m; i++) {
//            objectPointBasicFunValue[i] = middle[i - num];
//        }
//        DenseVector basicFunValueVector = new DenseVector(objectPointBasicFunValue);
//        double[] deviXObjectPointBasicFunValue = new double[num + m];
//        double[] deviYObjectPointBasicFunValue = new double[num + m];
//        for (int i = 0; i < num; i++) {
//            deviXObjectPointBasicFunValue[i] = getDeviXRadialBasicFunValue(nodeInEffectDomain.get(i).x, center.x, nodeInEffectDomain.get(i).y, center.y, qVal, nonUnitValue, averageNodeDis);
//        }
//        double[] xMiddle = new double[m];
//        xMiddle = getDeviXPolyBasicFunValue(center.x, center.y, p);
//        for (int i = num; i < num + m; i++) {
//            deviXObjectPointBasicFunValue[i] = xMiddle[i - num];
//        }
//        DenseVector deviXBasicFunValueVector = new DenseVector(deviXObjectPointBasicFunValue);
//        for (int i = 0; i < num; i++) {
//            deviYObjectPointBasicFunValue[i] = getDeviYRadialBasicFunValue(nodeInEffectDomain.get(i).x, center.x, nodeInEffectDomain.get(i).y, center.y, qVal, nonUnitValue, averageNodeDis);
//        }
//        double[] yMiddle = new double[m];
//        yMiddle = getDeviYPolyBasicFunValue(center.x, center.y, p);
//        for (int i = num; i < num + m; i++) {
//            deviYObjectPointBasicFunValue[i] = yMiddle[i - num];
//        }
//        DenseVector deviYBasicFunValueVector = new DenseVector(deviYObjectPointBasicFunValue);
//
//        gUpperSymmMatrix.solve(basicFunValueVector, shapeFunValue);
//        gUpperSymmMatrix.solve(deviXBasicFunValueVector, deviXShapeFunValue);
//        gUpperSymmMatrix.solve(deviYBasicFunValueVector, deviYShapeFunValue);
//
//    }
}
