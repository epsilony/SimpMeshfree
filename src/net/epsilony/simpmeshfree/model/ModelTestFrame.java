/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModelTestFrame.java
 *
 * Created on 2009-5-13, 21:27:52
 */
package net.epsilony.simpmeshfree.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.swing.JPanel;
import net.epsilony.math.radialbasis.MultiQuadRadial;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.BoundaryConditions;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.LineSegment;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.mechanics.MechanicsModel;
import net.epsilony.simpmeshfree.model.mechanics.MechanicsModel.SimpleRoundSupportDomain;
import net.epsilony.simpmeshfree.model.mechanics.SupportDomain;
import net.epsilony.simpmeshfree.shapefun.RadialPolynomialShapeFunction;
import net.epsilony.simpmeshfree.utils.Constitutives;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.util.ui.geom.ShapeUtils;
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
public class ModelTestFrame extends javax.swing.JFrame {

    double P = -1000;
    double E = 3e7;
    double v = 0.3;
    double D = 12;
    double L = 48;
    static Logger log = Logger.getLogger(ModelTestFrame.class);

    /** Creates new form ModelTestFrame */
    public ModelTestFrame() {
        initComponents();
    }

    public class ModelPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            mpm.paintPanel((Graphics2D) g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLUE);
//            for (BoundaryNode bn : mm.getBoundaryNodes()) {
//                if (bn.getSegment() == null) {
//                    g2.draw(mpm.viewMarker(bn.getX(), bn.getY(), 10, ModelPanelManager.ViewMarkerType.UpTriangle));
//                }
//            }
          
                g2.draw(mpm.viewMarker(outputs, 10,ModelPanelManager.ViewMarkerType.UpTriangle));

        }
    }
    LinkedList<Node> outputs = new LinkedList<Node>();
    ModelPanel panel = new ModelPanel();
    ModelPanelManager mpm;
    GeometryModel gm = new GeometryModel();
    MechanicsModel mm = new MechanicsModel(gm);


    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger(MechanicsModel.class.getName() + ".deep1").setLevel(Level.DEBUG);
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            Appender at = (Appender) allAppenders.nextElement();
            at.setLayout(new PatternLayout("%r [%t] %-5p %c{3} %x - %m%n"));
        }
        Shape rect = new Rectangle2D.Double(0, -6, 48, 12);
        log.debug(rect.getPathIterator(null).getWindingRule());
        log.debug(PathIterator.WIND_NON_ZERO);
        Area area = new Area(rect);
        System.out.println(ShapeUtils.toString(area));

//        area.subtract(new Area(new Ellipse2D.Double(21, -3, 6, 6)));
//
//        area.subtract(new Area(new Ellipse2D.Double(3, -2, 4, 4)));
        System.out.println(ShapeUtils.toString(area));
        gm.addShape(area);

        gm.compile(0.1, 0.1);
        LinkedList<Segment> segs = new LinkedList<Segment>();
        gm.segmentSearch(-1, -1, 1, 1, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        LineSegment lineSegment = (LineSegment) segs.getFirst();
//        lineSegment.addBoundaryCondition(BoundaryConditions.getTimoshenkoEssential(E, v, P, L, D, lineSegment));
        lineSegment.addBoundaryCondition(BoundaryConditions.getStretchEssential(0));
        gm.segmentSearch(47, -1, 49, 1, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        LineSegment lineSegment2 = (LineSegment) segs.getFirst();
//        lineSegment2.addBoundaryCondition(BoundaryConditions.getTimoshenkoNatural(E, v, P, L, D, lineSegment));
        lineSegment2.addBoundaryCondition(BoundaryConditions.getStretchEssential(1e-3));
        mpm = new ModelPanelManager(panel, gm.getLeftDown().getX(), gm.getLeftDown().getY(), gm.getRightUp().getX(), gm.getRightUp().getY());
        mpm.addModelImagePainter(gm);

        mm.generateNodesByTriangle(1, 0.1, "pqa0.5nQ", true, true);
        mm.generateQuadratureDomainsByTriangle(2, 0.1, "pqa2nQ");
//        mm.generateQuadratureDomainsByTriangle();
        mm.setQuadN(5);
        mpm.addModelImagePainter(mm);
        mm.setSupportDomain(mm.new SimpleRoundSupportDomain(4, 8, 3, 6));
        RadialBasisFunction rbf = new MultiQuadRadial(3, 1.03);
        mm.setRadialBasisFunction(rbf);
        mm.setShapeFunction(new RadialPolynomialShapeFunction(rbf, 1));
        mm.setConstitutiveLaw(Constitutives.planeStressMatrix(E, v));
        SupportDomain supportDomain = mm.getSupportDomain();

        supportDomain.boundarySupportNodes(lineSegment2, 0.0986, outputs);
//        for(Node nd:outputs){
//            System.out.println(nd.getX()+"  "+nd.getY());
//        }
        try {
            mm.solve();
        } catch (ArgumentOutsideDomainException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
//

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
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(java.awt.Color.white);
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clickTestSupportDomain(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 942, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 752, Short.MAX_VALUE)
        );

        jButton1.setText("jButton1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addContainerGap(731, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clickTestSupportDomain(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clickTestSupportDomain
        // TODO add your handling code here:
        Point2D dstPt = new Point2D.Double();
        try {
            mpm.inverseTransform(evt.getX(), evt.getY(), dstPt);
        } catch (NoninvertibleTransformException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        mm.getSupportDomain().supportNodes(dstPt.getX(),dstPt.getY(),outputs);
        repaint();
    }//GEN-LAST:event_clickTestSupportDomain

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new ModelTestFrame().setVisible(true);
            }
        });

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
