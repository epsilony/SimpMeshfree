/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.shapefun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.epsilony.math.polynomial.BivariateBinomials;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.mechanics.RCMJni;
import net.epsilony.util.collection.LayeredDomainTree;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author epsilon
 */
public class GlobalRadialPolynomialShapeFunction implements ShapeFunction {

    RCMJni rcmJni = new RCMJni();
    RadialBasisFunction radialBasisFunction;
    LayeredDomainTree<Node> nodeSearchTree;
    double supportDomainSize;
    int nomialPower;
    int m;
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Node> supportNodes;
    UpperSymmBandMatrix gMat;
    DenseVector transVector;

    public GlobalRadialPolynomialShapeFunction(RadialBasisFunction radialBasisFunction, double supportDomainSize, Collection<Node> nodes, int nomialPower) {
        this.radialBasisFunction = radialBasisFunction;
        this.supportDomainSize = supportDomainSize;
        this.nodes.addAll(nodes);
        if (supportDomainSize > 0) {
            nodeSearchTree = new LayeredDomainTree<Node>(this.nodes, Point.compX, Point.compY, true);
        }
        this.nomialPower = nomialPower;
        if (nomialPower < 0) {
            m = 0;
        } else {
            m = (nomialPower * nomialPower + 3 * nomialPower + 2) / 2;
        }
        initiate();
    }

//    UpperSymmPackMatrix tempUpMat;
    private void initiate() {
        FlexCompRowMatrix tempMat = new FlexCompRowMatrix(m + nodes.size(), m + nodes.size());
        double[] binomials = new double[m];
        double nodex, nodey;
        double tds = supportDomainSize * supportDomainSize;
        if (supportDomainSize > 0) {
            supportNodes = new ArrayList<Node>(100);
        } else {
            supportNodes = nodes;
        }
        for (int i = 0; i < nodes.size(); i++) {
            nodex = nodes.get(i).getX();
            nodey = nodes.get(i).getY();
            nodes.get(i).setMatrixIndex(i);
            radialBasisFunction.setCenter(nodex, nodey);
            for (int j = i; j < nodes.size(); j++) {
                double xj = nodes.get(j).getX();
                double yj = nodes.get(j).getY();
                if (supportDomainSize < 0 || (xj - nodex) * (xj - nodex) + (yj - nodey) * (yj - nodey) <= tds) {
                    tempMat.set(i, j, radialBasisFunction.value(nodes.get(j).getX(), nodes.get(j).getY()));
                }
            }
            BivariateBinomials.getBinomials(nomialPower, nodex, nodey, binomials);
            for (int j = 0; j < m; j++) {
                tempMat.set(i, j + nodes.size(), binomials[j]);
            }
        }
//        tempUpMat=new UpperSymmPackMatrix(tempMat);
//        StringBuilder sb = new StringBuilder();
//        for(int i=0;i<tempMat.numRows();i++){
//            for(int j=0;j<tempMat.numColumns();j++){
//                sb.append(String.format("%10.2e ", tempMat.get(i, j)));
//            }
//            sb.append(String.format("%n"));
//        }
//        System.out.println(sb);
        DenseVector b = new DenseVector(nodes.size() + m);
        for (int i = 0; i < b.size(); i++) {
            b.set(i, i);
        }
        Object[] results = rcmJni.compile(tempMat, b);
        gMat = (UpperSymmBandMatrix) results[0];

//        sb = new StringBuilder();
//        for(int i=0;i<gMat.numRows();i++){
//            for(int j=0;j<gMat.numColumns();j++){
//                sb.append(String.format("%10.2e ", gMat.get(i, j)));
//            }
//            sb.append(String.format("%n"));
//        }
//        System.out.println(sb);

        transVector = (DenseVector) results[1];
        values = new DenseVector(nodes.size() + m);
        pxValues = new DenseVector(nodes.size() + m);
        pyValues = new DenseVector(nodes.size() + m);
        pxxValues = new DenseVector(nodes.size() + m);
        pxyValues = new DenseVector(nodes.size() + m);
        pyyValues = new DenseVector(nodes.size() + m);
        tVector = new DenseVector(nodes.size() + m);
    }
    private DenseVector values,  pxValues,  pyValues,  pxxValues,  pxyValues,  pyyValues,  tVector;

    @Override
    public Vector[] shapePartialValues(List<Node> nodes, double x, double y) {
        if (supportDomainSize > 0) {
            Node from = Node.tempNode(x - supportDomainSize, y - supportDomainSize);
            Node to = Node.tempNode(x + supportDomainSize, y + supportDomainSize);
            nodeSearchTree.domainSearch(supportNodes, from, to);
        }
        int[] PInv = rcmJni.getPInv();
        values.zero();
        double[] tds = new double[2];
        for (Node node : supportNodes) {
            radialBasisFunction.setCenter(node.getX(), node.getY());
            radialBasisFunction.partialDifferential(x, y, tds);
            pxValues.set(PInv[node.getMatrixIndex()] - 1, tds[0]);
            pyValues.set(PInv[node.getMatrixIndex()] - 1, tds[1]);
        }
        int[] P = rcmJni.getP();
        gMat.solve(pxValues, tVector);

        for (int i = 0; i < nodes.size(); i++) {
            pxValues.set(P[i] - 1, tVector.get(i));
        }
        gMat.solve(pyValues, tVector);
        for (int i = 0; i < nodes.size(); i++) {
            pyValues.set(P[i] - 1, tVector.get(i));
        }
        return new Vector[]{pxValues, pyValues};
    }

    @Override
    public Vector[] shapeQuadPartialValues(List<Node> nodes, double x, double y) {
        if (supportDomainSize > 0) {
            Node from = Node.tempNode(x - supportDomainSize, y - supportDomainSize);
            Node to = Node.tempNode(x + supportDomainSize, y + supportDomainSize);
            nodeSearchTree.domainSearch(supportNodes, from, to);
        }
        int[] PInv = rcmJni.getPInv();
        values.zero();
        double[] tds = new double[3];
        for (Node node : supportNodes) {
            radialBasisFunction.setCenter(node.getX(), node.getY());
            radialBasisFunction.quadPartialDifferential(x, y, tds);
            pxxValues.set(PInv[node.getMatrixIndex()] - 1, tds[0]);
            pxyValues.set(PInv[node.getMatrixIndex()] - 1, tds[1]);
            pyyValues.set(PInv[node.getMatrixIndex()] - 1, tds[2]);
        }
        int[] P = rcmJni.getP();
        gMat.solve(pxxValues, tVector);

        for (int i = 0; i < nodes.size(); i++) {
            pxxValues.set(P[i] - 1, tVector.get(i));
        }

        gMat.solve(pyyValues, tVector);
        for (int i = 0; i < nodes.size(); i++) {
            pyyValues.set(P[i] - 1, tVector.get(i));
        }
        gMat.solve(pxyValues, tVector);
        for (int i = 0; i < nodes.size(); i++) {
            pxyValues.set(P[i] - 1, tVector.get(i));
        }
        return new Vector[]{pxxValues, pxyValues, pyyValues};
    }

    @Override
    public Vector shapeValues(List<Node> nodes, double x, double y) {

        if (supportDomainSize > 0) {
            Node from = Node.tempNode(x - supportDomainSize, y - supportDomainSize);
            Node to = Node.tempNode(x + supportDomainSize, y + supportDomainSize);
            nodeSearchTree.domainSearch(supportNodes, from, to);
        }
        int[] PInv = rcmJni.getPInv();
        values.zero();
        for (Node node : supportNodes) {
            radialBasisFunction.setCenter(node.getX(), node.getY());
            values.set(PInv[node.getMatrixIndex()] - 1, radialBasisFunction.value(x, y));
        }
//        if(x==0&&y==0){
//            DenseVector tv=new DenseVector(values.size());
//            for(Node node:supportNodes){
//                radialBasisFunction.setCenter(node.getX(), node.getY());
//                tv.set(node.getMatrixIndex(),radialBasisFunction.value(x, y));
//            }
//            for(int i=0;i<tv.size();i++){
//                System.out.println(tv.get(i));
//            }
//            tempUpMat.solve(tv, tVector);
//            System.out.println("tVector:");
//            System.out.println(tVector);
//            System.out.println(transVector);
//            for(int i=0;i<PInv.length;i++){
//                System.out.println(PInv[i]);
//            }
//        }
        gMat.solve(values, tVector);
        int[] P = rcmJni.getP();
        for (int i = 0; i < this.nodes.size(); i++) {
            values.set(P[i] - 1, tVector.get(i));
        }
        return values;
    }
}
