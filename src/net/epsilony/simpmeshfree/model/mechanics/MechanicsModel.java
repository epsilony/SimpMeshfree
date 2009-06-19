/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.mechanics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import net.epsilony.math.util.EYMath;
import net.epsilony.math.util.AreaCoordTriangleQuadrature;
import net.epsilony.simpmeshfree.model.ModelTestFrame;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Logger;

/**
 * <br>RPIM法无网格法</br>
 * <br>正确运行本类的一个最简单代法见{@link ModelTestFrame}的构造部分</br>
 * <br>其中至少需要调用以下函数以完成一个次最简单的计算</br>
 * <br>{@link MechanicsModel#setConstitutiveLaw(no.uib.cipr.matrix.Matrix) }，</br>
 * <br>{@link MechanicsModel#setRadialBasisFunction(net.epsilony.math.radialbasis.RadialBasisFunction) }</br>
 * <br>{@link MechanicsModel#setShapeFunction(net.epsilony.simpmeshfree.shapefun.ShapeFunction) }</br>
 * <br>{@link MechanicsModel#setSupportDomain(net.epsilony.simpmeshfree.model.mechanics.SupportDomain) }</br>
 * @author epsilon
 */
public class MechanicsModel extends AbstractModel implements ModelImagePainter {

    /**
     * 设置本构矩阵
     * @param constitutiveLaw
     */
    public void setConstitutiveLaw(Matrix constitutiveLaw) {
        this.constitutiveLaw = new DenseMatrix(constitutiveLaw);
    }
    static Logger logDeep = Logger.getLogger(MechanicsModel.class.getName() + ".deep1");
    int logi;

    public MechanicsModel(GeometryModel gm) {
        this.gm = gm;
    }

    public void quadrateTriangleDomainsByAreaCoord(int qn) throws ArgumentOutsideDomainException {
        log.info(String.format("Start quadrateTriangleDomainsByAreaCoord(%d)", qn));
        double x, y, w, area;
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;
        kMat = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);
        DenseMatrix bk = new DenseMatrix(3, 2);
        DenseMatrix bl = new DenseMatrix(3, 2);
        DenseMatrix kkl = new DenseMatrix(2, 2);
        DenseMatrix tempMat = new DenseMatrix(2, 3);
        double[] weights = AreaCoordTriangleQuadrature.getWeights(qn);
        double[] areaCoords = AreaCoordTriangleQuadrature.getAreaCoordinates(qn);
        int i = 0, k, l;

        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(areaCoords));
        }
        logi = 0;
        int tsum = 0;
        for (double[] triangleDomain : triangleQuadratureDomains) {


            if (log.isDebugEnabled()) {
                log.debug(String.format("%d: triangleDomain %s", logi, Arrays.toString(triangleDomain)));
                logi++;
            }
            double x1 = triangleDomain[0];
            double y1 = triangleDomain[1];
            double x2 = triangleDomain[2];
            double y2 = triangleDomain[3];
            double x3 = triangleDomain[4];
            double y3 = triangleDomain[5];
            area = Math.abs((x1 - x2) * (y3 - y2) - (x3 - x2) * (y1 - y2)) / 2;

            for (i = 0; i < weights.length; i++) {
                x = x1 * areaCoords[i * 3] + x2 * areaCoords[i * 3 + 1] + x3 * areaCoords[i * 3 + 2];
                y = y1 * areaCoords[i * 3] + y2 * areaCoords[i * 3 + 1] + y3 * areaCoords[i * 3 + 2];
                if (logDeep.isDebugEnabled()) {
                    double vec1 = EYMath.vectorProduct(x - x1, y - y1, x2 - x1, y2 - x1);
                    double vec2 = EYMath.vectorProduct(x - x1, y - y1, x3 - x1, y3 - x1);
                    boolean out = false;
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    vec1 = EYMath.vectorProduct(x - x2, y - y2, x1 - x2, y1 - y2);
                    vec2 = EYMath.vectorProduct(x - x2, y - y2, x3 - x2, y3 - y2);
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    vec1 = EYMath.vectorProduct(x - x3, y - y3, x1 - x3, y1 - y3);
                    vec2 = EYMath.vectorProduct(x - x3, y - y3, x2 - x3, y2 - y3);
                    if (vec1 * vec2 > 0) {
                        out = true;
                    }
                    if (out) {
                        tsum++;
                        logDeep.debug(String.format("%d:point(%.2f,%.2f)is outof %s", tsum, x, y, Arrays.toString(triangleDomain)));
                    }
                }
                w = weights[i];
                nodesAverDistance = supportDomain.supportNodes(x, y, supportNodes);
                radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                partialValues = shapeFunction.shapePartialValues(supportNodes, x, y);
                for (k = 0; k < supportNodes.size(); k++) {
                    int kIndex = supportNodes.get(k).getMatrixIndex() * 2;

                    for (l = 0; l < supportNodes.size(); l++) {
                        int lIndex = supportNodes.get(l).getMatrixIndex() * 2;
                        if (k < l) {
                            continue;
                        }
                        bk.set(0, 0, partialValues[0].get(k));
                        bk.set(1, 1, partialValues[1].get(k));
                        bk.set(2, 0, partialValues[1].get(k));
                        bk.set(2, 1, partialValues[0].get(k));
                        bl.set(0, 0, partialValues[0].get(l));
                        bl.set(1, 1, partialValues[1].get(l));
                        bl.set(2, 0, partialValues[1].get(l));
                        bl.set(2, 1, partialValues[0].get(l));
                        bk.transAmult(constitutiveLaw, tempMat);
                        tempMat.mult(bl, kkl);
//                        if(logDeep.isDebugEnabled()){
//                            logDeep.debug("bkbl");
//                            logDeep.debug(bk);
//                            logDeep.debug(bl);
//                            logDeep.debug("kkl:");
//                            logDeep.debug(kkl);
//                            logDeep.debug(constitutiveLaw);
//                        }

                        kMat.add(kIndex, lIndex, kkl.get(0, 0) * w * area);
                        kMat.add(kIndex, lIndex + 1, kkl.get(0, 1) * w * area);
                        kMat.add(kIndex + 1, lIndex + 1, kkl.get(1, 1) * w * area);
                        kMat.add(kIndex + 1, lIndex, kkl.get(1, 0) * w * area);
                    }
                }
            }
        }


        log.info("End of quadrateTriangleDomainsByAreaCoord");
    }
    boolean showDisplacedNodes = true;
    Color nodesDisplacedColor = Color.lightGray;
    double displaceFactor = 500;

    public double getDisplaceFactor() {
        return displaceFactor;
    }

    public void setDisplaceFactor(double displaceFactor) {
        this.displaceFactor = displaceFactor;
    }

    @Override
    public void paintModel(BufferedImage modelImage, ModelPanelManager manager) {
        super.paintModel(modelImage, manager);
        Graphics2D g2 = modelImage.createGraphics();


        g2.setComposite(AlphaComposite.Clear);
        //g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());

        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();

        path.reset();
        if (showDisplacedNodes) {
            for (Node node : nodes) {
                manager.viewMarker(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy(), nodesScreenSize, nodesScreenType, path);

            }

            for (Node node : boundaryNodes) {
                manager.viewMarker(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy(), boundaryNodesScreenSize, boundaryNodesScreenType, path);
            }


            g2.setColor(nodesDisplacedColor);
            g2.draw(path.createTransformedShape(null));
            path.reset();
            for (Node node : nodes) {
                path.moveTo(node.getX(), node.getY());
                path.lineTo(node.getX() + displaceFactor * node.getUx(), node.getY() + displaceFactor * node.getUy());
            }
            g2.setColor(Color.BLUE);
            g2.draw(path.createTransformedShape(tx));
        }





    }
    private RCMJni rcmJni;

    public void solve() throws ArgumentOutsideDomainException {
        log.info("Start solve()");
        initNodesMatrixIndex();
//        quadrateTriangleDomainsByGrid(quadratureNum);
//        initialKMatrix();
//        quadrateRectangleDomains();
        quadrateDomains();
        bVector = new DenseVector(nodes.size() * 2);
        natureBoundaryQuadrate(quadratureNum);
        applyAccurateEssentialBoundaryConditions();
        rcmJni = new RCMJni();
        Object[] results = rcmJni.compile(kMat, bVector);
        log.info("solve the Ax=b now");
        xVector = new DenseVector(bVector.size());
        ((UpperSymmBandMatrix) results[0]).solve((DenseVector) results[1], xVector);
        log.info("Finished: solve the Ax=b");
        fillDisplacement();
        log.info("End of solve()");
    }

    public void fillDisplacement() {
        int index1, index2;
        log.info("edit the nodes ux uy data");
        for (Node node : nodes) {
            index1 = rcmJni.PInv[node.getMatrixIndex() * 2] - 1;
            index2 = rcmJni.PInv[node.getMatrixIndex() * 2 + 1] - 1;
            node.setUx(xVector.get(index1));
            node.setUy(xVector.get(index2));
        }

    }

    private void initNodesMatrixIndex() {
        int i = 0;
        for (Node node : nodes) {
            node.setMatrixIndex(i);
            i++;
        }
    }

    public Matrix getConstitutiveLaw() {
        return constitutiveLaw;
    }
    DenseMatrix constitutiveLaw = null;

    @Override
    public void quadrateCore(int mIndex, double dphim_dx, double dphim_dy, int nIndex, double dphin_dx, double dphin_dy, double coefs,FlexCompRowMatrix matrix) {
        double td;
        mIndex *= 2;
        nIndex *= 2;
        double d00 = constitutiveLaw.get(0, 0);
        double d01 = constitutiveLaw.get(0, 1);
        double d10 = constitutiveLaw.get(1, 0);
        double d11 = constitutiveLaw.get(1, 1);
        double d22 = constitutiveLaw.get(2, 2);
        td = dphim_dx * d00 * dphin_dx + dphim_dy * d22 * dphin_dy;

        matrix.add(mIndex, nIndex, coefs * td);
        td = dphim_dx * d01 * dphin_dy + dphim_dy * d22 * dphin_dx;
        matrix.add(mIndex, nIndex + 1, coefs * td);
        td = dphim_dy * d11 * dphin_dy + dphim_dx * d22 * dphin_dx;
        matrix.add(mIndex + 1, nIndex + 1, coefs * td);
        if (mIndex != nIndex) {
            td = dphim_dy * d10 * dphin_dx + dphim_dx * d22 * dphin_dy;
            matrix.add(mIndex + 1, nIndex, coefs * td);
        }
    }

    @Override
    public void initialKMatrix() {
        kMat = new FlexCompRowMatrix(nodes.size() * 2, nodes.size() * 2);
    }

    @Override
    public void natureConBoundaryQuadrateCore(double[] values, int index, double phi) {
        bVector.add(index * 2, phi * values[1]);
        bVector.add(index * 2 + 1, phi * values[2]);
    }

    @Override
    public void natureBoundaryQudarateCore(double[] values, int index, double phi, double coefs) {
        bVector.add(index * 2, phi * values[0] * coefs);
        bVector.add(index * 2 + 1, phi * values[1] * coefs);
    }

    @Override
    public void accurateEssentialCore(double[] values, int index, byte flag) {
        int rowcol = index * 2;
        if ((BoundaryCondition.X & flag) == BoundaryCondition.X) {
            double ux = values[0];
            double kk = 0;

            for (int i = 0; i < rowcol; i++) {
                kk = kMat.get(i, rowcol);
                if (0 != kk) {
                    bVector.add(i, -kk * ux);
                    kMat.set(i, rowcol, 0);
                }
            }
            for (VectorEntry vn : kMat.getRow(rowcol)) {
                bVector.add(vn.index(), -vn.get() * ux);
            }
            kMat.setRow(rowcol, new SparseVector(kMat.numColumns()));
            kMat.set(rowcol, rowcol, 1);
            bVector.set(rowcol, ux);
        }

        if ((BoundaryCondition.Y & flag) == BoundaryCondition.Y) {
            double uy = values[1];
            double kk = 0;
            for (int i = 0; i < rowcol + 1; i++) {
                kk = kMat.get(i, rowcol + 1);
                if (kk != 0) {
                    bVector.add(i, -kk * uy);
                    kMat.set(i, rowcol + 1, 0);
                }
            }
            for (VectorEntry vn : kMat.getRow(rowcol + 1)) {
                bVector.add(vn.index(), -vn.get() * uy);
            }
            kMat.setRow(rowcol + 1, new SparseVector(kMat.numColumns()));
            kMat.set(rowcol + 1, rowcol + 1, 1);
            bVector.set(rowcol + 1, uy);
        }
    }
}
