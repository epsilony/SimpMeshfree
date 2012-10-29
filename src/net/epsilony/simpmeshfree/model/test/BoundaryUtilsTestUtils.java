/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import net.epsilony.simpmeshfree.model.LineBoundary;
import net.epsilony.simpmeshfree.model.Node;
import net.epsilony.utils.geom.Coordinate;
import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;

/**
 *
 * @author epsilon
 */
public class BoundaryUtilsTestUtils {

    public static double[] genLine2DPerpendicularTo(Coordinate oriLinePt, double oriLineTheta, double distance, double length, double startDistToFoot) {
        double root_x = Math.cos(oriLineTheta) * distance + oriLinePt.x;
        double root_y = Math.sin(oriLineTheta) * distance + oriLinePt.y;
        double start_x = root_x + startDistToFoot * -Math.sin(oriLineTheta);
        double start_y = root_y + startDistToFoot * Math.cos(oriLineTheta);
        double end_x = start_x - length * -Math.sin(oriLineTheta);
        double end_y = start_y - length * Math.cos(oriLineTheta);
        return new double[]{start_x, start_y, end_x, end_y};
    }
    
    public static Object[] genLineIntersection2DTestSample(){
                Coordinate center=new Coordinate(12.45,-20.8);
        double rad=9;
        double theta=Math.PI*2.1/3;    
        
        double[][] linePts=new double[][]{
            {3.44,-100,3.44,100},
            {100,-29.9,-100,-29.9},
        };
        ArrayList<double[]> lines=new ArrayList<>(Arrays.asList(linePts));
        lines.add(genLine2DPerpendicularTo(center, theta, rad-0.01, rad, rad/2));
        lines.add(genLine2DPerpendicularTo(center, theta, rad+0.01, rad, rad/2));
        lines.add(genLine2DPerpendicularTo(center, theta, rad, rad, -0.01));
        lines.add(genLine2DPerpendicularTo(center, theta, rad, rad, rad+0.01));
                lines.add(genLine2DPerpendicularTo(center, theta, rad-0.01, rad, 0));
        lines.add(genLine2DPerpendicularTo(center, theta, rad-0.01, rad, rad-0.1));
        boolean[] expResults=new boolean[]{false,false,true,false,false,false,true,true};
        return new Object[]{expResults,lines,center,rad};
    }
    
    @SuppressWarnings("unchecked")
    public static Object[] genLineIntersection3DTestSample(){
        Vector3D axis=new Vector3D(new Random().nextDouble(), new Random().nextDouble(), new Random().nextDouble());
        double angle=new Random().nextDouble()*Math.PI*2;
        Rotation rot=new Rotation(axis, angle);
        Object[] samples=genLineIntersection2DTestSample();
        ArrayList<double[]> lines=(ArrayList<double[]>) samples[1];
        ArrayList<LineBoundary> lineBnds=new ArrayList<>(lines.size());
        Coordinate center=(Coordinate) samples[2];
        Vector3D vecCenter=new Vector3D(center.x,center.y,center.z);
        for (double[] line :lines){
            Vector3D start2D=new Vector3D(line[0],line[1],0);
            Vector3D end2D=new Vector3D(line[2],line[3],0);
            Vector3D start=rot.applyTo(start2D.add(-1,vecCenter)).add(vecCenter);
            Vector3D end=rot.applyTo(end2D.add(-1,vecCenter)).add(vecCenter);
            lineBnds.add(new LineBoundary(new Node(start.getX(),start.getY(),start.getZ()), new Node(end.getX(),end.getY(),end.getZ())));
        }
        samples[1]=lineBnds;
        return samples;
    }
}
