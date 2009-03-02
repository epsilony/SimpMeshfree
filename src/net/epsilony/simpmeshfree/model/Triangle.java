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

    double cx, cy, cr, crsq;
    static ModelElementIndexManager triangleIM = new ModelElementIndexManager();

    @Override
    public ModelElementType getType() {
        return ModelElementType.Triangle;
    }
    Node n1, n2, n3;

    public Triangle(Node[] nodes) {
        this.index = triangleIM.getNewIndex();
        n1 = nodes[0];
        n2 = nodes[1];
        n3 = nodes[3];
    }

    public Triangle(Node n1, Node n2, Node n3) {
        this.index = triangleIM.getNewIndex();
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
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
        double x1 = n1.x;
        double y1 = n1.y;
        double x2 = n2.x;
        double y2 = n2.y;
        double x3 = n3.x;
        double y3 = n3.y;
        double t = -y1 * x3 - y2 * x1 + y2 * x3 + y1 * x2 + y3 * x1 - y3 * x2;
        cx = 1 / 2 * (y3 * x1 * x1 - y2 * x1 * x1 - y1 * x3 * x3 + y1 * y2 * y2 - y3 * y2 * y2 + y3 * y3 * y2 - y3 * x2 * x2 - y1 * y3 * y3 + x3 * x3 * y2 + y1 * x2 * x2 - y1 * y1 * y2 + y1 * y1 * y3) / t;
        cy = -1 / 2 * (-y2 * y2 * x3 + y3 * y3 * x2 - y3 * y3 * x1 - x3 * x2 * x2 + x3 * x3 * x2 + y2 * y2 * x1 + y1 * y1 * x3 - y1 * y1 * x2 + x1 * x1 * x3 - x1 * x1 * x2 + x1 * x2 * x2 - x1 * x3 * x3) / t;
        crsq = 1 / 4 * (-2 * x3 * x2 + y3 *y3 - 2 * y3 * y2 + x3 *x3 + y2 *y2 + x2 *x2) * (y2 *y2 + x1 *x1 + y1 *y1 + x2 *x2 - 2 * y1 * y2 - 2 * x1 * x2) * (x1 *x1 - 2 * x1 * x3 + y1 *y1 + y3 *y3 - 2 * y1 * y3 + x3 *x3) / (t*t);
        cr=sqrt(crsq);
    }

    public boolean isInCirumCircle(Point p){
        return (p.x-cx)*(p.x-cx)+(p.y-cy)*(p.y-cy)-crsq<=0;
    }

    public boolean isInCirumCircle(double x,double y){
            return (x-cx)*(x-cx)+(y-cy)*(y-cy)<=0;
    }
    public static void main(String[] args) {
        Node n1=new Node(sqrt(3),0);
        Node n2=new Node(0,1);
        Node n3=new Node(0,-1);
        Triangle tr=new Triangle(n1, n2, n3);
    }
}
