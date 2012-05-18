/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import gnu.trove.list.array.TDoubleArrayList;
import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.*;
import net.epsilony.simpmeshfree.utils.BivariateCompletePolynomial;
import net.epsilony.simpmeshfree.utils.CoordinatePartDiffArrayFunction;
import net.epsilony.simpmeshfree.utils.UnivariatePartDiffFunction;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseVector;
import org.junit.Test;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class ShapeFunctions2DTest {

    @Test
    public void testLinear() {
        System.out.println("start linear function reproduction test");
        int testNum = 1000;

    }

    public static List<Node> genNodes(double xMin, double yMin, double xMax, double yMax, double ndDistOrNum, boolean isDist) {
        int numX, numY;
        double stepX, stepY;
        if (isDist) {
            numX = (int) ceil((xMax - xMin) / ndDistOrNum) + 1;
            numY = (int) ceil((yMax - yMin) / ndDistOrNum) + 1;

        } else {
            numX = (int) ceil(ndDistOrNum);
            numY = (int) ceil(ndDistOrNum);
        }
        stepX = (xMax - xMin) / numX;
        stepY = (yMax - yMin) / numY;
        LinkedList<Node> result = new LinkedList<>();
        for (int i = 0; i < numX; i++) {
            double x = xMin + stepX * i;
            if (i == 0) {
                x = xMin;
            } else if (i == numX - 1) {
                x = xMax;
            }
            for (int j = 0; j < numY; j++) {
                double y = yMin + stepY * j;
                if (j == 0) {
                    y = yMin;
                } else if (j == numY - 1) {
                    y = yMax;
                }
                result.add(new Node(x, y));
            }
        }
        return result;

    }

    public static ShapeFunction genShapeFunction(double rad, List<Node> nds, int baseOrder, UnivariatePartDiffFunction coreFun) {
        SupportDomainCritierion criterion = SupportDomainUtils.simpCriterion(rad, nds);
        CoordinatePartDiffArrayFunction baseFun = BivariateCompletePolynomial.complete2DBaseFunction(baseOrder);
        WeightFunction weightFunction = WeightFunctions.factory(coreFun, new DistanceFunctions.Common());
        return new ShapeFunctions2D.MLS(weightFunction, baseFun, criterion);
    }

    public interface ValueFun {

        double[] value(Coordinate c);
    }
    
    public static double[] value(DenseVector[] shapeFunVals,List<Node> nds,ValueFun fun){
        int i=0;
        double[] result=new double[3];
        for (Node nd:nds){
            double[] funVs=fun.value(nd);
            for (int j=0;j<3;j++){
                result[j]+=shapeFunVals[j].get(i)*funVs[j];
            }
            i++;
        }
        return result;
    }
    
    public static void evalCase(ShapeFunction shapeFun,ValueFun expFun,List<Node> nds,List<? extends Coordinate> centers,List<double[]> actResults){
        shapeFun.setDiffOrder(1);
        DenseVector[] funVal=new DenseVector[3];
        ArrayList<Node> resNodes=new ArrayList(2000);
        for(Coordinate center:centers){
            TDoubleArrayList[] shapeFunVals=shapeFun.values(center, null, resNodes);
            //TODO:
        }
    }

    double cubicTestFun(double x, double y) {
        return 3.5 + 4.2 * x + 6.3 * y + 2.2 * x * x + 1.7 * x * y + 3.9 * y * y + 7.2 * x * x * x + 0.7 * x * x * y + 0.2 * x * y * y + y * y * y;
    }

    ValueFun genExpFun(String choice) {
        String ch = choice.toLowerCase();
        switch (ch) {
            case "c":
            case "cubic":
                return new ValueFun() {

                    final double[] result = new double[3];

                    @Override
                    public double[] value(Coordinate c) {
                        double x = c.x, y = c.y;
                        result[0] = cubicTestFun(x, y);
                        result[1]=cubicXTestFun(x, y);
                        result[2]=cubicYTestFun(x, y);
                        return result;
                    }
                };
            case "s":
            case "sin":
                return new ValueFun() {

                    final double[] result = new double[3];

                    @Override
                    public double[] value(Coordinate c) {
                        double x = c.x, y = c.y;
                        result[0] = sinTestFun(x, y);
                        result[1]=sinXTestFun(x, y);
                        result[2]=sinYTestFun(x, y);
                        return result;
                    }
                };
            default:
                throw new IllegalArgumentException("Choice must come from \"c\", \"cubic\", \"s\" and \"sin\"");
        }
    }

    static double cubicXTestFun(double x, double y) {
        return 4.2 + 4.4 * x + 1.7 * y + 21.6 * x * x + 1.4 * x * y + 0.2 * y * y;
    }

    static double cubicYTestFun(double x, double y) {
        return 6.3 + 1.7 * x + 7.8 * y + 0.7 * x * x + 0.4 * x * y + 3 * y * y;
    }

    static double sinTestFun(double x, double y) {
        return Math.sin(x / (2 * PI)) * Math.sin(y / (2 * PI));
    }

    static double sinXTestFun(double x, double y) {
        return Math.cos(x / (2 * PI)) * Math.sin(y / (2 * PI)) / (2 * PI);
    }

    static double sinYTestFun(double x, double y) {
        return Math.sin(x / (2 * PI)) * Math.cos(y / (2 * PI)) / (2 * PI);
    }
}
