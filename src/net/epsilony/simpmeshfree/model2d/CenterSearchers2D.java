/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import java.util.List;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.utils.CenterSearcher;
import net.epsilony.utils.LayeredRangeTree;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class CenterSearchers2D {
    public static class NodesMaxSupportDomainRadium implements CenterSearcher<Coordinate, Node> {
        LayeredRangeTree<Node> nodesTree;
        double maxRadiu;
        Node fromNode=new Node();
        Node toNode=new Node();
        Coordinate from=fromNode;
        Coordinate to=fromNode;
        
        public NodesMaxSupportDomainRadium(double maxRadiu,LayeredRangeTree<Node> nodesTree){
            this.maxRadiu=maxRadiu;
            this.nodesTree=nodesTree;
        }
        
        @Override
        public List<Node> search(Coordinate center, List<Node> results) {
            from.x=center.x-maxRadiu;
            from.y=center.y-maxRadiu;
            to.x=center.x+maxRadiu;
            to.y=center.y+maxRadiu;
            nodesTree.search(results, fromNode, toNode);
            return results;
        }
    }
}
