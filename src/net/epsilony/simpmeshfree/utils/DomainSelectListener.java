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
public interface DomainSelectListener {
    public void searching(int x1,int y1,int x2,int y2,ViewTransform vt,BufferedImage rubberImage);
    public void searched(int x1,int y1,int x2,int y2,ViewTransform vt);
//    public void interrupted();
}
