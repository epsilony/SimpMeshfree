/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ElectricTestFrame.java
 *
 * Created on 2009-6-9, 14:49:40
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.swing.JPanel;
import net.epsilony.math.radialbasis.MultiQuadRadial;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.BoundaryCondition;
import net.epsilony.simpmeshfree.model.geometry.BoundaryConditions;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.LineSegment;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Point;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.mechanics.ElectricModel;
import net.epsilony.simpmeshfree.model.mechanics.MechanicsModel;
import net.epsilony.simpmeshfree.model.mechanics.SupportDomain;
import net.epsilony.simpmeshfree.model.mechanics.SupportDomains.SimpleRoundSupportDomain;
import net.epsilony.simpmeshfree.shapefun.RadialPolynomialShapeFunction;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.util.ui.geom.ShapeUtils;
import no.uib.cipr.matrix.Vector;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author epsilon
 */
public class ElectricTestFrame extends javax.swing.JFrame {

    static Logger log = Logger.getLogger(ElectricTestFrame.class);

    public class ModelPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

                super.paintComponent(g);

            mpm.paintPanel((Graphics2D) g);
        }
    }
    LinkedList<Node> outputs = new LinkedList<Node>();
    ModelPanel panel = new ModelPanel();
    ModelPanelManager mpm;
    GeometryModel gm = new GeometryModel();
    ElectricModel em = new ElectricModel(gm);


    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        log.setLevel(Level.DEBUG);
        Logger.getLogger(MechanicsModel.class.getName() + ".deep1").setLevel(Level.DEBUG);
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            Appender at = (Appender) allAppenders.nextElement();
            at.setLayout(new PatternLayout("%r [%t] %-5p %c{3} %x - %m%n"));
        }
        Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(0, 100);
        path.lineTo(100, 100);
        path.lineTo(100, 0);
        path.lineTo(90, 0);
        path.lineTo(70, 0);
        path.lineTo(30, 0);
        path.lineTo(10, 0);
        path.lineTo(0, 0);
        path.closePath();



        System.out.println(ShapeUtils.toString(path.createTransformedShape(null)));
        gm.addShape(path.createTransformedShape(null));

        gm.compile(0.1, 0.1);
        LinkedList<Segment> segs = new LinkedList<Segment>();
        gm.segmentSearch(19, -1, 21, 1, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        LineSegment lineSegment = (LineSegment) segs.getFirst();
        BoundaryCondition oneV = BoundaryConditions.getConstantEssentialBoundaryConditions(1, true, 0, false);
        lineSegment.addBoundaryCondition(oneV);
        gm.segmentSearch(79, -1, 81, 1, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        lineSegment = (LineSegment) segs.getFirst();
        lineSegment.addBoundaryCondition(oneV);

        gm.segmentSearch(49, 99, 51, 101, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        LineSegment lineSegment2 = (LineSegment) segs.getFirst();
        BoundaryCondition groundV = BoundaryConditions.getConstantEssentialBoundaryConditions(0, true, 0, false);
        lineSegment2.addBoundaryCondition(groundV);
        mpm = new ModelPanelManager(panel, gm.getLeftDown().getX(), gm.getLeftDown().getY(), gm.getRightUp().getX(), gm.getRightUp().getY());
        mpm.addModelImagePainter(gm);

        em.generateNodesByTriangle(2, 0.1, "pqa2nQ", true, true);
        em.generateQuadratureDomainsByTriangle(4, 0.05, "pqa8nQ");

        em.setQuadN(3);
        mpm.addModelImagePainter(em);
        em.setSupportDomain(new SimpleRoundSupportDomain(5, 8, 3, 6, gm, em.getNodes()));
        RadialBasisFunction rbf = new MultiQuadRadial(5, 1.03);
        em.setRadialBasisFunction(rbf);
        em.setShapeFunction(new RadialPolynomialShapeFunction(rbf, 2));

        try {
            em.solve();
        } catch (ArgumentOutsideDomainException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        ArrayList<Point> samplePoints = new ArrayList<Point>(400);
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                samplePoints.add(Point.tempPoint(i * 3.3 + 2.1, j * 3.3 + 2.1));
            }
        }
        em.setGrandSamplePoints(samplePoints);
        em.setDisplayFactor(2e2);
        em.setShowNodes(false);
        em.setShowTriangleDomain(false);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("./electric.dat"));
            ArrayList<Node> supportNodes = new ArrayList<Node>(100);
            Vector values = null;
            SupportDomain postSupportDomain = new SimpleRoundSupportDomain(7, 10, 3, 6, gm, em.getNodes());
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    double x = 0.5 + i * 1.0;
                    double y = 0.5 + j * 1.0;
                    double nodesAverDistance = postSupportDomain.supportNodes(x, y, supportNodes);
                    em.getRadialBasisFunction().setNodesAverageDistance(nodesAverDistance);
                    values = em.getShapeFunction().shapeValues(supportNodes, x, y);
                    double u = 0;
                    for (int k = 0; k < supportNodes.size(); k++) {
                        u += supportNodes.get(k).getUx() * values.get(k);
                    }
                    bw.append(String.format("%10.3f%10.3f%20.16f%n", x, y, u));
                }
            }
            bw.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ElectricTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    /** Creates new form ElectricTestFrame */
    public ElectricTestFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = panel;

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(javax.swing.UIManager.getDefaults().getColor("white"));

        jPanel1.setBackground(java.awt.Color.white);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 770, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ElectricTestFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
