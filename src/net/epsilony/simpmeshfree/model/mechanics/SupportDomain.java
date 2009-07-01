/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.io.Serializable;
import java.util.List;

import net.epsilony.simpmeshfree.model.geometry.Node;
import net.epsilony.simpmeshfree.model.geometry.Segment;

/**
 *
 * @author epsilon
 */
public interface SupportDomain extends Serializable{

    public double supportNodes(double x,double y,List<Node> list);

    public double boundarySupportNodes(Segment segment,double parm,List<Node> list);

    public SupportDomain CopyOf(boolean deep);
}
