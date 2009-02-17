/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModelPanel.java
 *
 * Created on 2009-2-17, 14:45:18
 */

package net.epsilony.simpmeshfree.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

/**
 *
 * @author epsilon
 */
public class ModelPanel extends javax.swing.JPanel {


    /** Creates new form ModelPanel */
    public ModelPanel() {
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

        setBackground(java.awt.Color.white);
        setAutoscrolls(true);
        setPreferredSize(new java.awt.Dimension(300, 300));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    NodesManager nodesManager=null;
    boolean showBuckets;
    double nodesRadiu=5;
    double coorX=5;
    double coorY=getHeight()-5;

    public void setNodesManager(NodesManager nodesManager) {
        this.nodesManager = nodesManager;
    }
    

    public void setNodesRadiu(double nodesRadiu) {
        this.nodesRadiu = nodesRadiu;
    }

    public void setShowBuckets(boolean showBuckets) {
        this.showBuckets = showBuckets;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        viewTransform(g2);
        if(showBuckets&&null!=nodesManager){
            g2.draw(nodesManager.getBucketsShape());
        }
        System.out.println("paintComponent");
    }
    
    private void viewTransform(Graphics2D g2){
        AffineTransform tx=new AffineTransform();
        tx.translate(0, 400);
        tx.quadrantRotate(3);
        g2.setTransform(tx);
    }

}
