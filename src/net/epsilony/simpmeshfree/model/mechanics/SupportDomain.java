/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.mechanics;

import java.util.List;
import net.epsilony.simpmeshfree.model.geometry.Node;

/**
 *
 * @author epsilon
 */
public interface SupportDomain {
    public List<Node> supportNode(Node n,List<Node> list);

    public List<Node> supportNode(double x,double y,List<Node> list);
}