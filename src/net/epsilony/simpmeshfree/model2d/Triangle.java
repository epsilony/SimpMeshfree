/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.Node;

/**
 *
 * @author epsilon
 */
public class Triangle {

    //double cx, cy, cr, crsq;//Cirum circle's center position cx,cy the radiu cr and crsq=cr*cr;
    Node [] nodes=new Node[3];

    public Triangle(Node[] nodes) {
        this.nodes[0]=nodes[0];
        this.nodes[1]=nodes[1];
        this.nodes[2]=nodes[2];
        //calculateCirumRadiuAndCenter();
    }

    public Triangle(Node n1, Node n2, Node n3) {
    
        nodes[0]=n1;
        nodes[1]=n2;
        nodes[2]=n3;
//        calculateCirumRadiuAndCenter();
    }
    Triangle[] neighbors = new Triangle[3];

    public Triangle[] getNeighbors() {
        return neighbors;
    }

//    public double getCircumRadiu() {
//        return cr;
//    }
//
//    public double getCircumRadiuSquare(){
//        return crsq;
//    }

//    private void calculateCirumRadiuAndCenter() {
//        double x1 = nodes[0].x;
//        double y1 = nodes[0].y;
//        double x2 = nodes[1].x;
//        double y2 = nodes[1].y;
//        double x3 = nodes[2].x;
//        double y3 = nodes[2].y;
//        double t = -y1 * x3 - y2 * x1 + y2 * x3 + y1 * x2 + y3 * x1 - y3 * x2;
//        cx = 1 / 2 * (y3 * x1 * x1 - y2 * x1 * x1 - y1 * x3 * x3 + y1 * y2 * y2 - y3 * y2 * y2 + y3 * y3 * y2 - y3 * x2 * x2 - y1 * y3 * y3 + x3 * x3 * y2 + y1 * x2 * x2 - y1 * y1 * y2 + y1 * y1 * y3) / t;
//        cy = -1 / 2 * (-y2 * y2 * x3 + y3 * y3 * x2 - y3 * y3 * x1 - x3 * x2 * x2 + x3 * x3 * x2 + y2 * y2 * x1 + y1 * y1 * x3 - y1 * y1 * x2 + x1 * x1 * x3 - x1 * x1 * x2 + x1 * x2 * x2 - x1 * x3 * x3) / t;
//        crsq = 1 / 4 * (-2 * x3 * x2 + y3 *y3 - 2 * y3 * y2 + x3 *x3 + y2 *y2 + x2 *x2) * (y2 *y2 + x1 *x1 + y1 *y1 + x2 *x2 - 2 * y1 * y2 - 2 * x1 * x2) * (x1 *x1 - 2 * x1 * x3 + y1 *y1 + y3 *y3 - 2 * y1 * y3 + x3 *x3) / (t*t);
//        cr=sqrt(crsq);
//    }

//    public boolean isInCirumCircle(Point p){
//        return (p.x-cx)*(p.x-cx)+(p.y-cy)*(p.y-cy)<=crsq;
//    }
//
//    public boolean isInCirumCircle(double x,double y){
//            return (x-cx)*(x-cx)+(y-cy)*(y-cy)<=crsq;
//    }
//    public static void main(String[] args) {
//        Node2D n1=new Node2D(sqrt(3),0);
//        Node2D n2=new Node2D(0,1);
//        Node2D n3=new Node2D(0,-1);
//        Triangle tr=new Triangle(n1, n2, n3);
//    }

   
}
