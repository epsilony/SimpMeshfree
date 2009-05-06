/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.epsilony.simpmeshfree.model.geometry;

import java.util.LinkedList;

/**
 *
 * @author epsilon
 */
public class GeometryLog {
//    private GeometryLog(){};
//    public final static GeometryLog gl=new GeometryLog();
    static LinkedList<String> logStrings=new LinkedList<String>();



    public static void add(String log){
        logStrings.add(log);
    }

    public static LinkedList<String> getLogStrings(){
        return logStrings;
    }

}
