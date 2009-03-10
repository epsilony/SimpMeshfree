/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.model.ModelElement.ModelElementType;

/**
 *
 * @author epsilon
 */
public class Vertex extends Point{

    Segment l,r;

    @Override
    public ModelElementType type() {
        return ModelElementType.Vertex;
    }

    public Vertex(Point x,Segment l,Segment r) {
        super(x);
        this.l=l;
        this.r=r;
    }
}
