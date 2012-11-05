/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d;

import java.util.Arrays;
import net.epsilony.utils.geom.Node;
import net.epsilony.utils.WithId;
import net.epsilony.utils.geom.GeometryMath;

/**
 * The atomic data structure of "structural adaptivity"
 *
 * @author epsilonyuan@gmail.com
 */
public class QuadPixcell implements WithId {

    public int id, level;
    public double errEst;  //errorEstimation  
    public int branchDepth;
    public QuadPixcell[] neighbours = new QuadPixcell[4];
    public Node[] nodes = new Node[4];
    
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
    private final static int[][] REFINDED_NDS_DISPATCH = new int[][]{
        {0, 1, 8, 7},
        {1, 2, 3, 8},
        {8, 3, 4, 5},
        {7, 8, 5, 6}};

    public QuadPixcell[] refine(){
        return refine(false);
    }
    
    public QuadPixcell[] refine(boolean check) {
        if (check) {
            //check the level distance betwean this and neibours
            for (int i = 0; i < neighbours.length; i++) {
                QuadPixcell nb = neighbours[i];
                if (nb == null) {
                    continue;
                }
                if (nb.level < level || nb.level > level + 1) {
                    throw new RuntimeException("Neigbour level distance is too large!");
                }
            }
        }

        Node[] edgeMidNodes = edgeMidNodes4Refine();
        Node centerNd = (Node) GeometryMath.crossPointOfLineSegments(edgeMidNodes[0], edgeMidNodes[2], edgeMidNodes[1], edgeMidNodes[3], new Node());

        Node[] refindedNds = new Node[9];
        for (int i = 0; i < 4; i++) {
            refindedNds[i * 2] = nodes[i];
            refindedNds[i * 2 + 1] = edgeMidNodes[i];
        }
        refindedNds[8] = centerNd;

        QuadPixcell[] results = new QuadPixcell[4];
        results[0] = this;

        for (int i = 1; i < results.length; i++) {
            results[i] = new QuadPixcell();
        }

        QuadPixcell[] oriNeibours = this.neighbours;
        this.neighbours = new QuadPixcell[4];
        int oriLevel = level;

        for (int i = 0; i < 4; i++) {
            QuadPixcell px = results[i];

            // fill refined pixcell's nodes
            for (int j = 0; j < px.nodes.length; j++) {
                px.nodes[j] = refindedNds[REFINDED_NDS_DISPATCH[i][j]];
            }

            // fill refined pixcells' neighbours
            px.neighbours[i] = oriNeibours[i];
            px.neighbours[(i + 1) % 4] = results[(i + 1) % 4];
            px.neighbours[(i + 2) % 4] = results[(i + 3) % 4];
            QuadPixcell px_nb_3 = oriNeibours[(i + 3) % 4];
            if (px_nb_3 != null && px_nb_3.level > oriLevel) {
                px_nb_3 = px_nb_3.neighbours[i];
            }
            px.neighbours[(i + 3) % 4] = px_nb_3;

            // fill the ori neighbours with refined pixcells
            QuadPixcell oriNb = oriNeibours[i];
            if (oriNb != null) {
                if (oriNb.level > oriLevel) {
                    oriNb.neighbours[(i + 2) % 4] = px;
                    QuadPixcell oriNbLeft = oriNb.neighbours[(i + 1) % 4];
                    oriNbLeft.neighbours[(i + 2) % 4] = results[(i + 1) % 4];
                } else {
                    oriNb.neighbours[(i + 2) % 4] = results[(i + 1) % 4];
                }
            }
            px.level = oriLevel + 1;
        }

        branchDepth++;

        return results;
    }

    Node[] edgeMidNodes4Refine() {
        Node[] result = new Node[4];
        for (int i = 0; i < 4; i++) {
            if (neighbours[i] == null || neighbours[i].level <= level) {
                Node st = nodes[i];
                Node ed = nodes[(i + 1) % 4];
                Node nd = new Node((st.x + ed.x) * 0.5, (st.y + ed.y) * 0.5);
                result[i] = nd;
            } else {
                result[i] = neighbours[i].nodes[(i + 2) % 4];
            }
        }
        return result;
    }
    
    public QuadPixcell[] merge(){
        return merge(false);
    }
    
    public QuadPixcell[] merge(boolean check){
        if (branchDepth<=0){
            throw new RuntimeException("Not a big brother, can't fire a merge operation on this pixcell!");
        }
        
        QuadPixcell[][] slNb=slibingsNeighbours4Merge();
        QuadPixcell[] slibings=slNb[0];
        QuadPixcell[] oriNbs=slNb[1];
        if(check){
            for(int i=0;i<slibings.length;i++){
                if(slibings[i].level!=level||oriNbs[i].level>level||oriNbs[i].level<level-1){
                    throw new UnsupportedOperationException("Neighbours or slibings level check failed.");
                }
            }
        }
        
        //fill nodes
        for(int i=0;i<4;i++){
            nodes[i]=slibings[i].nodes[i];
        }
        
        //fill neighbourhoods
        neighbours=oriNbs;
        
        //change neighbour's link to this
        for(int i=0;i<4;i++){
            QuadPixcell nb=oriNbs[i];
            if(nb==null){
                continue;
            }
            nb.neighbours[(i+2)%4]=this;
            if(nb.level==level){
                QuadPixcell leftOfNb=nb.neighbours[(i+1)%4];
                leftOfNb.neighbours[(i+2)%4]=this;
            }
        }
        
        level--;
        branchDepth--;
        
        return slibings;
    }
    
    public QuadPixcell[] slibings(){
        QuadPixcell[] slibings=new QuadPixcell[4];
        slibings[0]=this;
        slibings[1]=this.neighbours[1];
        slibings[2]=this.neighbours[1].neighbours[2];
        slibings[3]=this.neighbours[2];
        return slibings;
    }
    
    public QuadPixcell[][] slibingsNeighbours4Merge(){
        QuadPixcell[] slibings=slibings();
        QuadPixcell[][] result=new QuadPixcell[2][4];
        QuadPixcell[] oriNeighbours=new QuadPixcell[4];
        for(int i=0;i<slibings.length;i++){
            oriNeighbours[i]=slibings[i].neighbours[i];
        }
        
        result[0]=slibings;
        result[1]=oriNeighbours;
        return result;
    }
}
