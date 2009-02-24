/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;



/**
 *
 * @author epsilon
 */
public class Vertex extends Point {

    static int maxIndex;
    int index = maxIndex++;
    Vertex previous, next;

    public int getIndex() {
        return index;
    }

    public Vertex getNext() {
        return next;
    }

    public void setNext(Vertex next) {
        this.next = next;
    }

    public Vertex getPrevious() {
        return previous;
    }

    public void setPrevious(Vertex previous) {
        this.previous = previous;
    }

}
