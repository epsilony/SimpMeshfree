/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2D;

import java.util.LinkedList;
import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class ModelPreprocessorCores {

    public static Logger log = Logger.getLogger(ModelPreprocessorCores.class);

    public static ModelPreprocessorCore autoTriangleCore(Model gm, double quadratureDomainSize, double quadratureDomainsFlatness, String quadratureDomainSwitch, boolean generateNodeByTriangle, double nodeSize, double nodesFlatness, String nodeSwitch) {
        TriangleJni triangleJni = null;
        if (generateNodeByTriangle) {
            log.info(String.format("Start generateNodesByTiangle%nsize=%6.3e flatness=%6.3e s=%s", nodeSize, nodesFlatness, nodeSwitch));

            //reset the node start number!
            Node tn = Node.tempNode(0, 0);
            tn.getIndexManager().reset();

            LinkedList<Node> nodes = new LinkedList<Node>();
            LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();

            triangleJni = new TriangleJni();
            gm.generateApproximatePoints(nodeSize, nodesFlatness);
            triangleJni.complie(gm, nodeSwitch);
            nodes.addAll(triangleJni.getNodes(true));
            for (Node n : nodes) {
                if (n.type() == ModelElementType.BoundaryNode) {
                    boundaryNodes.add((BoundaryNode) n);
                }
            }
            log.info(String.format("End of generateNodesByTriangle in autoTriangle%n nodes.size()=%d boundaryNodes.size()=%d", nodes.size(), boundaryNodes.size()));
        }
        log.info(String.format("Start generateQuadratureDomainsByTriangle in autoTriangle(%6.3e, %6.3e, %s", quadratureDomainSize, quadratureDomainsFlatness, nodeSwitch));
        triangleJni = new TriangleJni();
        gm.generateApproximatePoints(quadratureDomainSize, quadratureDomainsFlatness);
        triangleJni.complie(gm, quadratureDomainSwitch);
        LinkedList<double[]> triangleQuadratureDomains = triangleJni.getTriangleXYsList();
        log.info("End of generateQuadratureDomainsByTriangle()");
        return null;
    }
}
