/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.processor2D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import net.epsilony.jni.RCMJni;
import net.epsilony.simpmeshfree.modeltest.ModelTestFrame;
import net.epsilony.simpmeshfree.model2D.BoundaryCondition;
import net.epsilony.simpmeshfree.model2D.Model;
import net.epsilony.simpmeshfree.model2D.Node;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
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
public class MechanicsModel extends WeakMethodProcessor2D implements ModelImagePainter {

    static class MechanicsModelCore implements WeakMethodCore {

        double d00;// = constitutiveLaw.get(0, 0);
        double d01;// = constitutiveLaw.get(0, 1);
        double d10;// = constitutiveLaw.get(1, 0);
        double d11;// = constitutiveLaw.get(1, 1);
        double d22;// = constitutiveLaw.get(2, 2);

        

        @Override
        public void essentialBoundaryConditionCore(double[] values, int index, byte flag, FlexCompRowMatrix matrix, DenseVector vector) {
            int rowcol = index * 2;
            if ((BoundaryCondition.X & flag) == BoundaryCondition.X) {
                double ux = values[0];
                double kk = 0;

                for (int i = 0; i < rowcol; i++) {
                    kk = matrix.get(i, rowcol);
                    if (0 != kk) {
                        vector.add(i, -kk * ux);
                        matrix.set(i, rowcol, 0);
                    }
                }
                for (VectorEntry vn : matrix.getRow(rowcol)) {
                    vector.add(vn.index(), -vn.get() * ux);
                }
                matrix.setRow(rowcol, new SparseVector(matrix.numColumns()));
                matrix.set(rowcol, rowcol, 1);
                vector.set(rowcol, ux);
            }

            if ((BoundaryCondition.Y & flag) == BoundaryCondition.Y) {
                double uy = values[1];
                double kk = 0;
                for (int i = 0; i < rowcol + 1; i++) {
                    kk = matrix.get(i, rowcol + 1);
                    if (kk != 0) {
                        vector.add(i, -kk * uy);
                        matrix.set(i, rowcol + 1, 0);
                    }
                }
                for (VectorEntry vn : matrix.getRow(rowcol + 1)) {
                    vector.add(vn.index(), -vn.get() * uy);
                }
                matrix.setRow(rowcol + 1, new SparseVector(matrix.numColumns()));
                matrix.set(rowcol + 1, rowcol + 1, 1);
                vector.set(rowcol + 1, uy);
            }
        }

        @Override
        public void quadrateCore(int mIndex, double dphim_dx, double dphim_dy, int nIndex, double dphin_dx, double dphin_dy, double coefs, FlexCompRowMatrix matrix) {
            double td;
            mIndex *= 2;
            nIndex *= 2;

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
        public void natureConBoundaryQuadrateCore(double[] values, int index, double phi, DenseVector vector) {
            vector.add(index * 2, phi * values[1]);
            vector.add(index * 2 + 1, phi * values[2]);
        }

        @Override
        public void natureBoundaryQudarateCore(double[] values, int index, double phi, double coef, DenseVector vector) {
            vector.add(index * 2, phi * values[0] * coef);
            vector.add(index * 2 + 1, phi * values[1] * coef);
        }

        @Override
        public FlexCompRowMatrix initialMatrixK(int nodesSize) {
            return new FlexCompRowMatrix(nodesSize*2, nodesSize*2);
        }
    }
    /**
     * 设置本构矩阵
     * @param constitutiveLaw
     */
    transient static Logger logDeep = Logger.getLogger(MechanicsModel.class.getName() + ".deep1");
    transient int logi;

    public MechanicsModel(Model gm) {
        super(new MechanicsModelCore() ,gm);
    }


    transient boolean showDisplacedNodes = true;
    transient Color nodesDisplacedColor = Color.lightGray;
    transient double displaceFactor = 500;

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
    transient private RCMJni rcmJni;

    public void solve() throws ArgumentOutsideDomainException, Exception {
        log.info("Start solve()");
        initNodesMatrixIndex();
        quadrateDomains();
        bVector = new DenseVector(nodes.size() * 2);
        natureBoundaryQuadrate(quadratureNum);
        applyEssentialBoundaryConditions();
        rcmJni = new RCMJni();
        Object[] results = rcmJni.compile(matK, bVector);
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
    transient DenseMatrix constitutiveLaw = null;
    public void setConstitutiveLaw(Matrix constitutiveLaw) {
//        this.constitutiveLaw = new DenseMatrix(constitutiveLaw);
            MechanicsModelCore mechanicsModelCore=(MechanicsModelCore) modelCore;
            mechanicsModelCore.d00 = constitutiveLaw.get(0, 0);
            mechanicsModelCore.d01 = constitutiveLaw.get(0, 1);
            mechanicsModelCore.d10 = constitutiveLaw.get(1, 0);
            mechanicsModelCore.d11 = constitutiveLaw.get(1, 1);
            mechanicsModelCore.d22 = constitutiveLaw.get(2, 2);
        }
}