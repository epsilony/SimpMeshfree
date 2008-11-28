/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.geom;

/**
 *
 * @author epsilon
 */
import static java.lang.Math.*;
import java.util.List;
import java.util.ArrayList;
import no.uib.cipr.matrix.*;

/**
 *
 * @author li
 */
public class NodesManager {

    List<NodeBucket> buckets = new ArrayList<NodeBucket>();

    public NodesManager(double xLeft, double yTop, double xRight, double yBottom) {
        NodeBucket overallBucket = new NodeBucket();
        overallBucket.xMin = xLeft;
        overallBucket.xMax = xRight;
        overallBucket.yMin = yBottom;
        overallBucket.yMax = yTop;
        overallBucket.node = new ArrayList<Node>();
        buckets.add(overallBucket);

    }

    void insertNode(Node n) {
        NodeBucket origBucket = null;
        for (int i = 0; i < buckets.size(); i++) {
            origBucket = buckets.get(i);
            double x = n.getX();
            double y = n.getY();
            if (x >= origBucket.xMin && x <= origBucket.xMax && y >= origBucket.yMin && y <= origBucket.yMax) {
                break;
            }
        }

        while (origBucket.num == origBucket.MAX) {
            NodeBucket subBucket = new NodeBucket();
            subBucket.node = new ArrayList<Node>();
            if ((origBucket.xMax - origBucket.xMin) > (origBucket.yMax - origBucket.yMin)) {
                double xMiddle = (origBucket.xMin + origBucket.xMax) / 2;
                if (n.getX() < xMiddle) {
                    subBucket.xMin = xMiddle;
                    subBucket.xMax = origBucket.xMax;
                    subBucket.yMin = origBucket.yMin;
                    subBucket.yMax = origBucket.yMax;
                    origBucket.xMax = xMiddle;
                } else {
                    subBucket.xMin = origBucket.xMin;
                    subBucket.xMax = xMiddle;
                    subBucket.yMin = origBucket.yMin;
                    subBucket.yMax = origBucket.yMax;
                    origBucket.xMin = xMiddle;
                }
            } else {
                double yMiddle = (origBucket.yMin + origBucket.yMax) / 2;
                if (n.getY() < yMiddle) {
                    subBucket.xMin = origBucket.xMin;
                    subBucket.xMax = origBucket.xMax;
                    subBucket.yMax = origBucket.yMax;
                    subBucket.yMin = yMiddle;
                    origBucket.yMax = yMiddle;
                } else {
                    subBucket.xMin = origBucket.xMin;
                    subBucket.xMax = origBucket.xMax;
                    subBucket.yMin = origBucket.yMin;
                    subBucket.yMax = yMiddle;
                    origBucket.yMin = yMiddle;
                }

            }
            for (int j = 0; j < origBucket.node.size(); j++) {
                if (origBucket.node.get(j).getX() >= subBucket.xMin && origBucket.node.get(j).getX() <= subBucket.xMax && origBucket.node.get(j).getY() >= subBucket.yMin && origBucket.node.get(j).getY() <= subBucket.yMax) {
                    subBucket.node.add(origBucket.node.get(j));
                    subBucket.num++;
                    origBucket.node.remove(j);
                    origBucket.num--;
                    j--;
                }
            }
            buckets.add(subBucket);
        }
        origBucket.node.add(n);
        origBucket.num++;
    }

    int get(double dis, Node key, List<Node> influenceNode) {
        int number = 0;
        for (int i = 0; i < buckets.size(); i++) {
            if (!(buckets.get(i).xMin > (key.getX() + dis) || buckets.get(i).xMax < (key.getX() - dis) && buckets.get(i).yMin > (key.getY() + dis) || buckets.get(i).yMax < (key.getY() - dis))) {
                for (int j = 0; j < buckets.get(i).node.size(); j++) {
                    Node inBucket = buckets.get(i).node.get(j);
                    if (sqrt((inBucket.getX() - key.getX()) * (inBucket.getX() - key.getX()) + (inBucket.getY() - key.getY()) * (inBucket.getY() - key.getY())) <= dis) {
                        number++;
                        influenceNode.add(inBucket);
                    }
                }
            }
        }
        return number;
    }

    double supportDomain(double r, double zeroDimensionValue, double crit, Node core, List<Node> supportNode) {
        double result;
        double test = 0;
        int n;
        do {
            supportNode.clear();
            n = get(r, core, supportNode);
            System.out.println(+n);
            result = sqrt(3.1415926 * r * r) / (sqrt(n) - 1) * zeroDimensionValue;
            test = (result - r) / r;
            if (test >= crit) {
                r = r * (1 + crit);
            } else {
                r = r * (1 - crit);
            }
        } while (abs(test) >= crit);
        return result;
    }

    public void getShapeFunValue(double rIni, double nonUnitValue, double criterion, Node center, List<Node> nodeInEffectDomain, double qVal, int p, Vector shapeFunValue, Vector deviXShapeFunValue, Vector deviYShapeFunValue) {
        double averageNodeDis = supportDomain(rIni, nonUnitValue, criterion, center, nodeInEffectDomain) / nonUnitValue;
        int num = nodeInEffectDomain.size();
        int m = 0;
        for (int i = 1; i <= p + 1; i++) {
            m = m + i;
        }
        double[] objectPointBasicFunValue = new double[num + m];
        double[][] polynomialArray = new double[num][m];
        UpperSymmDenseMatrix rMatrix = new UpperSymmDenseMatrix(num);
        UpperSymmDenseMatrix gUpperSymmMatrix = new UpperSymmDenseMatrix(num + m);
        for (int i = 0; i < num; i++) {
            for (int j = i; j < num; j++) {
                rMatrix.add(i, j, getRadialBasicFunValue(nodeInEffectDomain.get(i).x, nodeInEffectDomain.get(j).x, nodeInEffectDomain.get(i).y, nodeInEffectDomain.get(j).y, qVal, nonUnitValue, averageNodeDis));
            }
        }
        for (int i = 0; i < num; i++) {
            polynomialArray[i] = getPolyBasicFunValue(nodeInEffectDomain.get(i).x, nodeInEffectDomain.get(i).y, p, m);
        }
        for (int i = 0; i < num; i++) {
            for (int j = i; j < num; j++) {
                gUpperSymmMatrix.add(i, j, rMatrix.get(i, j));
            }
            for (int j = num; j < num + m; j++) {
                gUpperSymmMatrix.add(i, j, polynomialArray[i][j - num]);
            }
        }
        for (int i = num; i < num + m; i++) {
            for (int j = i; j < num + m; j++) {
                gUpperSymmMatrix.add(i, j, 0);
            }
        }
        for (int i = 0; i < num; i++) {
            objectPointBasicFunValue[i] = getRadialBasicFunValue(center.x, nodeInEffectDomain.get(i).x, center.y, nodeInEffectDomain.get(i).y, qVal, nonUnitValue, averageNodeDis);
        }
        double[] middle = new double[m];
        middle = getPolyBasicFunValue(center.x, center.y, p, m);
        for (int i = num; i < num + m; i++) {
            objectPointBasicFunValue[i] = middle[i - num];
        }
        DenseVector basicFunValueVector = new DenseVector(objectPointBasicFunValue);
        double[] deviXObjectPointBasicFunValue = new double[num + m];
        double[] deviYObjectPointBasicFunValue = new double[num + m];
        for (int i = 0; i < num; i++) {
            deviXObjectPointBasicFunValue[i] = getDeviXRadialBasicFunValue(nodeInEffectDomain.get(i).x, center.x, nodeInEffectDomain.get(i).y, center.y, qVal, nonUnitValue, averageNodeDis);
        }
        double[] xMiddle = new double[m];
        xMiddle = getDeviXPolyBasicFunValue(center.x, center.y, p);
        for (int i = num; i < num + m; i++) {
            deviXObjectPointBasicFunValue[i] = xMiddle[i - num];
        }
        DenseVector deviXBasicFunValueVector = new DenseVector(deviXObjectPointBasicFunValue);
        for (int i = 0; i < num; i++) {
            deviYObjectPointBasicFunValue[i] = getDeviYRadialBasicFunValue(nodeInEffectDomain.get(i).x, center.x, nodeInEffectDomain.get(i).y, center.y, qVal, nonUnitValue, averageNodeDis);
        }
        double[] yMiddle = new double[m];
        yMiddle = getDeviYPolyBasicFunValue(center.x, center.y, p);
        for (int i = num; i < num + m; i++) {
            deviYObjectPointBasicFunValue[i] = yMiddle[i - num];
        }
        DenseVector deviYBasicFunValueVector = new DenseVector(deviYObjectPointBasicFunValue);

        gUpperSymmMatrix.solve(basicFunValueVector, shapeFunValue);
        gUpperSymmMatrix.solve(deviXBasicFunValueVector, deviXShapeFunValue);
        gUpperSymmMatrix.solve(deviYBasicFunValueVector, deviYShapeFunValue);

    }

    double getRadialBasicFunValue(double x1, double x2, double y1, double y2, double q, double nonDimenVal, double d) {
        return pow((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + pow(nonDimenVal * d, 2), q);
    }

    double getDeviXRadialBasicFunValue(double x1, double x2, double y1, double y2, double q, double nonDimenVal, double d) {
        return q * pow((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + pow(nonDimenVal * d, 2), q - 1) * 2 * (x1 - x2);
    }

    double getDeviYRadialBasicFunValue(double x1, double x2, double y1, double y2, double q, double nonDimenVal, double d) {
        return q * pow((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + pow(nonDimenVal * d, 2), q - 1) * 2 * (y1 - y2);
    }

    double[] getPolyBasicFunValue(double x, double y, int p, int m) {
        double[] polyVector = new double[m];
        polyVector[0] = 1;
        for (int i = 1; i < p + 1; i++) {
            int n = 0;
            for (int k = 1; k <= i; k++) {
                n = n + k;
            }
            for (int j = 0; j <= i; j++) {
                polyVector[n + j] = pow(x, (i - j)) * pow(y, j);
            }
        }
        return polyVector;
    }

    double[] getDeviXPolyBasicFunValue(double x, double y, int p) {
        int m = 0;
        for (int i = 1; i <= p + 1; i++) {
            m = m + i;
        }
        double[] deviPolyVector = new double[m];
        deviPolyVector[0] = 0;
        for (int i = 1; i < p + 1; i++) {
            int n = 0;
            for (int k = 1; k <= i; k++) {
                n = n + k;
            }
            for (int j = 0; j <= i; j++) {
                deviPolyVector[n + j] = (i - j) * pow(x, (i - j - 1)) * pow(y, j);
            }
        }
        return deviPolyVector;
    }

    double[] getDeviYPolyBasicFunValue(double x, double y, int p) {
        int m = 0;
        for (int i = 1; i <= p + 1; i++) {
            m = m + i;
        }
        double[] deviPolyVector = new double[m];
        deviPolyVector[0] = 0;
        for (int i = 1; i < p + 1; i++) {
            int n = 0;
            for (int k = 1; k <= i; k++) {
                n = n + k;
            }
            for (int j = 0; j <= i; j++) {
                deviPolyVector[n + j] = j * pow(x, (i - j)) * pow(y, j - 1);
            }
        }
        return deviPolyVector;
    }
}

