/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.sfun;

import java.util.ArrayList;
import java.util.Arrays;
import net.epsilony.simpmeshfree.model.InfluenceDomainSizers;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.simpmeshfree.sfun.test.SFunTestUtils;
import net.epsilony.simpmeshfree.sfun.wcores.Wendland;
import net.epsilony.simpmeshfree.utils.CoordinatePartDiffFunction;
import net.epsilony.utils.geom.Coordinate;
import no.uib.cipr.matrix.DenseVector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author epsilon
 */
public class RBFTest {

    public RBFTest() {
    }

//    @Test
//    public void unitTest() {
//        DenseVector u = new DenseVector(new double[]{0, 1, 1, 0});
//        WeightFunctionCore radialBaseCore = new Wendland(Wendland.C4);
//        double[] posts = new double[]{0, 0, 1, 0, 1, 1, 0, 1};
//        double rad = 2;
//
//        ArrayList<Node> nds = new ArrayList<>();
//        for (int i = 0; i < posts.length / 2; i++) {
//            Node nd = new Node(posts[i * 2], posts[i * 2 + 1]);
//            nds.add(nd);
//        }
//
//        RBF rbf = new RBF(2, nds, u, InfluenceDomainSizers.constantSizer(rad), radialBaseCore);
//        Coordinate testPt = new Coordinate(0.5, 0.5);
//        rbf.setDiffOrder(1);
//        double[] vs = rbf.values(testPt, null);
//        System.out.println("vs = " + Arrays.toString(vs));
//    }

    @Test
    public void fitnessTest() {
        double x0 = 0, y0 = 0, w = 100, h = 50, wCir = 3, hCir = 1.7, wPhase = 0, hPhase = Math.PI / 2;
        double node_freq = 10;
        double step = Math.min(w / wCir, h / hCir) / node_freq;
        double infRad = step * 2.5;

        double sampleStep = 10;

        WeightFunctionCore radialBaseCore = new Wendland(Wendland.C4);
        ArrayList<Node> nodes = SFunTestUtils.genNodes(x0, y0, w, h, step);

        CoordinatePartDiffFunction sampleFun = SFunTestUtils.sinSin(x0, y0, w, h, wCir, hCir, wPhase, hPhase);
        DenseVector u = new DenseVector(nodes.size());
        sampleFun.setDiffOrder(0);
        for (int i = 0; i < nodes.size(); i++) {
            double[] svs = sampleFun.values(nodes.get(i), null);
            u.set(i, svs[0]);
        }

        RBF rbf = new RBF(2, nodes, u, InfluenceDomainSizers.constantSizer(infRad), radialBaseCore);
        rbf.setDiffOrder(1);
        sampleFun.setDiffOrder(1);
        int sampleSzX = (int) Math.ceil(w / sampleStep) + 1;       
        int sampleSzY = (int) Math.ceil(h / sampleStep) + 1;
        double stepX = w / (sampleSzX - 1);
        double stepY = h / (sampleSzY - 1);
        double[] errSqrs=new double[3];
        for (int i = 0; i < sampleSzX; i++) {
            double x, y = 0;
            if (i == sampleSzX - 1) {
                x = x0 + w;
            } else {
                x = x0 + stepX * i;
            }
            for (int j = 0; j < sampleSzY; j++) {

                if (j == sampleSzY - 1) {
                    y = y0 + h;
                } else {
                    y = y0 + stepY * j;
                }
                double[] rbfVs = rbf.values(new Coordinate(x, y), null);
                double[] exps = sampleFun.values(new Coordinate(x, y), null);
                //System.out.println("x,y="+x+" "+y+" ;rbfVs[0] = " + rbfVs[0] + " exp=" + exps[0] + " dx=" + rbfVs[1] + " exp_dx=" + exps[1] + " dy=" + rbfVs[2] + " exp_dy" + exps[2]);
                for(int k=0;k<errSqrs.length;k++){
                    errSqrs[k]+=Math.pow((rbfVs[k]-exps[k]),2);
                }
            }


        }
        
            for(int k=0;k<errSqrs.length;k++){
                errSqrs[k]=Math.sqrt(errSqrs[k])/(sampleSzX*sampleSzY);
            }
            System.out.println("(err norm)/sample times = " + Arrays.toString(errSqrs));
            assertArrayEquals(errSqrs, new double[]{0,0,0}, 0.01);
    }
}
