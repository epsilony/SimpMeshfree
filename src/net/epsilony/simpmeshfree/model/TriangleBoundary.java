/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Coordinate;


/**
 *
 * @author epsilon
 */
public class TriangleBoundary implements Boundary {

    Node[] nodes = new Node[3];
    TriangleBoundary[] neighbors = new TriangleBoundary[3];
    private int id;
    
    @Override
    public Node getNode(int index) {
        return nodes[index];
    }

    @Override
    public Boundary getNeighbor(int index) {
        return neighbors[index];
    }

    @Override
    public int num() {
        return 3;
    }

//    @Override
//    public Coordinate center(Coordinate result) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    @Override
//    public Coordinate valueByParameter(Coordinate par, Coordinate result) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    public Coordinate normal(Coordinate result) {
//        if(null==result){
//            result=new Coordinate();
//        }
//        double[] distSqs=new double[]{distanceSquare(nodes[0], nodes[1]),distanceSquare(nodes[1], nodes[2]),distanceSquare(nodes[2], nodes[0])};
//        int maxId=ArrayUtils.maxItemId(distSqs);
//        int startId=(maxId-1)%3;
//        int endId1=(maxId+1)%3;
//        int endId2=maxId;
//        double norm=Math.sqrt(distSqs[startId]*distSqs[endId1]);
//        Coordinate start,end1,end2;
//        start=nodes[startId];
//        end1=nodes[endId1];
//        end2=nodes[endId2];
//        GeometryMath.cross(end1.x-start.x,end1.y-start.y,end1.z-start.z,end2.x-start.x,end2.y-start.y,end2.z-start.z,result);
//        result.scale(1/norm);
//        return result;
//    }
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

//    public Coordinate projection(Coordinate toProject, Coordinate result, double[] distance) {
//        Coordinate nv=normal(null);
//        int minDisId=0;
//        double minDisSq=distanceSquare(nodes[0], toProject);
//        for(int i=1;i<3;i++){
//            double disSq=distanceSquare(nodes[i], toProject);
//            if(disSq<minDisSq){
//                minDisId=i;
//                minDisSq=disSq;
//            }
//        }
//        Coordinate vec=minus(nodes[minDisId],toProject);
//        double norm=dot(vec,nv);
//        result=linearCombine(toProject, 1, nv, norm,result);
//        if(distance!=null){
//            distance[0]=norm;
//        }
//        return result;
//    }
//
//    @Override
//    public boolean isInDistance(Coordinate from, double radius) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    
//    @Override
//    public double sizeSquare() {
//        double max=0;
//        for (int i=0;i<3;i++){
//            double disSq=distanceSquare(nodes[i], nodes[(i+1)%3]);
//            if(disSq>max){
//                max=disSq;
//            }
//        }
//        return max;
//    }
    @Override
    public double circum(Coordinate outputCenter) {
        double[] distSqs = new double[3];
        Coordinate start, end;
        for (int i = 0; i < 3; i++) {
            start = nodes[(i + 1) % 3];
            end = nodes[(i - 1) % 3];
            double dx, dy, dz;
            dx = end.x - start.x;
            dy = end.y - start.y;
            dz = end.z - start.z;
            distSqs[i] = dx * dx + dy * dy + dz * dz;
        }
        int obtuse = -1;
        for (int i = 0; i < 3; i++) {
            if (distSqs[i] >= distSqs[(i + 1) % 3] + distSqs[(i - 1) % 3]) {
                obtuse = i;
                break;
            }
        }
        if (obtuse > -1) {
            if (null != outputCenter) {
                start = nodes[(obtuse + 1) % 3];
                end = nodes[(obtuse - 1) % 3];
                outputCenter.x = (start.x + end.x) / 2;
                outputCenter.y = (start.y + end.y) / 2;
                outputCenter.z = (start.z + end.z) / 2;
            }
            return Math.sqrt(distSqs[obtuse]) / 2;
        } else {
            if (null != outputCenter) {
                outputCenter.x=0;
                outputCenter.y=0;
                outputCenter.z=0;
                for (int ni = 0; ni < 3; ni++) {
                    double barycentric = distSqs[ni] * (distSqs[(ni + 1) % 3] + distSqs[(ni - 1) % 3] - distSqs[ni]);
                    outputCenter.x+=nodes[ni].x*barycentric;
                    outputCenter.y+=nodes[ni].y*barycentric;
                    outputCenter.z+=nodes[ni].z*barycentric;
                }

            }
            double s = 0;
            double t1 = 1;
            double[] dist = distSqs;
            for (int i = 0; i < 3; i++) {
                double t = Math.sqrt(distSqs[i]);
                dist[i] = t;
                s += t;
                t1 *= t;
            }
            double t2 = 1;
            for (int i = 0; i < 3; i++) {
                t2 *= s - dist[i] * 2;
            }
            double r = t1 / (s * t2);
            return r;
        }
    }

    @Override
    public boolean isIntersect(Coordinate center, double radius) {
        return BoundaryUtils.isTriBoundarySphereIntersect(this, center, radius);
    }

//    @Override
//    public int getDim() {
//        return 3;
//    }

    @Override
    public Coordinate outNormal(Coordinate result) {
        return BoundaryUtils.outNormal(this, result);
    }
}
