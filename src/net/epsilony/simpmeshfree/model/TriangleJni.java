/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

/**
 *
 * @author epsilon
 */
public class TriangleJni {

    public native void triangleFun(double[] io);
    static {
        System.load("/home/epsilon/documents/4_java/JavaProject/TriangleJni/dist/libTriangleJni.so");
    }
    public static void main(String []args){
        new TriangleJni().triangleFun(new double[1]);
    }
}