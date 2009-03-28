/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.utils;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *
 * @author epsilon
 */
public interface ModelImageWriter {
    public void write(BufferedImage modelImage,AffineTransform tx);
}
