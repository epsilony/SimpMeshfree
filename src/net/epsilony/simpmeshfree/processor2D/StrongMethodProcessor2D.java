/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.processor2D;
import java.util.LinkedList;
import net.epsilony.simpmeshfree.model2D.BoundaryNode;
import net.epsilony.simpmeshfree.model2D.Node;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 *
 * @author epsilon
 */
public class StrongMethodProcessor2D {
    //the central matrix of Strong Method Processor:
    public FlexCompRowMatrix kMat;
    //the constitutive law matrix
    public DenseMatrix dMat;
    //the vector of force, boudary conditions
    public DenseVector bVector;

    public LinkedList<Node> nodes=new LinkedList<Node>();
    public LinkedList<BoundaryNode> boundaryNodes=new LinkedList<BoundaryNode>();

    public LinkedList<BoundaryNode> getBoundaryNodes() {
        return boundaryNodes;
    }

    public void setBoundaryNodes(LinkedList<BoundaryNode> boundaryNodes) {
        this.boundaryNodes = boundaryNodes;
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(LinkedList<Node> nodes) {
        this.nodes = nodes;
    }

    

}
