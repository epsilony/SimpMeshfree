/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;
import static java.lang.Math.*;

/**
 *
 * @author epsilon
 */
public class Triangle extends ModelElement {

    double cx, cy, cr, crsq;//Cirum circle's center position cx,cy the radiu cr and crsq=cr*cr;
    static ModelElementIndexManager triangleIM = new ModelElementIndexManager();

    @Override
    public ModelElementType type() {
        return ModelElementType.Triangle;
    }
    Node [] nodes=new Node[3];

    public Triangle(Node[] nodes) {
        this.index = triangleIM.getNewIndex();
        this.nodes[0]=nodes[0];
        this.nodes[1]=nodes[1];
        this.nodes[2]=nodes[2];
        calculateCirumRadiuAndCenter();
    }

    public Triangle(Node n1, Node n2, Node n3) {
        this.index = triangleIM.getNewIndex();
        nodes[0]=n1;
        nodes[1]=n2;
        nodes[2]=n3;
        calculateCirumRadiuAndCenter();
    }
    Triangle[] neighbors = new Triangle[3];

    public Triangle[] getNeighbors() {
        return neighbors;
    }

    @Override
    public ModelElementIndexManager getIndexManager() {
        return triangleIM;
    }

    public double getCircumRadiu() {
        return cr;
    }

    public double getCircumRadiuSquare(){
        return crsq;
    }

    private void calculateCirumRadiuAndCenter() {
        double x1 = nodes[0].x;
        double y1 = nodes[0].y;
        double x2 = nodes[1].x;
        double y2 = nodes[1].y;
        double x3 = nodes[2].x;
        double y3 = nodes[2].y;
        double t = -y1 * x3 - y2 * x1 + y2 * x3 + y1 * x2 + y3 * x1 - y3 * x2;
        cx = 1 / 2 * (y3 * x1 * x1 - y2 * x1 * x1 - y1 * x3 * x3 + y1 * y2 * y2 - y3 * y2 * y2 + y3 * y3 * y2 - y3 * x2 * x2 - y1 * y3 * y3 + x3 * x3 * y2 + y1 * x2 * x2 - y1 * y1 * y2 + y1 * y1 * y3) / t;
        cy = -1 / 2 * (-y2 * y2 * x3 + y3 * y3 * x2 - y3 * y3 * x1 - x3 * x2 * x2 + x3 * x3 * x2 + y2 * y2 * x1 + y1 * y1 * x3 - y1 * y1 * x2 + x1 * x1 * x3 - x1 * x1 * x2 + x1 * x2 * x2 - x1 * x3 * x3) / t;
        crsq = 1 / 4 * (-2 * x3 * x2 + y3 *y3 - 2 * y3 * y2 + x3 *x3 + y2 *y2 + x2 *x2) * (y2 *y2 + x1 *x1 + y1 *y1 + x2 *x2 - 2 * y1 * y2 - 2 * x1 * x2) * (x1 *x1 - 2 * x1 * x3 + y1 *y1 + y3 *y3 - 2 * y1 * y3 + x3 *x3) / (t*t);
        cr=sqrt(crsq);
    }

    public boolean isInCirumCircle(Point p){
        return (p.x-cx)*(p.x-cx)+(p.y-cy)*(p.y-cy)<=crsq;
    }

    public boolean isInCirumCircle(double x,double y){
            return (x-cx)*(x-cx)+(y-cy)*(y-cy)<=crsq;
    }
    public static void main(String[] args) {
        Node n1=new Node(sqrt(3),0);
        Node n2=new Node(0,1);
        Node n3=new Node(0,-1);
        Triangle tr=new Triangle(n1, n2, n3);
    }
}
