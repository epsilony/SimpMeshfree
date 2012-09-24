/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d.sample;

import java.util.ArrayList;
import java.util.Arrays;
import net.epsilony.simpmeshfree.adpt2d.QuadPixcell;
import net.epsilony.simpmeshfree.model.Node;

/**
 *
 * @author epsilon
 */
public class QuadPixcellSample {
    /**
     * Generate a QuadPixcell which has a No.3 node at (ox,oy) 
     * and No.2 node is at (ox+scale,oy*scale)
     * @param ox
     * @param oy
     * @param scale
     * @return 
     */
    static QuadPixcell sampleRefineOri(double ox,double oy,double scale){
        Node[] nds=new Node[4];
        for(int i=0,s=0;i<2;i++){
            for (int j=0;j<2;j++,s++){
                nds[s]=new Node(ox+scale*j,oy+scale*i);
            }
        }
        int[] ids=new int[]{1,3,2,0};
        QuadPixcell result=new QuadPixcell();
        result.level=0;
        result.neighbours=new QuadPixcell[4];
        result.nodes=new Node[4];
        for(int i=0;i<4;i++){
            result.nodes[i]=nds[ids[i]];
        }
        return result;
    }
    
    /**
     * Generate neighour(s, which depends by @code{small}) at Edge.@code{direct} for ori and fill the neighbourhoods
     * for ori and newly generated pixcells
     * @param ori
     * @param scale  the same @code{scale} value of building ori
     * @param direct the Edge direction
     * @param small if true, get 4 pixcells with a smaller level. if false, return 1 pixcell with the same level of ori
     * @return Newly generated sample neigbour(s)
     */
    static QuadPixcell[] sampleRefineOriNeibour(QuadPixcell ori,double scale,int direct,boolean small){
        double[] dirOri=new double[]{ori.nodes[0].x,ori.nodes[0].y,ori.nodes[2].x,ori.nodes[2].y,ori.nodes[3].x-scale,ori.nodes[3].y,ori.nodes[3].x,ori.nodes[3].y-scale};
        double ox=dirOri[direct*2];
        double oy=dirOri[direct*2+1];
        double gridSize=scale;
        int ndsSize=2;
        if(small){
            gridSize=scale*0.5;
            ndsSize=3;
        }
        Node[] nds=new Node[ndsSize*ndsSize];
        for(int i=0,s=0;i<ndsSize;i++){
            for(int j=0;j<ndsSize;j++,s++){
                nds[s]=new Node(ox+gridSize*j,oy+gridSize*i);
            }
        }
        QuadPixcell[] results;
        if(small){
            results=new QuadPixcell[4];
            int[][] ndsIds=new int[][]{{2,5,4,1},{5,8,7,4},{4,7,6,3},{1,4,3,0}};
            for(int i=0;i<results.length;i++){
                QuadPixcell px=new QuadPixcell();
                results[i]=px;
                px.nodes=new Node[4];
                for(int j=0;j<px.nodes.length;j++){
                    px.nodes[j]=nds[ndsIds[i][j]];
                }
            }
            for(int i=0;i<results.length;i++){
                QuadPixcell px=results[i];
                px.neighbours=new QuadPixcell[4];
                px.neighbours[(i+1)%4]=results[(i+1)%4];
                px.neighbours[(i+2)%4]=results[(i+3)%4];
                px.level=ori.level+1;
            }
            ori.neighbours[direct]=results[(direct+3)%4];
            results[(direct+3)%4].neighbours[(direct+2)%4]=ori;
            results[(direct+2)%4].neighbours[(direct+2)%4]=ori;
        }else{
            results=new QuadPixcell[1];
            int[] ids=new int[]{1,3,2,0};
            QuadPixcell px=new QuadPixcell();
            results[0]=px;
            px.nodes=new Node[4];
            for(int i=0;i<px.nodes.length;i++){
                px.nodes[i]=nds[ids[i]];
            }
            ori.neighbours[direct]=px;
            px.neighbours=new QuadPixcell[4];
            px.neighbours[(direct+2)%4]=ori;
            px.level=ori.level;
        }
        return results;
    }
    
    /**
     * the No.3 node of results.get(0) is (ox,oy) the No.2 node is (ox+scale,oy+scale)
     * smallbit determines the level of neighbour of results.get(0). Let ori=reuslts.get(0) that
     * smallbit&0x0001!=0 iff results.neibours[0].level lower(0 level is the highest) that ori.level. Smallbit&0x0002/0x0004/0x0008 represent the small level charater of ori.neibours(1/2/3)
     * @param ox
     * @param oy
     * @param scale
     * @param smallbit
     * @return 
     */
    public static ArrayList<QuadPixcell> refineSamples(double ox,double oy,double scale,int smallbit){
        ArrayList<QuadPixcell> results=new ArrayList<>(13);
        QuadPixcell ori=sampleRefineOri(ox, oy, scale);
        results.add(ori);
        int checkBit=0x0001;
        for(int dir=0;dir<4;dir++){
            boolean small=(checkBit&smallbit)!=0;
            checkBit<<=1;
            QuadPixcell[] pxes=sampleRefineOriNeibour(ori, scale, dir, small);
            results.addAll(Arrays.asList(pxes));
        }
        return results;
    }
}
