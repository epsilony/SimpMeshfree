/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.LineBoundary;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.Boundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizer;
import net.epsilony.simpmeshfree.model.NodeSupportDomainSizers;
import net.epsilony.utils.geom.Coordinate;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class BoundaryBasedCriterions2DTest {
    
    public BoundaryBasedCriterions2DTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testVisibleFilter(){        // TODO review the generated test code and remove the default call to fail.
        System.out.println("testVisibleFilter");
        NodeSupportDomainSizer sizer=new NodeSupportDomainSizers.ConstantSizer(10);
        BoundaryBasedCriterions2D.Visible visible=new BoundaryBasedCriterions2D.Visible(sizer);
        double[] bndXys=new double[]{3,0.5,0,0.5,-1,2,0,1,1,1,2,1.5,3,2,2,2,1,3,1,4,2,4};
        
        List<Boundary> bounds=getBoundaries(bndXys);
        Coordinate center=new Coordinate(1, 1);
        for(Boundary bn:bounds){
            if (bn.getPoint(0).equals2D(center)){
                center=bn.getPoint(0);
                break;
            }
        }
        double[] nodesXys=new double []{0,-1,3,3,1,5,1,2};
        List<Node> nodes=getNodes(nodesXys);
        for(Boundary bn:bounds){
            nodes.add(new Node(bn.getPoint(1)));
        }
        nodes.add(new Node(bounds.get(0).getPoint(0)));
        LinkedList<Node> results=new LinkedList<>();
        visible.filterNodes(bounds, center, nodes, results);
        double expXys[]=new double[]{-1,2,0,1,1,1,1,2,1,3,2,1.5,2,2,3,2,};
        
        Collections.sort(results, new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                double t=o1.coordinate.x-o2.coordinate.x;
                if(t<0){
                    return -1;
                }else if(t>0){
                    return 1;
                }else{
                    t=o1.coordinate.y-o2.coordinate.y;
                    if(t<0){
                        return -1;
                    }else if(t>0){
                        return 1;
                    }
                }
                return 0;
            }
        });

        System.out.println("results.size() = " + results.size());
                System.out.println(nodes);
         System.out.println(results);
        assertEquals(expXys.length/2, results.size());
        int i=0;
        for(Node nd:results){
            assertEquals(expXys[i], nd.coordinate.x,1e-10);
            assertEquals(expXys[i+1], nd.coordinate.y,1e-10);
            i+=2;
        }
       
    }
    
    List<Node> getNodes(double[] xys){
        LinkedList<Node> nodes=new LinkedList<>();
        for(int i=0;i<xys.length;i+=2){
            nodes.add(new Node(xys[i], xys[i+1]));
        }
        return nodes;
    }
    
    List<Boundary> getBoundaries(double[] xys){
        
        LinkedList<Coordinate> coordinates=new LinkedList<>();
        for(int i=0;i<xys.length;i+=2){
            coordinates.add(new Coordinate(xys[i], xys[i+1]));
        }
        LinkedList<Boundary> bounds=new LinkedList<>();
        Coordinate rear=coordinates.poll();
        do{
            Coordinate front=coordinates.poll();
            LineBoundary tb=new LineBoundary();
            tb.start=rear;
            tb.end=front;
            bounds.add(tb);
            rear=front;
        }
        while(!coordinates.isEmpty());
        return bounds;
    }
}
