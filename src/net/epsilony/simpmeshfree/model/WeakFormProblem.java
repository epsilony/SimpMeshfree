/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public interface WeakFormProblem {

    QuadraturePointIterator volumeIterator(int[] numOut);

    QuadraturePointIterator neumannIterator(int[] numOut);

    QuadraturePointIterator dirichletIterator(int[] numOut);

    VolumeCondition volumeCondition();
}
