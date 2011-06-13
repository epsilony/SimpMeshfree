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
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JPanel;
import net.epsilony.math.radialbasis.MultiQuadRadial;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model.geometry.BoundaryConditions;
import net.epsilony.simpmeshfree.model.geometry.GeometryModel;
import net.epsilony.simpmeshfree.model.geometry.LineSegment;
import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Segment;
import net.epsilony.simpmeshfree.model.mechanics.MechanicsModel;
import net.epsilony.simpmeshfree.model.mechanics.SupportDomains.SimpleRoundSupportDomain;
import net.epsilony.simpmeshfree.shapefun.RadialPolynomialShapeFunction;
import net.epsilony.simpmeshfree.utils.Constitutives;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
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

    double P = 1000;
    double E = 3e7;
    double nu = 0.3;
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
        lineSegment.addBoundaryCondition(BoundaryConditions.getTimoshenkoEssential(E, nu, P, L, D, lineSegment));
//        lineSegment.addBoundaryCondition(BoundaryConditions.getStretchEssential(0));
        gm.segmentSearch(47, -1, 49, 1, segs);
        if (log.isDebugEnabled()) {
            log.debug("segs:");
            for (Segment seg : segs) {
                log.debug(seg);
            }
        }
        LineSegment lineSegment2 = (LineSegment) segs.getFirst();
        lineSegment2.addBoundaryCondition(BoundaryConditions.getTimoshenkoNatural(E, nu, P, L, D, lineSegment));
//        lineSegment2.addBoundaryCondition(BoundaryConditions.getStretchEssential(1e-3));
//        lineSegment2.addBoundaryCondition(BoundaryConditions.getStretchNatural(1e3));
        mpm = new ModelPanelManager(panel, gm.getLeftDown().getX(), gm.getLeftDown().getY(), gm.getRightUp().getX(), gm.getRightUp().getY());
        mpm.addModelImagePainter(gm);

//        LinkedList<Node> nodes = new LinkedList<Node>();
//        for (int i = 0; i < 23; i++) {
//            for (int j = 0; j < 5; j++) {
//                nodes.add(new Node(i * 2 + 2.0, j * 2 - 4));
//            }
//        }
//        mm.getNodes().addAll(nodes);
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 4; j++) {
//                double[] tds = new double[4];
//                tds[0] = i * 4.8;
//                tds[1] = j * 3 - 6;
//                tds[2] = (i + 1) * 4.8;
//                tds[3] = (j + 1) * 3 - 6;
//                mm.getRectangleQuadratureDomains().add(tds);
//            }
//        }

//        mm.generateBoundaryNodeByApproximatePoints(2, 0.1);

        mm.generateNodesByTriangle(10, 0.5, "pqa0.5nQ", true, true);
        mm.generateQuadratureDomainsByTriangle(5, 0.25, "pqa0.25nQ");
        mm.generateQuadratureDomainsByTriangle();
        mm.setQuadratureNum(3);
        mpm.addModelImagePainter(mm);
        mm.setSupportDomain(new SimpleRoundSupportDomain(3, 9, 3, 20, gm, mm.getNodes()));
        RadialBasisFunction rbf = new MultiQuadRadial(1.5, 1.03);
        mm.setRadialBasisFunction(rbf);
        mm.setShapeFunction(new RadialPolynomialShapeFunction(rbf, 1));
        mm.setConstitutiveLaw(Constitutives.planeStressMatrix(E, nu));
//        SupportDomain supportDomain = mm.getSupportDomain();

//        supportDomain.boundarySupportNodes(lineSegment2, 0.0986, outputs);
//        for(Node nd:outputs){
//            System.out.println(nd.getX()+"  "+nd.getY());
//        }


        mpm.addModelImagePainter(new ModelImagePainter() {

            @Override
            public void paintModel(BufferedImage modelImage, ModelPanelManager manager) {
                Graphics2D g2 = (Graphics2D) modelImage.getGraphics();
//                long t3 = System.nanoTime();
//            g2.setRenderingHint(RenderingHints., rootPane)
                g2.setColor(Color.BLUE);
                double I = D * D * D / 12;
//            Path2D path=new Path2D.Double();
//                long tt = System.nanoTime();
                double[] posts = new double[mm.getNodes().size() * 2];
//                tt = System.nanoTime() - tt;
//                System.out.println("new double[] time:" + tt);
                int i = 0;
                AffineTransform tx = mpm.getViewTransform();
                Point2D ptSrc = new Point2D.Double();
                Point2D ptDst = new Point2D.Double();
                for (Node node : mm.getNodes()) {
                    double x = node.getX();
                    double y = node.getY();
                    double u = -P * y / (6 * E * I) * ((6 * L - 3 * x) * x + (2 + nu) * (y * y - D * D / 4));
                    double v = P / (6 * E * I) * (3 * nu * y * y * (L - x) + (4 + 5 * nu) * D * D * x / 4 + (3 * L - x) * x * x);
                    posts[i] = x + u * 500;
                    posts[i + 1] = y + v * 500;
                    i = i + 2;
                    ptSrc.setLocation(x, y);
                    tx.transform(ptSrc, ptDst);
                    double esux = u - node.getUx();
                    double esuy = v - node.getUy();
                    double es = Math.sqrt((esux * esux + esuy * esuy) / (u * u + v * v));
                    if (es > 0.001) {
//                        System.out.println(String.format("%%%3.1f x=%3.1f y=%3.1f u=%5e v=%5e uh=%5e vh=%5e", es * 100,x,y,u,v,node.getUx(),node.getUy()));
                        g2.drawString(String.format("%%%3.1f", es * 100), (int) ptDst.getX(), (int) ptDst.getY());
                    }
//                    if(x==0){
//                        System.out.println(String.format("y=%5e u=%5e v=%5e", y,u,v));
//                    }
                //path.append(mpm.viewMarker(x+u*500, y+v*500, 10, ModelPanelManager.ViewMarkerType.DownTriangle), false);

//                g2.draw(mpm.viewMarker(x+u*500, y+v*500, 10, ModelPanelManager.ViewMarkerType.DownTriangle));
                }
//                long ttshape = System.nanoTime();
                Shape tshape = mpm.viewMarker(posts, 10, ModelPanelManager.ViewMarkerType.Rectangle);
//                ttshape = System.nanoTime() - ttshape;
////                System.out.println("generateTime+" + ttshape);
//                long ttt = System.nanoTime();

                g2.draw(tshape);
//                ttt = System.nanoTime() - ttt;
//                System.out.println("draw time:" + ttt);
//                long t4 = System.nanoTime();
//                System.out.println("t4-t3=" + (t4 - t3));
            }
        });

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
        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(java.awt.Color.white);
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
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
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("jCheckBox1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jCheckBox1))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addContainerGap(706, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        // TODO add your handling code here:
        if (evt.getButton() == MouseEvent.BUTTON1) {
            LinkedList<Node> list = new LinkedList<Node>();
            Point2D pt = new Point2D.Double();
            try {
                mpm.inverseTransform(evt.getX(), evt.getY(), pt);
            } catch (NoninvertibleTransformException ex) {
                java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            mm.getSupportDomain().supportNodes(pt.getX(), pt.getY(), list);
            double min = Math.pow(list.getFirst().getX() - pt.getX(), 2) + Math.pow(list.getFirst().getY() - pt.getY(), 2);
            Node minNode = list.getFirst();
            for (Node node : list) {
                double td = Math.pow(node.getX() - pt.getX(), 2) + Math.pow(node.getY() - pt.getY(), 2);
                if (td < min) {
                    min = td;
                    minNode = node;
                }
            }
            double x = pt.getX();
            double y = pt.getY();
            double I = D * D * D / 12;
            double u = -P * y / (6 * E * I) * ((6 * L - 3 * x) * x + (2 + nu) * (y * y - D * D / 4));
            double v = P / (6 * E * I) * (3 * nu * y * y * (L - x) + (4 + 5 * nu) * D * D * x / 4 + (3 * L - x) * x * x);
            System.out.println(minNode.getX() + " " + minNode.getY() + " " + minNode.getUx() + " " + minNode.getUy() + " " + u + " " + v);
        }
    }//GEN-LAST:event_jPanel1MouseClicked
    Runnable task = new Runnable() {

        @Override
        public void run() {
//            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                mm.solve();
            } catch (ArgumentOutsideDomainException ex) {
                java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            mpm.viewWhole();
            mpm.repaintModel();
        }
    };
    ExecutorService es;
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        mm.setForceSingleCore(jCheckBox1.isSelected());
        if (null == es) {
            es = Executors.newSingleThreadExecutor();
            es.submit(task);
            es.shutdown();
        } else {
            if (es.isTerminated()) {
                es = Executors.newSingleThreadExecutor();
                es.submit(task);
                es.shutdown();
            } else {
                System.out.println("busy!");
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

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
//        new ModelTestFrame();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
