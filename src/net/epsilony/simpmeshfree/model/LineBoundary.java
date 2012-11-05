/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.utils.geom.Node;
import java.util.Comparator;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class LineBoundary implements Boundary {

    public LineBoundary pred;
    public LineBoundary succ;
    public int id;

    @Override
    public String toString() {
        return "LB{" + "id=" + id + ", start=" + start + ", end=" + end + '}';
    }

    public LineBoundary(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    public LineBoundary() {
    }
    public Node start, end;

    @Override
    public int num() {
        return 2;
    }

//    @Override
//    public Coordinate center(Coordinate result) {
//        if (null == result) {
//            result = new Coordinate();
//        }
//        result.x = (end.x + start.x) / 2;
//        result.y = (end.y + start.y) / 2;
//        result.z = (end.z + start.z) / 2;
//        return result;
//    }
//
//    @Override
//    public Coordinate[] valuePartialByParameter(Coordinate par, Coordinate[] results) {
//        double dx_dt = end.x - start.x;
//        double dy_dt = end.y - start.y;
//        double dz_dt = end.z - start.z;
//        results[0].x = dx_dt;
//        results[0].y = dy_dt;
//        results[0].z = dz_dt;
//        return results;
//    }
//
//    @Override
//    public Coordinate valueByParameter(Coordinate par, Coordinate result) {
//        double t = par.x;
//        result.x = end.x * t + start.x * (1 - t);
//        result.y = end.y * t + start.y * (1 - t);
//        result.z = end.z * t + start.z * (1 - t);
//        return result;
//    }

    public static Comparator<LineBoundary> comparatorByDim(final int dim) {
        switch (dim) {
            case 0:
                return new Comparator<LineBoundary>() {

                    @Override
                    public int compare(LineBoundary o1, LineBoundary o2) {
                        double t1 = o1.end.x + o1.start.x;
                        double t2 = o2.end.x + o2.start.x;
                        if (t1 < t2) {
                            return -1;
                        } else if (t1 == t2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };
            case 1:
                return new Comparator<LineBoundary>() {

                    @Override
                    public int compare(LineBoundary o1, LineBoundary o2) {
                        double t1 = o1.end.y + o1.start.y;
                        double t2 = o2.end.y + o2.start.y;
                        if (t1 < t2) {
                            return -1;
                        } else if (t1 == t2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };
            case 2:
                return new Comparator<LineBoundary>() {

                    @Override
                    public int compare(LineBoundary o1, LineBoundary o2) {
                        double t1 = o1.end.z + o1.start.z;
                        double t2 = o2.end.z + o2.start.z;
                        if (t1 < t2) {
                            return -1;
                        } else if (t1 == t2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                };
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Node getNode(int index) {
        switch (index) {
            case 0:
                return start;
            case 1:
                return end;
            default:
                throw new IndexOutOfBoundsException(String.format("The index must betwean [0,2), but requires %d", index));
        }
    }

    @Override
    public Boundary getNeighbor(int index) {
        switch (index) {
            case 0:
                return pred;
            case 1:
                return succ;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

//    /**
//     * Only prepared for 2D situation! (Ignore the z dim or coordinates)
//     *
//     * @param result
//     * @return
//     */
//
//    public Coordinate normal(Coordinate result) {
//        if (null == result) {
//            result = new Coordinate();
//        }
//        result.z = 0;
//        double dx, dy, norm;
//        dx = end.x - start.x;
//        dy = end.y - start.y;
//        norm = Math.sqrt(dx * dx + dy * dy);
//        result.x = -dy / norm;
//        result.y = dx / norm;
//        return result;
//    }

   
//    public Coordinate projection(Coordinate toProject, Coordinate result, double[] distance) {
//        if(null==result){
//            result=new Coordinate();
//        }
//        double disSq1, disSq2;
//        disSq1 = GeometryMath.distanceSquare(start, toProject);
//        disSq2 = GeometryMath.distanceSquare(end, toProject);
//        Coordinate nearPt, farPt;
//        if (disSq1 < disSq2) {
//            nearPt = start;
//            farPt = end;
//        } else {
//            nearPt = end;
//            farPt = end;
//        }
//        Coordinate vec1=minus(toProject,nearPt,null);
//        Coordinate vec2=minus(farPt,nearPt,null);
//        Coordinate normal=cross(cross(vec1,vec2,result),vec2);
//        double norm=dot(normal,vec1)*-1;
//
//        result.scale(norm);
//        GeometryMath.add(result, toProject, result);
//        if(null!=distance){
//            distance[0]=norm;
//        }
//        return result;
//    }
//
//   
//    public boolean isInDistance(Coordinate from, double radius) {
//        double radiusSq=radius*radius;
//        double startDisSq=distanceSquare(from, start);
//        if(startDisSq<=radiusSq){
//            return true;
//        }
//        double endDisSq=distanceSquare(from, end);
//        if(endDisSq<=radiusSq){
//            return true;
//        }
//        double[] distance=new double[1];
//        Coordinate foot=projection(from,null,distance);
//        double dist=distance[0];
//        if(dist>radius){
//            return false;
//        }
//        
//        double dotv=dot(minus(foot,start),minus(foot,end));
//        if(dotv<=0){
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    @Override
//    public double sizeSquare() {
//        double dx=end.x-start.x;
//        double dy=end.y-start.y;
//        double dz=end.z-start.z;
//        return dx*dx+dy*dy+dz*dz;
//    }

    @Override
    public double circum(Coordinate outputCenter) {
        if(outputCenter!=null){
            outputCenter.x=(start.x+end.x)/2;
            outputCenter.y=(start.y+end.y)/2;
            outputCenter.z=(start.z+end.z)/2;
        }
        
        double dx,dy,dz;
        dx=end.x-start.x;
        dy=end.y-start.y;
        dz=end.z-start.z;
        double r=dx*dx+dy*dy+dz*dz;
        r=Math.sqrt(r)/2;
        return r;
                
    }

    @Override
    public boolean isIntersect(Coordinate center, double radius) {
        return BoundaryUtils.isLineBoundarySphereIntersect(this, center, radius);
    }

//    @Override
//    public int getDim() {
//        return 2;
//    }

    @Override
    public Coordinate outNormal(Coordinate result) {
       return BoundaryUtils.outNormal(this, null, result);
    }
}
