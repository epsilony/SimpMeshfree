/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.Math.*;
import net.epsilony.simpmeshfree.model.geometry.Point;

/**
 *
 * @author epsilon
 */
public class ModelPanelManager implements MouseMotionListener, MouseListener, MouseWheelListener, JPanelPainter, HierarchyBoundsListener {

    private AffineTransform viewTransform = new AffineTransform();
    public static int DOMAIN_SLECECT_MIN_TIME_SCAPE = 200;
    //modelBounds
    double minX, maxX, minY, maxY;

    //JPanel width and height setting and havent' been rewrite the model buffer;
//    boolean panelResized;
    public void setModelBound(double x1, double y1, double x2, double y2) {
        modelImageLock.lock();
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
            modelImageLock.unlock();
        }
    }


    {
        topMargin = leftMargin = rightMargin = downMargin = 10;
    }
    double topMargin, leftMargin, downMargin, rightMargin;
    JPanel panel;
    ReentrantLock selectLock = new ReentrantLock();
    Condition selectCondition = selectLock.newCondition();
    int sx1, sy1, sx2, sy2;
    BufferedImage rubberImage;
    BufferedImage modelImage;
//    Graphics2D rubberG2 = rubberImage.createGraphics();
//    Graphics2D modelG2 = modelImage.createGraphics();

    @Override
    public void ancestorMoved(HierarchyEvent e) {
    }

    @Override
    public void ancestorResized(HierarchyEvent e) {
        if (panel.getWidth() == 0 || panel.getHeight() == 0) {
            return;
        }
        modelImageLock.lock();
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
                System.gc();
            }
        } finally {
            modelImageLock.unlock();
        }
    }

    public enum ViewMoveOperationStatus {

        None,
        MoveStarted, MoveFirstPointSet
    }
    ViewMoveOperationStatus viewMoveOperation = ViewMoveOperationStatus.None;

    class ZoomSelectListener implements SelectListener {

        Color rubberColor = new Color(0, 0, 255, 255);
        Rectangle2D rubberRect = new Rectangle2D.Double();

        @Override
        public void selecting(int x1, int y1, int x2, int y2, ModelPanelManager vt, BufferedImage rubberImage) {
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
        }
        private Point2D pt1 = new Point2D.Double(),  pt2 = new Point2D.Double();

        public ZoomSelectListener(Color rubberColor) {
            this.rubberColor = rubberColor;
        }

        public ZoomSelectListener() {
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
        public boolean isRubberAutoClearRepaint() {
            return true;
        }
    }
    ZoomSelectListener zoomSelectListener = new ZoomSelectListener();

    enum SelectStatus {

        None, DomainSelectStarted, DomainSelecting, DomainSelected, CenterSelecting,
        CenterSelected;
    }
    SelectStatus selectStatus = SelectStatus.None;

    private SelectStatus getSelectStatus() {
        selectLock.lock();
        try {
            return selectStatus;
        } finally {
            selectLock.unlock();
        }
    }

    private void setSelectStatus(SelectStatus selectStatus) {
        selectLock.lock();
        try {
            this.selectStatus = selectStatus;
        } finally {
            selectLock.unlock();
        }

    }

    private class SelectTask implements Runnable {

        SelectListener listener;
        int formerRubberXmin = 0, formerRubberYmin = 0, formerRubberXmax = 1024, formerRubberYmax = 768;
        boolean stopped = false;
        boolean autoRepaint = false;

        public void setListener(SelectListener listener) {
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
                            case DomainSelecting:
                                g2 = rubberImage.createGraphics();
                                if (listener.isRubberAutoClearRepaint()) {

                                    tx1 = min(min(sx1, sx2), formerRubberXmin);
                                    ty1 = min(min(sy1, sy2), formerRubberYmin);
                                    tx2 = max(max(sx1, sx2), formerRubberXmax);
                                    ty2 = max(max(sy1, sy2), formerRubberYmax);
                                    g2.setComposite(AlphaComposite.Clear);
                                    g2.fillRect(formerRubberXmin, formerRubberYmin, formerRubberXmax - formerRubberXmin + 1, formerRubberYmax - formerRubberYmin + 1);
                                }
                                g2.setComposite(AlphaComposite.Src);
                                listener.selecting(sx1, sy1, sx2, sy2, ModelPanelManager.this, rubberImage);
                                if (listener.isRubberAutoClearRepaint()) {
                                    panel.repaint(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);
                                    formerRubberXmin = min(sx1, sx2);
                                    formerRubberYmin = min(sy1, sy2);
                                    formerRubberXmax = max(sx1, sx2);
                                    formerRubberYmax = max(sy1, sy2);
                                }
                                selectCondition.await();
                                continue;
                            case CenterSelected:
                            case DomainSelected:
                                g2 = rubberImage.createGraphics();
                                g2.setComposite(AlphaComposite.Clear);
                                g2.fillRect(0, 0, rubberImage.getWidth(), rubberImage.getHeight());
                                g2.setComposite(AlphaComposite.Src);
                                modelImageLock.lock();
                                try {
                                    autoRepaint = listener.selected(sx1, sy1, sx2, sy2, ModelPanelManager.this);
                                    selectStatus = SelectStatus.None;
                                    if (autoRepaint) {
                                        repaintModel();
                                    }
                                } finally {
                                    modelImageLock.unlock();
                                }
                                continue;
                            default:
                                selectCondition.await();
                                continue;
                        }
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                        g2 = rubberImage.createGraphics();
                        g2.setComposite(AlphaComposite.Clear);
                        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
                        g2.setComposite(AlphaComposite.Src);
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
            thread.start();
        }

        public void stop() {
            selectLock.lock();
            try {
                stopped = true;
                selectCondition.signalAll();
            } finally {
                selectLock.unlock();
            }
        }

        private void cancel() {
            thread.interrupt();
        }
    }
    private SelectTask selectTask = new SelectTask();
    private int centerSelectSize;

    public void domainSelect(SelectListener listener) {
        try {
            selectTask.cancel();
            if (selectLock.tryLock(DOMAIN_SLECECT_MIN_TIME_SCAPE, TimeUnit.MICROSECONDS)) {
                try {
                    selectTask.setListener(listener);
                    selectStatus = SelectStatus.DomainSelectStarted;
                    selectCondition.signalAll();
                } finally {
                    selectLock.unlock();
                }
            }
        } catch (InterruptedException ex) {
            return;
        }
    }

    public void centerSelect(SelectListener listener, int selectSize) {
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
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
        selectLock.lock();
        try {
            if (getSelectStatus() == SelectStatus.DomainSelecting);
            sx2 = e.getX();
            sy2 = e.getY();
            selectCondition.signalAll();
        } finally {
            selectLock.unlock();
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
                }
                break;
            case MouseEvent.BUTTON1:
                selectLock.lock();
                try {
                    switch (getSelectStatus()) {
                        case DomainSelectStarted:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            setSelectStatus(SelectStatus.DomainSelecting);
                            selectCondition.signalAll();
                            break;
                        case DomainSelecting:
                            sx2 = e.getX();
                            sy2 = e.getY();
                             setSelectStatus(SelectStatus.DomainSelected);
                            selectCondition.signalAll();
                            break;
                        case CenterSelecting:
                            sx1 = e.getX() - centerSelectSize;
                            sy1 = e.getY() - centerSelectSize;
                            sx2 = e.getX() + centerSelectSize;
                            sy2 = e.getY() + centerSelectSize;
                            setSelectStatus(SelectStatus.CenterSelected);
                            selectCondition.signalAll();
                            break;
                    }
                } finally {
                    selectLock.unlock();
                }
        }

    }

    public void selectViewZoom() {
        domainSelect(zoomSelectListener);
    }

    public void selectViewZoom(Color rubberColor) {
        domainSelect(new ZoomSelectListener(rubberColor));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
//                setDomainSelectStatus(SelectStatus.None);
                viewFirstX = e.getX();
                viewFirstY = e.getY();
                viewMoveOperation = ViewMoveOperationStatus.MoveFirstPointSet;
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                if (viewMoveOperation == ViewMoveOperationStatus.MoveFirstPointSet) {
                    viewMove(e.getX() - viewFirstX, e.getY() - viewFirstY);
                    repaintModel();
                    viewMoveOperation = ViewMoveOperationStatus.None;
                    selectLock.lock();
                    try {
                        if (selectStatus == SelectStatus.DomainSelecting) {
                            sx2 = e.getX();
                            sy2 = e.getY();
                            selectCondition.signalAll();
                        }
                    } finally {
                        selectLock.unlock();
                    }
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
            }
        } finally {
            selectLock.unlock();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        selectLock.lock();
        int tx1, tx2, ty1, ty2;
        try {
            switch (selectStatus) {
                case DomainSelecting:
                case CenterSelecting:
                    tx1 = min(sx1, sx2);
                    tx2 = max(sx1, sx2);
                    ty1 = min(sy1, sy2);
                    ty2 = max(sy1, sy2);
                    Graphics2D g2 = rubberImage.createGraphics();
                    g2.setComposite(AlphaComposite.Clear);
                    g2.fillRect(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);
                    panel.repaint(tx1, ty1, tx2 - tx1 + 1, ty2 - ty1 + 1);
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
    double wheelRatio = 0.05;

    public void setWheelRatio(double wheelRatio) {
        this.wheelRatio = wheelRatio;
    }

    public Point2D inverseTransform(double scrX, double scrY, Point2D dstPt) throws NoninvertibleTransformException {
        modelImageLock.lock();
        try {
            forInverseTransform.setLocation(scrX, scrY);
            return viewTransform.inverseTransform(forInverseTransform, dstPt);
        } finally {
            modelImageLock.unlock();
        }

    }
    private Point2D forInverseTransform = new Point2D.Double();

    public double inverseTransLength(double length) {
        modelImageLock.lock();
        try {
            return length / viewTransform.getScaleX();
        } finally {
            modelImageLock.unlock();
        }

    }

    public void setMargin(double top, double down, double left, double right) {
        modelImageLock.lock();
        try {
            topMargin = top;
            downMargin = down;
            leftMargin = left;
            rightMargin = right;
        } finally {
            modelImageLock.unlock();
        }

    }

    @Override
    public void paintPanel(Graphics2D g2) {
        g2.setComposite(AlphaComposite.SrcAtop);


        modelImageLock.lock();
        try {
            g2.drawImage(modelImage, null, panel);

        } finally {
            modelImageLock.unlock();
        }
        selectLock.lock();
        try {
            g2.drawImage(rubberImage, null, panel);

        } finally {
            selectLock.unlock();
        }

    }

    class ModelImageRepaintTask implements Runnable {

        ModelImageWriter writer;
        private boolean stopped;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override
        public void run() {
            modelImageLock.lock();
            try {
                while (!stopped) {
                    try {
                        modelImageCondition.await();
                        writer.writeModelBuffer(modelImage, ModelPanelManager.this);
                        panel.repaint(x, y, width, height);
//                        panel.repaint();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
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
            modelImageLock.lock();
            try {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                modelImageCondition.signalAll();
            } finally {
                modelImageLock.unlock();
            }
        }

        public ModelImageRepaintTask() {
            thread = new Thread(this);
            thread.start();
        }

        public void setModelImageWriter(ModelImageWriter writer) {
            modelImageLock.lock();
            try {
                this.writer = writer;
            } finally {
                modelImageLock.unlock();
            }
        }
    }
    ReentrantLock modelImageLock = new ReentrantLock();
    Condition modelImageCondition = modelImageLock.newCondition();
    ModelImageRepaintTask modelImageRepaintTask = new ModelImageRepaintTask();

    public void setModelImageWriter(ModelImageWriter writer) {
        modelImageRepaintTask.setModelImageWriter(writer);
    }

    public void repaintModel() {
        modelImageRepaintTask.repaintModel();
    }

    public void repaintModel(int x, int y, int width, int height) {
        modelImageRepaintTask.repaintModel(x, y, width, height);
    }

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
    }

    public ModelPanelManager(JPanel panel, double x1, double y1, double x2, double y2, ModelImageWriter modelWriter) {
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
        setModelImageWriter(modelWriter);
    }

    public void viewMove(double dx, double dy) {
        modelImageLock.lock();
        try {
            AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);//);
            viewTransform.preConcatenate(tx);
//            repaintModel();
        } finally {
            modelImageLock.unlock();
        }
    }

    public void viewScale(double centerX, double centerY, double t) {
        modelImageLock.lock();
        try {
            AffineTransform tx = AffineTransform.getTranslateInstance(centerX, centerY);
            tx.scale(t, t);
            tx.translate(-centerX, -centerY);
            viewTransform.preConcatenate(tx);
//            repaintModel();
        } finally {
            modelImageLock.unlock();
        }
    }

    public void viewZoom(double zx1, double zy1, double zx2, double zy2) {
        modelImageLock.lock();
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
            modelImageLock.unlock();
        }
    }

    public void viewWhole() {
        modelImageLock.lock();
        try {
            viewZoom(minX, minY, maxX, maxY);
        } finally {
            modelImageLock.unlock();
        }
    }

    public Shape viewMarker(double x, double y, double size, ViewMarkerType type) {
        modelImageLock.lock();
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
            modelImageLock.unlock();
        }

    }

    public Shape viewMarker(Collection<? extends Point> points, double size, ViewMarkerType type) {
        modelImageLock.lock();
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
            modelImageLock.unlock();
        }


    }
    private Point2D modelPtMarker = new Point2D.Double(),  modelPt2Marker = new Point2D.Double(),  screenPtMarker = new Point2D.Double();
    private Rectangle2D rectMarker = new Rectangle2D.Double();
    private Path2D pathMarker = new Path2D.Double();
    private Ellipse2D ellMarker = new Ellipse2D.Double();

    public AffineTransform getViewTransform() {
        return viewTransform;
    }

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
