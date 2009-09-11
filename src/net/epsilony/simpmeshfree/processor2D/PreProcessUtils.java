/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.processor2D;

import java.util.LinkedList;
import net.epsilony.simpmeshfree.model2D.ApproximatePoint;
import net.epsilony.simpmeshfree.model2D.BoundaryNode;
import net.epsilony.simpmeshfree.model2D.Model;
import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model2D.Node;
import net.epsilony.simpmeshfree.model2D.TriangleJni;
import org.apache.log4j.Logger;

/**
 *
 * @author epsilon
 */
public class PreProcessUtils {

    static Logger log=Logger.getLogger(PreProcessUtils.class);
    public static void generateBoundaryNodeByApproximatePoints(Model gm, double size, double flatness, LinkedList<Node> nodes, LinkedList<BoundaryNode> boundaryNodes) {
        gm.generateApproximatePoints(size, flatness);
        nodes.clear();
        boundaryNodes.clear();
        for (ApproximatePoint ap : gm.getApproximatePoints()) {
            BoundaryNode bn = new BoundaryNode(ap);
            nodes.add(bn);
            boundaryNodes.add(bn);
        }
    }

    public static TriangleJni  generateQuadratureDomainsByTriangle(Model gm,double size, double flatness, String s,LinkedList<double[]> triangleQuadratureDomains) {
        log.info(String.format("Start generateQuadratureDomainsByTriangle(%6.3e, %6.3e, %s", size, flatness, s));
        TriangleJni triangleJni = new TriangleJni();
        gm.generateApproximatePoints(size, flatness);
        triangleJni.complie(gm, s);
        log.info("End of generateQuadratureDomainsByTriangle()");
        triangleQuadratureDomains.clear();
        triangleQuadratureDomains.addAll(triangleJni.getTriangleXYsList());
        return triangleJni;
    }

    public static TriangleJni generateNodesByTriangle(Model gm,double size, double flatness, String s,LinkedList<Node> nodes,LinkedList<BoundaryNode> boundaryNodes) {
        log.info(String.format("Start generateNodesByTiangle%nsize=%6.3e flatness=%6.3e s=%s ", size, flatness, s));
        nodes.clear();
        boundaryNodes.clear();
        TriangleJni triangleJni = new TriangleJni();
        gm.generateApproximatePoints(size, flatness);
        triangleJni.complie(gm, s);
        nodes.addAll(triangleJni.getNodes(true));
        for (Node n : nodes) {
            if (n.type() == ModelElementType.BoundaryNode) {
                boundaryNodes.add((BoundaryNode) n);
            }

        }
        log.info(String.format("End of generateNodesByTriangle%n nodes.size()=%d boundaryNodes.size()=%d", nodes.size(), boundaryNodes.size()));
        return triangleJni;
    }
}
