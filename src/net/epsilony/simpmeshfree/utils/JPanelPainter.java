/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.utils;

import java.awt.Graphics2D;

/**
 *
 * @author epsilon
 */
public interface JPanelPainter {
    public void paint(Graphics2D g2);
    public void setViewTransform(ViewTransform vt);
}
