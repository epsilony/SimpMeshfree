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
    public static final int DOMAIN_SLECECT_MIN_TIME_SCAPE = 200000000;
    //modelBounds
    double minX, maxX, minY, maxY;
    //JPanel width and height setting and havent' been rewrite the model buffer;
    boolean panelResized;

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
    ReentrantLock domainSelectLock = new ReentrantLock();
    Condition domainSelectCondition = domainSelectLock.newCondition();
    int sx1, sy1, sx2, sy2;
    BufferedImage rubberImage;
    BufferedImage modelImage;
    Graphics2D rubberG2 = rubberImage.createGraphics();
    Graphics2D modelG2 = modelImage.createGraphics();

    @Override
    public void ancestorMoved(HierarchyEvent e) {
    }

    @Override
    public void ancestorResized(HierarchyEvent e) {
        modelImageLock.lock();
        try {
            if (modelImage.getWidth() < panel.getWidth() || modelImage.getHeight() < panel.getHeight() || modelImage.getWidth() > 1.5 * panel.getWidth() || modelImage.getHeight() > 1.5 * panel.getHeight()) {
                BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
                WritableRaster wr = bi.getRaster();
                wr.setRect(modelImage.getRaster());
                modelImage = bi;
                bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
                wr = bi.getRaster();
                wr.setRect(rubberImage.getRaster());
                rubberImage = bi;
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

    public static class ZoomDomainSelectListener implements DomainSelectListener {

        Color rubberColor = Color.BLUE;
        Rectangle2D rubberRect = new Rectangle2D.Double();

        @Override
        public void selecting(int x1, int y1, int x2, int y2, ModelPanelManager vt, BufferedImage rubberImage) {
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setColor(rubberColor);
            g2.drawRect(x1, y1, x2, y2);
        }
        private Point2D pt1 = new Point2D.Double(),  pt2 = new Point2D.Double();

        public ZoomDomainSelectListener(Color rubberColor) {
            this.rubberColor = rubberColor;
        }

        public ZoomDomainSelectListener() {
        }

        @Override
        public void selected(int x1, int y1, int x2, int y2, ModelPanelManager vt) {
            try {
                vt.inverseTransform(x1, y1, pt1);
                vt.inverseTransform(x2, y2, pt2);
                vt.viewZoom(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }
    ZoomDomainSelectListener zoomDomainSelectListener = new ZoomDomainSelectListener();

    enum DomainSelectStatus {

        None, Started, Selecting, Selected;
    }
    DomainSelectStatus domainSelectStatus = DomainSelectStatus.None;

    private DomainSelectStatus getDomainSelectStatus() {
        domainSelectLock.lock();
        try {
            return domainSelectStatus;
        } finally {
            domainSelectLock.unlock();
        }
    }

    private void setDomainSelectStatus(DomainSelectStatus domainSelectStatus) {
        domainSelectLock.lock();
        try {
            this.domainSelectStatus = domainSelectStatus;
        } finally {
            domainSelectLock.unlock();
        }

    }

    private class DomainSelectTask implements Runnable {

        DomainSelectListener listener;
        int formerSx1 = 0, formerSy1 = 0, formerSx2 = rubberImage.getWidth(), formerSy2 = rubberImage.getHeight();
        boolean stopped = false;

        public void setListener(DomainSelectListener listener) {
            domainSelectLock.lock();
            try {
                this.listener = listener;
            } finally {
                domainSelectLock.unlock();
            }
        }

        @Override
        public void run() {
            domainSelectLock.lock();
            try {
                while (!stopped) {
                    try {
                        switch (getDomainSelectStatus()) {
                            case None:
                            case Started:

                                domainSelectCondition.await();
                                continue;

                            case Selecting:
                                rubberImage.createGraphics().clearRect(formerSx1, formerSy1, formerSx2, formerSy2);
                                listener.selecting(sx1, sy1, sx2, sy2, ModelPanelManager.this, rubberImage);
                                int tx1 = min(min(sx1, sx2), formerSx1);
                                int ty1 = min(min(sy1, sy2), formerSy1);
                                int tx2 = max(max(sx1, sx2), formerSx2);
                                int ty2 = max(max(sy1, sy2), formerSy2);
                                panel.repaint(tx1, ty1, tx2, ty2);
                                formerSx1 = min(sx1, sx2);
                                formerSy1 = min(sy1, sy2);
                                formerSx2 = max(sx1, sx2);
                                formerSy2 = max(sy1, sy2);
                                domainSelectCondition.await();
                                continue;

                            case Selected:
                                rubberImage.createGraphics().clearRect(formerSx1, formerSy1, formerSx2, formerSy2);
                                listener.selected(sx1, sy1, sx2, sy2, ModelPanelManager.this);
                                setDomainSelectStatus(DomainSelectStatus.None);
                                continue;
                            default:
                                domainSelectCondition.await();
                                continue;

                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                        rubberImage.createGraphics().clearRect(formerSx1, formerSy1, formerSx2, formerSy2);
                        continue;
                    }
                }

            } finally {
                domainSelectLock.unlock();
            }
        }
        Thread thread;

        public DomainSelectTask() {
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            domainSelectLock.lock();
            try {
                stopped = true;
                domainSelectCondition.signalAll();
            } finally {
                domainSelectLock.unlock();
            }
        }

        private void cancel() {
            thread.interrupt();
        }
    }
    private DomainSelectTask domainSelectTask = new DomainSelectTask();
    private long lastDomainSelectNanoTime = System.nanoTime();

    public void domainSelect(DomainSelectListener listener) {
        long nt = System.nanoTime();
        if (nt - lastDomainSelectNanoTime > DOMAIN_SLECECT_MIN_TIME_SCAPE) {
            domainSelectTask.cancel();
            domainSelectTask.setListener(listener);
            lastDomainSelectNanoTime = System.nanoTime();
        }
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
                panel.repaint();
                return;

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        domainSelectLock.lock();
        try {
            if (getDomainSelectStatus() == DomainSelectStatus.Selecting);
            sx2 = e.getX();
            sy2 = e.getY();
            domainSelectCondition.signalAll();
            return;
        } finally {
            domainSelectLock.unlock();
        }
    }
    int viewFirstX, viewFirstY;

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:

                switch (e.getClickCount()) {
                    case 2:
                        setDomainSelectStatus(DomainSelectStatus.None);
                        viewWhole();                  
                        return;
                }
                break;
            case MouseEvent.BUTTON1:
                domainSelectLock.lock();
                try {
                    switch (getDomainSelectStatus()) {
                        case Started:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            setDomainSelectStatus(DomainSelectStatus.Selecting);
                            domainSelectCondition.signalAll();
                            break;
                        case Selecting:
                            sx2 = e.getX();
                            sy2 = e.getY();
                            setDomainSelectStatus(DomainSelectStatus.Selected);
                            domainSelectCondition.signalAll();
                            break;
                    }
                } finally {
                    domainSelectLock.unlock();
                }
        }

    }

    public void selectViewZoom() {
        domainSelect(new ZoomDomainSelectListener());
    }

    public void selectViewZoom(Color rubberColor) {
        domainSelect(new ZoomDomainSelectListener(rubberColor));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                setDomainSelectStatus(DomainSelectStatus.None);
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
                    viewMoveOperation = ViewMoveOperationStatus.None;
                    panel.repaint();
                    return;
                }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        domainSelectLock.lock();
        try {
            switch (domainSelectStatus) {
                case Selecting:
                    sx2 = e.getX();
                    sy2 = e.getY();
                    domainSelectCondition.signalAll();
                    break;
            }
        } finally {
            domainSelectLock.unlock();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        domainSelectLock.lock();
        try {
            switch (domainSelectStatus) {
                case Selecting:
                    int tx1 = min(sx1, sx2);
                    int tx2 = max(sx1, sx2);
                    int ty1 = min(sy1, sy2);
                    int ty2 = max(sy1, sy2);
                    rubberImage.createGraphics().clearRect(tx1, ty1, tx2 - tx1, ty2 - ty1);
                    panel.repaint(tx1, ty1, tx2 - tx1, ty2 - ty1);
                    break;
            }
        } finally {
            domainSelectLock.unlock();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double r = Math.pow(1 + wheelRatio, e.getWheelRotation());
        setDomainSelectStatus(DomainSelectStatus.None);
        viewScale(e.getX(), e.getY(), r);
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
        g2.drawImage(modelImage, null, panel);
        g2.drawImage(rubberImage, null, panel);
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
                        writer.write(modelImage, viewTransform);
                        panel.repaint(x, y, width, height);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                }
            } finally {
                modelImageLock.unlock();
            }
        }

        private boolean isStopped() {
            modelImageLock.lock();
            try {
                return stopped;
            } finally {
                modelImageLock.unlock();
            }
        }
        Thread t;

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
            modelImageLock.lock();
            try {
                repaintModel(0, 0, panel.getHeight(), panel.getWidth());
            } finally {
                modelImageLock.unlock();
            }
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
            t = new Thread(this);
            t.start();
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

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
    }

    public ModelPanelManager(JPanel panel, double x1, double y1, double x2, double y2, ModelImageWriter modelWritet) {
        this.panel = panel;
        modelImage = new BufferedImage((int) panel.getPreferredSize().getWidth(), (int) panel.getPreferredSize().getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
        rubberImage = new BufferedImage((int) panel.getPreferredSize().getWidth(), (int) panel.getPreferredSize().getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);
        panel.addMouseListener(this);
        panel.addMouseWheelListener(this);
        panel.addMouseMotionListener(this);
        panel.addHierarchyBoundsListener(this);
        setModelBound(x1, y1, x2, y2);
        setModelImageWriter(modelWritet);
    }

    public void viewMove(double dx, double dy) {
        modelImageLock.lock();
        try {
            AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);//);
            viewTransform.preConcatenate(tx);
            repaintModel();
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
            repaintModel();
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
            repaintModel();
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

    public Shape viewMarker(Point p, double size, ViewMarkerType type) {
        return viewMarker(p.getX(), p.getY(), size, type);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        modelImageRepaintTask.stop();
        domainSelectTask.stop();
    }

    public static void main(String[] args) throws InterruptedException {
        int a = 0;
        final ReentrantLock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    try {
                        condition.await(200, TimeUnit.MICROSECONDS);
                        System.out.println("signled");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    lock.lock();
                    try {
                        System.out.println("getLock Inside");
                    } finally {
                        lock.unlock();
                    }
                } finally {
                    lock.unlock();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                lock.lock();
                try {
                    try {
                        System.out.println("before sleep");
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("after sleep");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ModelPanelManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    lock.unlock();
                }


            }
        });

        t1.start();
        t2.start();
    }
}
