/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import net.epsilony.simpmeshfree.utils.JPanelPainter;
import net.epsilony.simpmeshfree.utils.ViewTransform;

/**
 *
 * @author epsilon
 */
public class GeneticBandWidthReducerOperator implements JPanelPainter,MouseListener{
    private GeneticBandWidthReducer geneticBandWidthReducer;
    private ViewTransform vt;


    public GeneticBandWidthReducerOperator(GeneticBandWidthReducer geneticBandWidthReducer) {
        this.geneticBandWidthReducer = geneticBandWidthReducer;
    }


    @Override
    public void paintPanel(Graphics2D g2) {
        
    }

   

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
