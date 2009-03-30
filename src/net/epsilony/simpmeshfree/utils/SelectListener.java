/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.utils;

import java.awt.image.BufferedImage;

/**
 *
 * @author epsilon
 */
public interface SelectListener {
    public boolean isRubberAutoClearRepaint();
    public void selecting(int x1,int y1,int x2,int y2,ModelPanelManager vt,BufferedImage rubberImage);
    public boolean selected(int x1,int y1,int x2,int y2,ModelPanelManager vt);
//    public void interrupted();
}
