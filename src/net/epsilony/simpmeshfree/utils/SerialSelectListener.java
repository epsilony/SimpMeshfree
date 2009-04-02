/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author epsilon
 */
public interface SerialSelectListener {

    /**
     * clear the former rubberImage write the rubberImage and call manager.{@link ModelPanelManager#getPanel() getPanel()}.repaint(...),
     * @param x
     * @param y
     * @param rubber
     * @param manager
     */
    public void rubber(int x, int y, BufferedImage rubber, ModelPanelManager manager);

    /**
     * manager will call this method, when a new point in JPanel is selected
     * @param x
     * @param y
     * @param manager
     */
    public void selectedPoint(int x, int y, ModelPanelManager manager);

    /**
     * ModelPanelManager will call this method after calling SelectedPoint when the selected points size is <
     * {@link #getPointSerialSize()}
     * @param index index>0 index<=getPointSerialSize
     */
    public void setNextPointIndex(int index);

    /**
     * 当selectPoint 被调用后，如果最近一次setNextPointIndex的值>=这个函数的返回值则完成整个选择过程。
     * @return
     */
    public int getPointSerialSize();

    /**
     * 当一个约定的事件（mouse中键单击事件）后调用此函数并结束选择
     */
    public void terminateSelection();

//    public static class polynomialSerialSelectListener implements SerialSelectListener{
//        LinkedList <Point2D>points=new LinkedList<Point2D>();
//        @Override
//        public void rubber(int x, int y, BufferedImage rubber, ModelPanelManager manager) {
//            if(points.size()==0)
//            {
//                return;
//            }
//            ListIterator<Point2D> pit=points.listIterator();
//
//        }
//
//        @Override
//        public void selectedPoint(int x, int y, ModelPanelManager manager) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void setNextPointIndex(int index) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public int getPointSerialSize() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void terminateSelection() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }
}
