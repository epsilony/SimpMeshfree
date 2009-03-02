/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.ModelElementIndexManager;

/**
 *
 * @author epsilon
 */
public class LineSegment extends Segment {
    
    @Override
    public ModelElementType getType() {
        return ModelElementType.LineSegment;
    }

    Point v1,v2;

    public LineSegment(Point v1, Point v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public Point getV1() {
        return v1;
    }

    public Point getV2() {
        return v2;
    }

    public static void main(String [] args){
        LineSegment s=new LineSegment(new Point(0,0),new Point(0,1));
        System.out.println("s.getIndex() = " + s.getIndex());
        s=new LineSegment(new Point(0,0),new Point(0,1));
        System.out.println("s.getIndex() = " + s.getIndex());
        Node n=new Node(0,1);
        n=new Node(0,1);
        System.out.println("n.getIndex() = " + n.getIndex());
    }

}