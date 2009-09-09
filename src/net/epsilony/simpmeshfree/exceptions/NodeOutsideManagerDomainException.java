/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.exceptions;
import net.epsilony.simpmeshfree.model2D.Node;
import net.epsilony.simpmeshfree.model2D.NodesManager;

/**
 *
 * @author epsilon
 */
public class NodeOutsideManagerDomainException extends Exception{

    public NodeOutsideManagerDomainException(Node node,NodesManager nm) {
        super("Node :"+node.getX()+" "+node.getY()+" is outside NodesManager:"+nm.getXMin()+" "+nm.getYMin()+" "+nm.getXMax()+" "+nm.getYMax()+" ");
    }
    

}
