/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model2d;

import net.epsilony.simpmeshfree.model.Boundary;

/**
 *
 * @author epsilon
 */
public class LineBoundary2D implements Boundary<Coordinate2D>{
    Coordinate2D front,rear;
    @Override
    public Coordinate2D getBoudaryElement(int index) {
        switch(index){
            case 0:
                return rear;
            case 1:
                return front;             
            default:
                throw new IndexOutOfBoundsException(String.format("The index must betwean [0,2), but requires %d", index));
        }
    }

    @Override
    public int getBoudaryElementsSize() {
        return 2;
    }

    @Override
    public Coordinate2D getCenterPoint(int index,Coordinate2D result) {
        result.x=(front.x+rear.x)/2;
        result.y=(front.y+rear.y)/2;
        return result;
    }
    
}
