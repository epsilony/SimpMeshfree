/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.adpt2d.sample;

import java.util.ArrayList;
import java.util.List;
import net.epsilony.simpmeshfree.adpt2d.QuadPixcell;
import net.epsilony.simpmeshfree.model.Node;

/**
 *
 * @author epsilon
 */
public class QuadPixcellRectangleSample {

    
    public static List<QuadPixcell> genPixcells(double x0, double y0, double w, double h, double scale){
        int nWidth=(int) Math.ceil(w/scale)+1;
        int nHeight=(int) Math.ceil(h/scale)+1;
        double sWidth=w/(nWidth-1);
        double sHeight=h/(nHeight-1);
        
        Node[][] nodes=new Node[nHeight][nWidth];
        for(int i=0;i<nHeight;i++){
            double y;
            if(i==0){
                y=y0;
            }else if(i==nHeight-1){
                y=y0+h;
            }else{
                y=y0+sHeight*i;
            }
            for (int j=0;j<nWidth;j++){
                double x;
                if(j==0){
                    x=x0;
                }else if(j==nWidth-1){
                    x=x0+w;
                }else{
                    x=x0+sWidth*j;
                }
                nodes[i][j]=new Node(x,y);
            }
        }
        
        int[] nodesIs=new int[]{0,1,1,0};
        int[] nodesJs=new int[]{1,1,0,0};
        
        QuadPixcell[][] pxArr=new QuadPixcell[nHeight-1][nWidth-1];
        
        ArrayList<QuadPixcell> results=new ArrayList<>((nWidth-1)*(nHeight-1));
        
        for(int i=0;i<nHeight-1;i++){
            for(int j=0;j<nWidth-1;j++){
                QuadPixcell px=new QuadPixcell();
                pxArr[i][j]=px;
                for(int k=0;k<px.nodes.length;k++){
                    px.nodes[k]=nodes[i+nodesIs[k]][j+nodesJs[k]];
                }
                
                results.add(px);
            }
        }
        
        int[] neighbIs=new int[]{0,1,0,-1};
        int[] neighbJs=new int[]{1,0,-1,0};
        for(int i=0;i<pxArr.length;i++){
            for(int j=0;j<pxArr[i].length;j++){
                QuadPixcell px=pxArr[i][j];
                for(int k=0;k<px.neighbours.length;k++){
                    int iId=i+neighbIs[k];
                    int jId=j+neighbJs[k];
                    if(iId<0||iId>=pxArr.length||jId<0||jId>=pxArr[i].length){
                        continue;
                    }
                    px.neighbours[k]=pxArr[iId][jId];
                }
            }
        }
 
        return results;
    }
    
    
}
