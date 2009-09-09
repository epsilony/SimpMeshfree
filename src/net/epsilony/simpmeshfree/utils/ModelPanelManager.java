/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.Shape;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.Math.*;
import net.epsilony.simpmeshfree.model2D.Point;

/**
 *
 * @author epsilon
 */
public class ModelPanelManager implements MouseMotionListener, MouseListener, MouseWheelListener, HierarchyBoundsListener {

    transient public static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelPanelManager.class);
    

//    static {
//        log.setLevel(org.apache.log4j.Level.DEBUG);
//    }
    //从模型空间到显示空间的2维坐标变换
    private AffineTransform viewTransform = new AffineTransform();
    public static int SLECECT_MIN_TIME_SCAPE = 200;
    private Cursor preCursor;

    /**
     * 设置2维模型空间的范围，(x1,y1)－(x2,y2)是全完囊括模型空间的一个矩形的对角线
     * <br>只有正确的设置了模型的空间范围{@link #viewZoom(double, double, double, double) }与{@link #viewWhole() }才能正常工作</br>
     * <br> x1与x2以及y1与y2的大小关系不需事先确定
     * <br>synchronized by modelImage Lock</br>
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void setModelBound(double x1, double y1, double x2, double y2) {
        viewOperationLock.lock();
        try {
            if (x1 > x2) {
                minX = x2;
                maxX = x1;
            } else {
                minX = x1;
                maxX = x2;
            }
            if (y1 > y2) {
                minY = y2;
                maxY = y1;
            } else {
                minY = y1;
                maxY = y2;
            }
        } finally {
            viewOperationLock.unlock();
        }
    }
    //modelBounds 包围模型坐标值
    double minX, maxX, minY, maxY;


    {
        topMargin = leftMargin = rightMargin = downMargin = 10;
    }
    double topMargin, leftMargin, downMargin, rightMargin;

    @Override
    public void ancestorMoved(HierarchyEvent e) {
    }

    @Override
    public void ancestorResized(HierarchyEvent e) {
        if (panel.getWidth() == 0 || panel.getHeight() == 0) {
            return;
        }
        boolean needgc = false;
        viewOperationLock.lock();
        try {
            if (modelImage.getWidth() < panel.getWidth() || modelImage.getHeight() < panel.getHeight()) {
                BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
                WritableRaster wr = bi.getRaster();
                wr.setRect(modelImage.getRaster());
                modelImage = bi;

                bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
                wr = bi.getRaster();
                wr.setRect(rubberImage.getRaster());
                rubberImage = bi;
                needgc = true;
            }
        } finally {
            viewOperationLock.unlock();
        }
        if (needgc) {
            System.gc();
        }
    }

    public enum ViewMoveOperationStatus {

        None,
        MoveStarted, MoveFirstPointSet
    }
    ViewMoveOperationStatus viewMoveOperation = ViewMoveOperationStatus.None;

    public class ZoomSelectListener implements DomainSelectListener {

        Color rubberColor = Color.ORANGE;
        Rectangle2D rubberRect = new Rectangle2D.Double();

        @Override
        public Rectangle2D selecting(int x1, int y1, int x2, int y2, ModelPanelManager vt, BufferedImage rubberImage) {
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setColor(rubberColor);
            g2.setComposite(AlphaComposite.Src);
            if (x1 > x2) {
                int t = x2;
                x2 = x1;
                x1 = t;
            }
            if (y1 > y2) {
                int t = y2;
                y2 = y1;
                y1 = t;
            }
            g2.drawRect(x1, y1, x2 - x1, y2 - y1);
            return null;
        }
        private Point2D pt1 = new Point2D.Double(),  pt2 = new Point2D.Double();

        ZoomSelectListener(Color rubberColor) {
            this.rubberColor = rubberColor;
        }

        ZoomSelectListener() {
        }

        public void setRubberColor(Color rubberColor) {
            selectLock.lock();
            try {
                this.rubberColor = rubberColor;
            } finally {
                selectLock.unlock();
            }
        }

        @Override
        public boolean selected(int x1, int y1, int x2, int y2, ModelPanelManager vt) {
            try {
                vt.inverseTransform(x1, y1, pt1);
                vt.inverseTransform(x2, y2, pt2);
                vt.viewZoom(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
                repaintModel();
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }

        @Override
        public boolean isRubberAutoClear() {
            return true;
        }

        @Override
        public void clearRubber(BufferedImage rubberImage, ModelPanelManager aThis) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    public final ZoomSelectListener zoomSelectListener = new ZoomSelectListener();

    enum SelectStatus {

        None, DomainSelectStarted, DomainSelecting, DomainSelected, CenterSelecting,
        CenterSelected, SerialSelecting, SerialSelected;
    }
    SelectStatus selectStatus = SelectStatus.None;
    ReentrantLock selectLock = new ReentrantLock();
    Condition selectCondition = selectLock.newCondition();
    int sx1, sy1, sx2, sy2;
    BufferedImage rubberImage;
    BufferedImage modelImage;

    private class SelectTask implements Runnable {

        DomainSelectListener listener;
        SerialSelectListener serialListener;
        int formerRubberXmin = 0, formerRubberYmin = 0, formerRubberXmax = 1, formerRubberYmax = 1;
        boolean stopped = false;
        boolean autoRepaint = false;
        int serialSelectIndex = 0;
        Rectangle2D formRubberRect;

        public void setListener(DomainSelectListener listener) {
            selectLock.lock();
            try {
                this.listener = listener;
            } finally {
                selectLock.unlock();
            }
        }

        @Override
        public void run() {
            Graphics2D g2;
            int tx1 = 0, tx2 = 0, ty1 = 0, ty2 = 0;
            selectLock.lock();
            try {
                while (!stopped) {
                    try {
                        switch (selectStatus) {
                            case None:
                                selectCondition.await();
                                continue;
                            case DomainSelectStarted:
                                formerRubberXmin = 0;
                                formerRubberYmin = 0;
                                formerRubberYmax = rubberImage.getHeight();
                                formerRubberXmax = rubberImage.getWidth();
                                selectCondition.await();
                                continue;
                            case CenterSelecting:
                                formerRubberXmin = 0;
                                formerRubberYmin = 0;
                                formerRubberYmax = rubberImage.getHeight();
                                formerRubberXmax = rubberImage.getWidth();
                            case DomainSelecting:
                                g2 = rubberImage.createGraphics();
                                if (listener.isRubberAutoClear()) {
                                    clearRubber(formerRubberXmin, formerRubberYmin, formerRubberXmax - formerRubberXmin + 1, formerRubberYmax - formerRubberYmin + 1);
                                } else {
                                    listener.clearRubber(rubberImage, ModelPanelManager.this);
                                }

                                formRubberRect = listener.selecting(sx1, sy1, sx2, sy2, ModelPanelManager.this, rubberImage);
                                if (listener.isRubberAutoClear()) {
                                    tx1 = formerRubberXmin;
                                    ty1 = formerRubberYmin;
                                    tx2 = formerRubberXmax;
                                    ty2 = formerRubberYmax;
                                    if (null == formRubberRect) {
                                        formerRubberXmin = min(sx1, sx2);
                                        formerRubberYmin = min(sy1, sy2);
                                        formerRubberXmax = max(sx1, sx2);
                                        formerRubberYmax = max(sy1, sy2);
                                    } else {
                                        formerRubberXmin = (int) formRubberRect.getMinX();
                                        formerRubberYmin = (int) formRubberRect.getMinY();
                                        formerRubberXmax = 1 + (int) formRubberRect.getMaxX();
                                        formerRubberYmax = 1 + (int) formRubberRect.getMaxY();
                                    }
                                    tx1 = min(tx1, formerRubberXmin);
                                    ty1 = min(ty1, formerRubberYmin);
                                    tx2 = max(tx2, formerRubberXmax);
                                    ty2 = max(ty2, formerRubberXmax);
                                    panel.repaint(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);
                                }
                                selectCondition.await();
                                continue;
                            case CenterSelected:
                            case DomainSelected:
                                clearRubber();
                                modelImageLock.lock();
                                try {
                                    if (listener.selected(sx1, sy1, sx2, sy2, ModelPanelManager.this)) {
                                        repaintModel();
                                    }
                                    selectStatus = SelectStatus.None;
                                } finally {
                                    modelImageLock.unlock();
                                }
                                continue;
                            case SerialSelecting:
                                serialListener.rubber(sx1, sy1, rubberImage, ModelPanelManager.this);
                                selectCondition.await();
                                continue;
                            case SerialSelected:
                                serialListener.setNextPointIndex(serialSelectIndex);
                                serialListener.selectedPoint(sx1, sy1, ModelPanelManager.this);
                                if (serialSelectIndex < serialListener.getPointSerialSize() - 1) {
                                    selectStatus = SelectStatus.SerialSelecting;
                                } else {
                                    clearRubber();
                                    selectStatus = SelectStatus.None;
                                }
                                continue;

                            default:
                                selectCondition.await();
                                continue;
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                        g2 = rubberImage.createGraphics();
                        g2.setComposite(AlphaComposite.Clear);
                        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
                        g2.setComposite(AlphaComposite.Src);
                        panel.repaint();
                        continue;
                    }
                }

            } finally {
                selectLock.unlock();
            }
        }
        Thread thread;

        public SelectTask() {
            thread = new Thread(this);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }

        private void stop() {
            selectLock.lock();
            try {
                stopped = true;
                selectCondition.signalAll();
            } finally {
                selectLock.unlock();
            }
        }

        /**
         * stop right now selecting and clear the rubberImage and repaint
         */
        private void cancel() {
            selectLock.lock();
            try {
                clearRubber();
                selectStatus = SelectStatus.None;
                selectCondition.signalAll();
            } finally {
                selectLock.unlock();
            }

        }

        private void clearRubber() {
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, rubberImage.getWidth(), rubberImage.getHeight());
            g2.setComposite(AlphaComposite.Src);
            panel.repaint();
        }

        private void clearRubber(int x, int y, int width, int height) {
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(x, y, width, height);
            g2.setComposite(AlphaComposite.Src);
        }
    }
    private SelectTask selectTask = new SelectTask();
    private int centerSelectSize;
    public Cursor selectingCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

    /**
     * 开始选择一个矩形域
     * @param listener 选择过程中以及选择后的动作响应接口
     * @see DomainSelectListener
     */
    public void fireDomainSelect(DomainSelectListener listener) {
        try {
            if (!selectLock.tryLock()) {
                selectTask.cancel();
                if (selectLock.tryLock(SLECECT_MIN_TIME_SCAPE, TimeUnit.MICROSECONDS)) {
                    try {
                        selectTask.setListener(listener);
                        selectStatus = SelectStatus.DomainSelectStarted;
                        selectCondition.signalAll();

                        panel.setCursor(selectingCursor);
                    } finally {
                        selectLock.unlock();
                    }
                }
            } else {
                try {
                    selectTask.setListener(listener);
                    selectStatus = SelectStatus.DomainSelectStarted;
                    selectCondition.signalAll();

                    panel.setCursor(selectingCursor);
                } finally {
                    selectLock.unlock();
                }
            }

        } catch (InterruptedException ex) {
            return;
        }
    }

    /**
     * 开始选择一个小区域，这个正方型域的中心由鼠标指定，半径为selectSize个pixel
     *
     * @param listener
     * @param selectSize
     */
    public void fireCenterSelect(DomainSelectListener listener, int selectSize) {
        try {
            if (!selectLock.tryLock()) {
                selectTask.cancel();
                if (selectLock.tryLock(SLECECT_MIN_TIME_SCAPE, TimeUnit.MICROSECONDS)) {
                    try {
                        selectTask.setListener(listener);
                        centerSelectSize = selectSize;
                        selectStatus = SelectStatus.CenterSelecting;
                        panel.setCursor(selectingCursor);
                    } finally {
                        selectLock.unlock();
                    }
                }
            } else {
                try {
                    selectTask.setListener(listener);
                    selectStatus = SelectStatus.CenterSelecting;
                    panel.setCursor(selectingCursor);
                    centerSelectSize = selectSize;
                } finally {
                    selectLock.unlock();
                }
            }
        } catch (InterruptedException ex) {
            return;
        }
    }

    public void fireSerialSelect(SerialSelectListener serialListener) {
        try {
            if (!selectLock.tryLock()) {
                selectTask.cancel();
                if (selectLock.tryLock(SLECECT_MIN_TIME_SCAPE, TimeUnit.MICROSECONDS)) {
                    try {
                        selectTask.serialListener = serialListener;
                        selectStatus = SelectStatus.SerialSelecting;
                        selectTask.serialSelectIndex = 0;
                        panel.setCursor(selectingCursor);
                    } finally {
                        selectLock.unlock();
                    }
                }
            } else {
                try {
                    selectTask.serialListener = serialListener;
                    selectStatus = SelectStatus.SerialSelecting;
                    selectTask.serialSelectIndex = 0;
                    selectCondition.signalAll();
                    panel.setCursor(selectingCursor);
                } finally {
                    selectLock.unlock();
                }
            }

        } catch (InterruptedException ex) {
            return;
        }
    }

    public JPanel getPanel() {
        return panel;
    }
    JPanel panel;

    /**
     * 设置新的JPanel连接，将一些鼠标动作响应也做相应的转移
     * <br>注！：必须在脱离连接的JPanel的子类与新连接的JPanel子类中重新设置相应的{@link ModelPanelManager}引用。</br>
     * <br>synchronized by modelImage Lock</br>
     * <br>synchronized by select Lock</br>
     * @param panel
     */
    public void setPanel(JPanel panel) {
        panel.removeMouseListener(this);
        panel.removeMouseMotionListener(this);
        panel.removeMouseWheelListener(this);
        panel.removeHierarchyBoundsListener(this);

        panel.setCursor(Cursor.getDefaultCursor());
        this.panel = panel;
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
        panel.addHierarchyBoundsListener(this);

        selectLock.lock();
        try {
            selectStatus = SelectStatus.None;
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, rubberImage.getWidth(), rubberImage.getHeight());
            g2.setComposite(AlphaComposite.Src);
            selectCondition.signalAll();
        } finally {
            selectLock.unlock();
        }
        viewWhole();
        repaintModel();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        switch (viewMoveOperation) {
            case MoveFirstPointSet:
                viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                viewFirstX = e.getX();
                viewFirstY = e.getY();
                repaintModel();
                return;

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            if (selectLock.tryLock(10, TimeUnit.MICROSECONDS)) {
                try {
                    switch (selectStatus) {
                        case DomainSelecting:
                            sx2 = e.getX();
                            sy2 = e.getY();
                            selectCondition.signalAll();
                            break;
                        case CenterSelecting:
                            sx1 = e.getX() - centerSelectSize;
                            sx2 = e.getX() + centerSelectSize;
                            sy1 = e.getY() - centerSelectSize;
                            sy2 = e.getY() + centerSelectSize;
                            selectCondition.signalAll();
                            break;
                        case SerialSelecting:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            selectCondition.signalAll();
                            break;
                    }
                } finally {
                    selectLock.unlock();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    int viewFirstX, viewFirstY;

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                switch (e.getClickCount()) {
                    case 2:
                        viewWhole();
                        repaintModel();
                        return;
                    case 1:
                        selectLock.lock();
                        try {
                            if (selectStatus == SelectStatus.SerialSelecting) {
                                selectTask.serialListener.terminateSelection();
                                selectTask.clearRubber();
                                selectStatus = SelectStatus.None;
                                selectCondition.signalAll();
                            }
                        } finally {
                            selectLock.unlock();
                        }
                }
                break;
            case MouseEvent.BUTTON1:
                selectLock.lock();
                try {
                    switch (selectStatus) {
                        case DomainSelectStarted:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            selectStatus = SelectStatus.DomainSelecting;
                            break;
                        case DomainSelecting:
                            sx2 = e.getX();
                            sy2 = e.getY();
                            selectStatus = SelectStatus.DomainSelected;
                            selectCondition.signalAll();
                            panel.setCursor(Cursor.getDefaultCursor());
                            break;
                        case CenterSelecting:
                            sx1 = e.getX() - centerSelectSize;
                            sy1 = e.getY() - centerSelectSize;
                            sx2 = e.getX() + centerSelectSize;
                            sy2 = e.getY() + centerSelectSize;
                            selectStatus = SelectStatus.CenterSelected;
                            selectCondition.signalAll();
                            panel.setCursor(Cursor.getDefaultCursor());
                            break;
                        case SerialSelecting:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            selectTask.serialSelectIndex++;
                            selectStatus = SelectStatus.SerialSelected;
                            selectCondition.signalAll();
                            if (selectTask.serialSelectIndex >= selectTask.serialListener.getPointSerialSize() - 1) {
                                panel.setCursor(Cursor.getDefaultCursor());
                            }
                            break;
                    }
                } finally {
                    selectLock.unlock();
                }
                break;
            //cancel selecting
            case MouseEvent.BUTTON3:
                selectLock.lock();
                try {
                    switch (e.getClickCount()) {
                        case 2:
                            switch (selectStatus) {
                                case DomainSelectStarted:
                                    selectTask.cancel();
                                    panel.setCursor(Cursor.getDefaultCursor());
                                    break;
                                case DomainSelecting:
                                    selectStatus = SelectStatus.DomainSelectStarted;
                                    selectTask.clearRubber();
                                    selectCondition.signalAll();
                                    break;
                                case CenterSelecting:
                                    selectTask.cancel();
                                    panel.setCursor(Cursor.getDefaultCursor());
                                    break;
                                case SerialSelecting:
                                    selectTask.serialSelectIndex--;
                                    if (selectTask.serialSelectIndex >= 0) {
                                        selectTask.serialListener.setNextPointIndex(selectTask.serialSelectIndex);
                                        sx1 = e.getX();
                                        sy1 = e.getY();
                                        selectCondition.signalAll();
                                    } else {
                                        selectTask.cancel();
                                        panel.setCursor(Cursor.getDefaultCursor());
                                    }
                            }
                            break;
                    }
                } finally {
                    selectLock.unlock();
                }
        }

    }

    /**
     * 开始选择模型的显示区域
     */
    public void selectViewZoom() {
        fireDomainSelect(zoomSelectListener);
    }

    /**
     * 开始选择模型的显示区域
     * @param rubberColor 那个方框框的颜色
     */
    public void selectViewZoom(Color rubberColor) {
        fireDomainSelect(new ZoomSelectListener(rubberColor));
    }

    @Override
    public void mousePressed(MouseEvent e) {

        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
//                setDomainSelectStatus(SelectStatus.None);
                viewFirstX = e.getX();
                viewFirstY = e.getY();
                viewMoveOperation = ViewMoveOperationStatus.MoveFirstPointSet;
                preCursor = panel.getCursor();
                panel.setCursor(modelMovingCursor);
                break;
        }
    }
    Cursor modelMovingCursor = new Cursor(Cursor.MOVE_CURSOR);

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                if (viewMoveOperation == ViewMoveOperationStatus.MoveFirstPointSet) {
                    panel.setCursor(preCursor);
                    if (e.getX() - viewFirstX == 0 && e.getY() - viewFirstY == 0) {
                        break;
                    }
                    viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                    repaintModel();
                    viewMoveOperation = ViewMoveOperationStatus.None;
//                    selectLock.lock();
//                    try {
//                        switch (selectStatus) {
//                            case DomainSelecting:
//                                sx2 = e.getX();
//                                sy2 = e.getY();
//                                selectCondition.signalAll();
//                                break;
//                            case CenterSelecting:
//                                sx1 = e.getX() - centerSelectSize;
//                                sx2 = e.getX() + centerSelectSize;
//                                sy1 = e.getY() - centerSelectSize;
//                                sy2 = e.getY() + centerSelectSize;
//                                selectCondition.signalAll();
//                                break;
//                        }
//                    } finally {
//                        selectLock.unlock();
//                    }
                    return;
                }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        selectLock.lock();
        try {
            switch (selectStatus) {
                case DomainSelecting:
                    sx2 = e.getX();
                    sy2 = e.getY();
                    selectCondition.signalAll();
                    break;
                case CenterSelecting:
                    sx1 = e.getX() - centerSelectSize;
                    sy1 = e.getY() - centerSelectSize;
                    sx2 = e.getX() + centerSelectSize;
                    sy2 = e.getY() + centerSelectSize;
                    selectCondition.signalAll();
                    break;
                case SerialSelecting:
                    sx1 = e.getX();
                    sy1 = e.getY();
                    selectCondition.signalAll();
                    break;
            }
        } finally {
            selectLock.unlock();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        selectLock.lock();
//        int tx1, tx2, ty1, ty2;
        try {
            switch (selectStatus) {
                case DomainSelecting:
                case CenterSelecting:
//                    tx1 = min(sx1, sx2);
//                    tx2 = max(sx1, sx2);
//                    ty1 = min(sy1, sy2);
//                    ty2 = max(sy1, sy2);
                    if (selectTask.listener.isRubberAutoClear()) {
                        Graphics2D g2 = rubberImage.createGraphics();
                        g2.setComposite(AlphaComposite.Clear);
                        g2.fillRect(selectTask.formerRubberXmin, selectTask.formerRubberYmin,
                                selectTask.formerRubberXmax - selectTask.formerRubberXmin + 1,
                                selectTask.formerRubberYmax - selectTask.formerRubberYmin + 1);
                        panel.repaint(selectTask.formerRubberXmin, selectTask.formerRubberYmin,
                                selectTask.formerRubberXmax - selectTask.formerRubberXmin + 1,
                                selectTask.formerRubberYmax - selectTask.formerRubberYmin + 1);
                    } else {
                        selectTask.listener.clearRubber(rubberImage, this);
                    }
//                    g2.fillRect(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);
//                    panel.repaint(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);

                    break;
                case SerialSelecting:
                    selectTask.clearRubber();
                    break;
            }
        } finally {
            selectLock.unlock();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double r = Math.pow(1 + wheelRatio, e.getWheelRotation());
//        setDomainSelectStatus(SelectStatus.None);
        viewScale(e.getX(), e.getY(), r);
        repaintModel();
    }

    /**
     * 设置滚轮滚动一下图片缩放的比例，默认为0.05
     * @param wheelRatio
     */
    public void setWheelRatio(double wheelRatio) {
        this.wheelRatio = wheelRatio;
    }
    double wheelRatio = 0.1;

    /**
     * 从JPanel的坐标向模型空间坐标的逆变换
     * <br>synchronized by modelImage Lock</br>
     * @param scrX JPanel的横坐标
     * @param scrY JPanel的纵坐标
     * @param dstPt 用以储存逆变换结果二维点
     * @return 进行过变换操作后的 dstPt
     * @throws java.awt.geom.NoninvertibleTransformException
     */
    public Point2D inverseTransform(int scrX, int scrY, Point2D dstPt) throws NoninvertibleTransformException {
        viewOperationLock.lock();
        try {
            forInverseTransform.setLocation(scrX, scrY);
            return viewTransform.inverseTransform(forInverseTransform, dstPt);
        } finally {
            viewOperationLock.unlock();
        }

    }
    private Point2D forInverseTransform = new Point2D.Double();

    /**
     * 将JPanel上长为n的东东变换为模型空间的对应长度
     * <br>synchronized by modelImage Lock</br>
     * @param length
     * @return
     */
    public double inverseTransLength(int length) {
        viewOperationLock.lock();
        try {
            return length / viewTransform.getScaleX();
        } finally {
            viewOperationLock.unlock();
        }

    }

    /**
     * 设置显示全模型时JPanel上下左右留边的长度，单位为pixel
     * <br>synchronized by modelImage Lock</br>
     * @param top
     * @param down
     * @param left
     * @param right
     */
    public void setMargin(double top, double down, double left, double right) {
        viewOperationLock.lock();
        try {
            topMargin = top;
            downMargin = down;
            leftMargin = left;
            rightMargin = right;
        } finally {
            viewOperationLock.unlock();
        }

    }

    /**
     * 重要的函数，应在本类所连接的JPanel子类覆盖paintComponent()方法，并在其中加入本函数的调用
     * <PRE>
     * inside the extended JPanel:
     * ModelPanelManager manger=...
     * @overide
     * private void paintComponent(){
     *      super.paintComponent();
     *      ...
     *      manager.paintPanel();
     *      ....
     * }
     * </PRE>
     * @param g2
     */
    public void paintPanel(Graphics2D g2) {
        g2.setComposite(AlphaComposite.SrcAtop);

//        if (modelImageLock.tryLock()) {
//        long t1 = System.nanoTime();
        try {
            modelImageLock.lock();
            g2.drawImage(modelImage, null, panel);
//            g2.drawImage(modelImage, null, null);
        } finally {
            modelImageLock.unlock();
        }

        selectLock.lock();
        try {
            g2.drawImage(rubberImage, null, panel);
        } finally {
            selectLock.unlock();
        }
//        long t2 = System.nanoTime();
//        System.out.println("Nano time:" + (t2 - t1));
//        }
    }
    private int repaintModelOnly;
    public final static int MODEL_GENERATED = 2;
    public final static int MODEL_GENERATING = 1;
    private ReentrantLock repaintModelOnlyLock = new ReentrantLock();

    public int getRepaintModelOnly() {
        try {
            repaintModelOnlyLock.lock();
            return repaintModelOnly;
        } finally {
            repaintModelOnlyLock.unlock();
        }
    }

    public void setRepaintModelOnly(int repaintModelOnly) {
        try {
            repaintModelOnlyLock.lock();
            this.repaintModelOnly = repaintModelOnly;
        } finally {
            repaintModelOnlyLock.unlock();
        }
    }
    private volatile boolean modelPaintingCanceled = false;
    private volatile boolean modelImageNeedsRegeneration = false;

    public boolean isImagePaintingCanceled() {
        return modelPaintingCanceled;
    }

    class ModelImageRepaintTask implements Runnable {

        LinkedList<ModelImagePainter> imagePainters = new LinkedList<ModelImagePainter>();
        private boolean stopped;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override
        public void run() {
            int tx, ty, twidth, theight;
            modelImageLock.lock();
            try {
                while (!stopped) {

                    if (!modelImageNeedsRegeneration) {
                        try {
                            modelImageCondition.await();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                            continue;
                        }
                    }
                    if (stopped) {
                        break;
                    }
                    try {
                        viewOperationLock.lock();
                        tx = x;
                        ty = y;
                        theight = height;
                        twidth = width;
                    } finally {
                        viewOperationLock.unlock();
                    }
//                        if (modelPaintingCanceled) {
//                            modelPaintingCanceled = false;
//                            continue;
//                        }
                    long time = System.nanoTime();
                    Graphics2D g2 = modelImage.createGraphics();
                    g2.setComposite(AlphaComposite.Clear);
                    g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());
                    if (modelPaintingCanceled) {
                        modelPaintingCanceled = false;
                        continue;
                    }
                    for (ModelImagePainter painter : imagePainters) {
                        painter.paintModel(modelImage, ModelPanelManager.this);
                        if (modelPaintingCanceled) {
                            break;
                        }
                    }
                    if (!modelPaintingCanceled) {
                        panel.repaint(tx, ty, twidth, theight);
                        modelImageNeedsRegeneration = false;
                    }
                    modelPaintingCanceled = false;
                    time = System.nanoTime() - time;
                    if (log.isDebugEnabled()) {
                        if (modelPaintingCanceled) {
                            log.debug("Model Image Generation canceled");
                        }
                        log.debug("Generating Model Image costs " + time + " nano secs");
                    }
                }
            } finally {
                modelImageLock.unlock();
            }
        }
        Thread thread;

        public void stop() {
            modelImageLock.lock();
            try {
                stopped = true;
                modelImageCondition.signalAll();
            } finally {
                modelImageLock.unlock();
            }
        }

        public void repaintModel() {
            repaintModel(0, 0, panel.getWidth(), panel.getHeight());
        }

        public void repaintModel(int x, int y, int width, int height) {
            viewOperationLock.lock();
            try {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            } finally {
                viewOperationLock.unlock();
            }
//            try {
//                if (!modelImageLock.tryLock(1, TimeUnit.MICROSECONDS)) {
            if (!modelImageLock.tryLock()) {
                modelPaintingCanceled = true;
                modelImageNeedsRegeneration = true;
            } else {
                try {
                    modelImageCondition.signalAll();
                } finally {
                    modelImageLock.unlock();
                }
            }
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
//            }

        }

        public ModelImageRepaintTask() {
            thread = new Thread(this);
//            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }

        public void addImagePainter(ModelImagePainter painter) {
            modelImageLock.lock();
            try {
                imagePainters.add(painter);
            } finally {
                modelImageLock.unlock();
            }
        }
    }
    /**
     * 用于锁住modelImage，本类中任何与modelImage有关的操作都应该上锁此锁
     */
    ReentrantLock modelImageLock = new ReentrantLock();
    Condition modelImageCondition = modelImageLock.newCondition();
    ModelImageRepaintTask modelImageRepaintTask = new ModelImageRepaintTask();

    /**
     * 注：一般重设后需要调用{@link #setModelBound(double, double, double, double) } {@link #viewWhole() }
     * <br>synchronized by modelImage Lock</br>
     * @param writer
     * @see ModelImagePainter
     */
    public void addModelImagePainter(ModelImagePainter painte) {
        modelImageRepaintTask.addImagePainter(painte);
    }

    /**
     * 重新生成modelImage并在本类所连接的JPanel中重画modelImage
     * <PRE>
     * 与getPanel().repaint 不同的是:
     *      repaintModel将调用本类所连接的ModelImageWriter用以重新生成modelImage
     *      getPanel().repaint 将只重画modelImage
     * </PRE>
     * @see #paintPanel(g2)
     */
    public void repaintModel() {
        modelImageRepaintTask.repaintModel();
    }

    /**
     * x,y,width,height参数的意义同{@link JPanel#repaint(int, int, int, int) }
     * 函数说明见{@link #repaintModel()}
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void repaintModel(int x, int y, int width, int height) {
        modelImageRepaintTask.repaintModel(x, y, width, height);
    }

    public ModelPanelManager(JPanel panel, double x1, double y1, double x2, double y2) {
        this.panel = panel;
//        modelImage = new BufferedImage((int) panel.getPreferredSize().getWidth(), (int) panel.getPreferredSize().getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
//        rubberImage = new BufferedImage((int) panel.getPreferredSize().getWidth(), (int) panel.getPreferredSize().getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
        modelImage = new BufferedImage(1280, 1024, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        rubberImage = new BufferedImage(1280, 1024, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        panel.addMouseListener(this);
        panel.addMouseWheelListener(this);
        panel.addMouseMotionListener(this);
        panel.addHierarchyBoundsListener(this);
        setModelBound(x1, y1, x2, y2);
    }
    private ReentrantLock viewOperationLock = new ReentrantLock();
//    private Condition viewOperationCondition = viewOperationLock.newCondition();

    /**
     * 更改{@link #getViewTransform() getViewTransform()}的结果，使利用其变换显示在JPanel中的模型在JPanel上水平移动dx,单位向右一个pixel,竖直移动dy,单位向下一个pixel(!!)
     * <br>synchronized by modelImage Lock</br>
     * @param dx
     * @param dy
     */
    public void viewMove(double dx, double dy) {
        viewOperationLock.lock();
        try {
            AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);//);
            viewTransform.preConcatenate(tx);
//            repaintModel();
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * 更改{@link #getViewTransform() getViewTransform()}的结果，使利用其变换显示在JPanel中的模型以JPanel坐标(centetX,centerY)为不动点缩放t倍
     * <br>synchronized by modelImage Lock</br>
     * @param centerX
     * @param centerY
     * @param t
     */
    public void viewScale(double centerX, double centerY, double t) {
        viewOperationLock.lock();
        try {
            AffineTransform tx = AffineTransform.getTranslateInstance(centerX, centerY);
            tx.scale(t, t);
            tx.translate(-centerX, -centerY);
            viewTransform.preConcatenate(tx);
//            repaintModel();
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * 更改{@link #getViewTransform() getViewTransform()}的结果，
     * 使其将模型空间中以(zx1,zy1)-(zx2,zy2)为对角线的矩型所包含的所有东东尽可能大比例的变换到JPanel上
     * 与{@link #viewScale(double, double, double) }, {@link #viewMove(double, double) }不同，
     * 这个函数的参数zx1,zy1,zx2,zy2都是模型空间中的坐标。zx1和zx2以即zy1与zy2之间的大小关系没有严格要求
     * <br>synchronized by modelImage Lock</br>
     * @param zx1
     * @param zy1
     * @param zx2
     * @param zy2
     */
    public void viewZoom(double zx1, double zy1, double zx2, double zy2) {
        viewOperationLock.lock();
        try {
            viewTransform.setToIdentity();
            double dx = Math.abs(zx2 - zx1);
            double dy = Math.abs(zy2 - zy1);
            viewTransform.translate(panel.getWidth() / 2, panel.getHeight() / 2);
            double t = Math.min((panel.getWidth() - leftMargin - rightMargin) / dx, (panel.getHeight() - topMargin - downMargin) / dy);
            viewTransform.scale(t, -t);
            viewTransform.translate(-(zx1 + zx2) / 2, -(zy1 + zy2) / 2);
//            repaintModel();
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * 更改{@link #getViewTransform() getViewTransform()}的结果使其将整模型尽可能大的显示在JPanel上
     * 模型的包围框的设置见{@link #setModelBound(double, double, double, double)}
     * <br>synchronized by modelImage Lock</br>
     */
    public void viewWhole() {
        viewOperationLock.lock();
        try {
            viewZoom(minX, minY, maxX, maxY);
        } finally {
            viewOperationLock.unlock();
        }
    }

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
    }

    /**
     * 可以追加Path2D将模型空间中的一个点(x,y)以半径为size个pixel, 以type为形式在JPanel中相应位置标注出来
     * <br>synchronized by modelImage Lock</br>
     * @param x 模型空间中的一点x坐标
     * @param y 模型空间中一点y坐标
     * @param size 在JPanel中显示的点的标注的半径
     * @param type 点的标注的形式
     * @param path 用于输出
     */
    public void viewMarker(double x, double y, double size, ViewMarkerType type, Path2D path) {
        viewOperationLock.lock();
        try {
            modelPtMarker.setLocation(x, y);
            viewTransform.transform(modelPtMarker, screenPtMarker);
            size = Math.abs(size);
            switch (type) {
                case Rectangle:
                    rectMarker.setRect(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                    path.append(rectMarker, false);
                    break;
                case X:
                    path.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2);
                    path.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() + size / 2);
                    path.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() + size / 2);
                    path.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() - size / 2);
                    break;
                case Round:
                    ellMarker.setFrame(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                    path.append(ellMarker, false);
                    break;
                case DownTriangle:
                    path.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                    path.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                    path.lineTo(screenPtMarker.getX(), screenPtMarker.getY() + size / 2);
                    path.closePath();
                    break;
                case UpTriangle:
                    path.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                    path.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                    path.lineTo(screenPtMarker.getX(), screenPtMarker.getY() - size / 2);
                    path.closePath();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * 返回一个Shape使得其可以将模型空间中的一个点(x,y)以半径为size个pixel, 以type为形式在JPanel中相应位置标注出来
     * <br>synchronized by modelImage Lock</br>
     * @param x 模型空间中的一点x坐标
     * @param y 模型空间中一点y坐标
     * @param size 在JPanel中显示的点的标注的半径
     * @param type 点的标注的形式
     * @return 变换好了的可以直接在JPanel上输出的Shape
     */
    public Shape viewMarker(double x, double y, double size, ViewMarkerType type) {
        viewOperationLock.lock();
        try {
            modelPtMarker.setLocation(x, y);
            viewTransform.transform(modelPtMarker, screenPtMarker);
            Shape result;
            size = Math.abs(size);
            switch (type) {
                case Rectangle:
                    rectMarker.setRect(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                    result = rectMarker;
                    break;
                case X:
                    pathMarker.reset();
                    pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2);
                    pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() + size / 2);
                    pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() + size / 2);
                    pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() - size / 2);
                    result = pathMarker.createTransformedShape(null);
                    break;
                case Round:
                    ellMarker.setFrame(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                    result = ellMarker;
                    break;
                case DownTriangle:
                    pathMarker.reset();
                    pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                    pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                    pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() + size / 2);
                    pathMarker.closePath();
                    result = pathMarker.createTransformedShape(null);
                    break;
                case UpTriangle:
                    pathMarker.reset();
                    pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                    pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                    pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() - size / 2);
                    pathMarker.closePath();
                    result = pathMarker.createTransformedShape(null);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return result;
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * 返回一个Shape使得其可以将模型空间中的points为中心的点，以半径为size个pixel, 以type为形式在JPanel中相应位置标注出来
     * <br>synchronized by modelImage Lock</br>
     * @param points 模型空间中的点
     * @param size 在JPanel中显示的点的标注的半径
     * @param type 点的标注的形式
     * @return 变换好了的可以直接在JPanel上输出的Shape
     */
    public Shape viewMarker(Collection<? extends Point> points, double size, ViewMarkerType type) {
        viewOperationLock.lock();
        try {
            pathMarker.reset();
            switch (type) {
                case Rectangle:
                    for (Point pt : points) {
                        modelPtMarker.setLocation(pt.getX(), pt.getY());
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        rectMarker.setRect(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                        pathMarker.append(rectMarker, false);
                    }
                    break;
                case X:
                    for (Point pt : points) {
                        modelPtMarker.setLocation(pt.getX(), pt.getY());
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2);
                        pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() + size / 2);
                        pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() + size / 2);
                        pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() - size / 2);
                    }
                    break;
                case Round:
                    for (Point pt : points) {
                        modelPtMarker.setLocation(pt.getX(), pt.getY());
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        ellMarker.setFrame(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                        pathMarker.append(ellMarker, false);
                    }
                    break;
                case DownTriangle:
                    for (Point pt : points) {
                        modelPtMarker.setLocation(pt.getX(), pt.getY());
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                        pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                        pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() + size / 2);
                        pathMarker.closePath();
                    }
                    break;
                case UpTriangle:
                    for (Point pt : points) {
                        modelPtMarker.setLocation(pt.getX(), pt.getY());
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                        pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                        pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() - size / 2);
                        pathMarker.closePath();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return pathMarker.createTransformedShape(null);
        } finally {
            viewOperationLock.unlock();
        }


    }

    /**
     * 返回一个Shape使得其可以将模型空间中的一个点(x,y)以半径为size个pixel, 以markerShape为模板JPanel中相应位置标注出来
     * 注要比其它的viewMarker略慢一点
     * <br>synchronized by modelImage Lock</br>
     * @param points points 模型空间中的点
     * @param markerShape 点标注的模版，并认为其中心在JPanel空间中的(0,0)
     * @return 变换好了的可以直接在JPanel上输出的Shape
     */
    public Shape viewMarker(Collection<? extends Point> points, Shape markerShape) {
        viewOperationLock.lock();
        try {
            pathMarker.reset();
            AffineTransform tx = new AffineTransform();
            for (Point pt : points) {
                modelPtMarker.setLocation(pt.getX(), pt.getY());
                viewTransform.transform(modelPtMarker, screenPtMarker);
                tx.setToTranslation(screenPtMarker.getX(), screenPtMarker.getY());
                pathMarker.append(markerShape.getPathIterator(tx), false);
            }
        } finally {
            viewOperationLock.unlock();
        }
        return pathMarker.createTransformedShape(null);
    }

    public Shape viewMarker(double[] posts, double size, ViewMarkerType type) {
        viewOperationLock.lock();
        try {
            pathMarker.reset();
            switch (type) {
                case Rectangle:
                    for (int i = 0; i < posts.length; i += 2) {
                        modelPtMarker.setLocation(posts[i], posts[i + 1]);
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        rectMarker.setRect(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                        pathMarker.append(rectMarker, false);
                    }
                    break;
                case X:
                    for (int i = 0; i < posts.length; i += 2) {
                        modelPtMarker.setLocation(posts[i], posts[i + 1]);
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2);
                        pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() + size / 2);
                        pathMarker.moveTo(screenPtMarker.getX() - size / 2, screenPtMarker.getY() + size / 2);
                        pathMarker.lineTo(screenPtMarker.getX() + size / 2, screenPtMarker.getY() - size / 2);
                    }
                    break;
                case Round:
                    for (int i = 0; i < posts.length; i += 2) {
                        modelPtMarker.setLocation(posts[i], posts[i + 1]);
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        ellMarker.setFrame(screenPtMarker.getX() - size / 2, screenPtMarker.getY() - size / 2, size, size);
                        pathMarker.append(ellMarker, false);
                    }
                    break;
                case DownTriangle:
                    for (int i = 0; i < posts.length; i += 2) {
                        modelPtMarker.setLocation(posts[i], posts[i + 1]);
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                        pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() - size / 4);
                        pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() + size / 2);
                        pathMarker.closePath();
                    }
                    break;
                case UpTriangle:
                    for (int i = 0; i < posts.length; i += 2) {
                        modelPtMarker.setLocation(posts[i], posts[i + 1]);
                        viewTransform.transform(modelPtMarker, screenPtMarker);
                        pathMarker.moveTo(screenPtMarker.getX() - size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                        pathMarker.lineTo(screenPtMarker.getX() + size * sqrt(3) / 4, screenPtMarker.getY() + size / 4);
                        pathMarker.lineTo(screenPtMarker.getX(), screenPtMarker.getY() - size / 2);
                        pathMarker.closePath();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return pathMarker.createTransformedShape(null);
        } finally {
            viewOperationLock.unlock();
        }
    }
    private Point2D modelPtMarker = new Point2D.Double(),  modelPt2Marker = new Point2D.Double(),  screenPtMarker = new Point2D.Double();
    private Rectangle2D rectMarker = new Rectangle2D.Double();
    private Path2D pathMarker = new Path2D.Double();
    private Ellipse2D ellMarker = new Ellipse2D.Double();

    /**
     * synchronized by modelImage Lock
     * @return
     */
    public AffineTransform getViewTransform() {
        viewOperationLock.lock();
        try {
            return viewTransform;
        } finally {
            viewOperationLock.unlock();
        }
    }

    /**
     * viewMaker(p.getX(),p.getY(),size,type)
     * @param p
     * @param size
     * @param type
     * @return
     */
    public Shape viewMarker(Point p, double size, ViewMarkerType type) {
        return viewMarker(p.getX(), p.getY(), size, type);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        modelImageRepaintTask.stop();
        selectTask.stop();
    }

    public static void main(String[] args) throws InterruptedException {
        BufferedImage image1 = new BufferedImage(10, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        BufferedImage image2 = new BufferedImage(10, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        WritableRaster wr1 = image1.getRaster();
        WritableRaster wra1 = image1.getAlphaRaster();
        WritableRaster wr2 = image2.getRaster();
        WritableRaster wra2 = image2.getAlphaRaster();
        int[] is = new int[40];
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));

        System.out.println("1");
        Graphics2D g1 = image1.createGraphics();
        Graphics2D g2 = image2.createGraphics();
        g1.setBackground(new Color(0, 0, 255, 0));
        g1.clearRect(0, 0, 10, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("2");
        g1.setColor(new Color(0, 0, 255, 255));
        g1.fillRect(0, 0, 10, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("3");
        g1.setComposite(AlphaComposite.Clear);
        g1.fillRect(0, 0, 10, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("4");
        g1.setComposite(AlphaComposite.Dst);
        g1.fillRect(0, 0, 10, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("g1.getColor.getRGB() = " + g1.getColor().getAlpha() + " " + g1.getColor().getRGB());
        System.out.println("5 = " + 5);
        g1.setComposite(AlphaComposite.Src);
        g1.fillRect(0, 0, 10, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("g1.getColor.getRGB() = " + g1.getColor().getAlpha() + " " + g1.getColor().getRGB());
        System.out.println("6");
        g1.setComposite(AlphaComposite.Src);
        g1.setColor(Color.RED);
        g1.fillRect(0, 0, 5, 1);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("g1.getColor.getRGB() = " + g1.getColor().getAlpha() + " " + g1.getColor().getRGB());
        System.out.println("7 = " + 7);
        g2.setColor(Color.GREEN);
        g1.setComposite(AlphaComposite.SrcAtop);
        g2.fillRect(6, 0, 3, 1);
        g1.drawImage(image2, null, null);
        System.out.println("wr1.getPixels(0,0,10,1,is) = " + Arrays.toString(wr1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra1.getPixels(0,0,10,1,is) = " + Arrays.toString(wra1.getPixels(0, 0, 10, 1, is)));
        System.out.println("wr2.getPixels(0,0,10,1,is) = " + Arrays.toString(wr2.getPixels(0, 0, 10, 1, is)));
        System.out.println("wra2.getPixels(0,0,10,1,is) = " + Arrays.toString(wra2.getPixels(0, 0, 10, 1, is)));
        System.out.println("g1.getColor.getRGB() = " + g1.getColor().getAlpha() + " " + g1.getColor().getRGB());

    }
}
