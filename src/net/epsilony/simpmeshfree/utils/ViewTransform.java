/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.Shape;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.Math.*;
import net.epsilony.simpmeshfree.model.geometry.Point;

/**
 *
 * @author epsilon
 */
public class ViewTransform extends AffineTransform implements MouseMotionListener, MouseListener, MouseWheelListener, JPanelPainter {

    double minX, maxX, minY, maxY;
    int width, heigh;

    synchronized public int getHeigh() {
        return heigh;
    }

    synchronized public void setHeigh(int heigh) {
        this.heigh = heigh;
    }

    synchronized public int getWidth() {
        return width;
    }

    synchronized public void setWidth(int width) {
        this.width = width;
    }


    {
        topMargin = leftMargin = rightMargin = downMargin = 10;
    }
    double topMargin, leftMargin, downMargin, rightMargin;
    JPanel panel;
    int domainSearchStage;
    ReentrantLock domainSearchLock = new ReentrantLock();
    Condition domainSearchCondition = domainSearchLock.newCondition();
    int sx1, sy1, sx2, sy2;
    BufferedImage rubberImage = new BufferedImage(1280, 1024, BufferedImage.TYPE_4BYTE_ABGR_PRE);
    BufferedImage modelImage = new BufferedImage(1280, 1024, BufferedImage.TYPE_4BYTE_ABGR_PRE);
    Graphics2D rubberG2 = rubberImage.createGraphics();
    Graphics2D modelG2 = modelImage.createGraphics();

    public enum ViewMoveOperationStatus {

        None,
        MoveStarted, MoveFirstPointSet,
    }
    ViewMoveOperationStatus viewMoveOperation = ViewMoveOperationStatus.None;

    public static class ZoomDomainSearchListener implements DomainSelectListener {

        Color rubberColor = Color.BLUE;
        Rectangle2D rubberRect = new Rectangle2D.Double();

        @Override
        public void searching(int x1, int y1, int x2, int y2, ViewTransform vt, BufferedImage rubberImage) {
            Graphics2D g2 = rubberImage.createGraphics();
            g2.setColor(rubberColor);
            g2.drawRect(x1, y1, x2, y2);
        }
        private Point2D pt1 = new Point2D.Double(),  pt2 = new Point2D.Double();

        public ZoomDomainSearchListener(Color rubberColor) {
            this.rubberColor = rubberColor;
        }

        public ZoomDomainSearchListener() {
        }

        @Override
        public void searched(int x1, int y1, int x2, int y2, ViewTransform vt) {
            try {
                vt.inverseTransform(x1, y1, pt1);
                vt.inverseTransform(x2, y2, pt2);
                vt.viewZoom(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    private Future<?> getFuture() {
        futureLock.lock();
        try {
            return future;
        } finally {
            futureLock.unlock();
        }
    }
    private Future<?> future;
    private ReentrantLock futureLock = new ReentrantLock();
    private Condition futureCondition = futureLock.newCondition();

    private void setFuture(Future<?> future) {
        futureLock.lock();
        try {
            this.future = future;
        } finally {
            futureLock.unlock();
        }
    }
    //private LinkedList<DomainSelectListener> domainSearchListeners = new LinkedList<DomainSelectListener>();

    enum DomainSearchStatus {

        None, Started, Searching, Searched;
    }
    DomainSearchStatus domainSearchStatus = DomainSearchStatus.None;
    ExecutorService executorService = Executors.newCachedThreadPool();

    private DomainSearchStatus getDomainSearchStatus() {
        domainSearchLock.lock();
        try {
            return domainSearchStatus;
        } finally {
            domainSearchLock.unlock();
        }
    }

    private void setDomainSearchStatus(DomainSearchStatus domainSearchStatus) {
        domainSearchLock.lock();
        try {
            this.domainSearchStatus = domainSearchStatus;
        } finally {
            domainSearchLock.unlock();
        }

    }

    private class DomainSelectTask implements Runnable {

        Future<?> innerFuture;
        DomainSelectListener listener;
        int formerSx1 = 0, formerSy1 = 0, formerSx2 = rubberImage.getWidth(), formerSy2 = rubberImage.getHeight();

        @Override
        public void run() {
            futureLock.lock();
            try {
                while (!getFuture().isDone()) {
                    future.cancel(true);
                    try {
                        futureCondition.await(500, TimeUnit.MICROSECONDS);
                        if (!getFuture().isDone()) {
                            return;
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
                setFuture(innerFuture);
            } finally {
                futureLock.unlock();
            }

            domainSearchLock.lock();
            try {
                setDomainSearchStatus(DomainSearchStatus.Started);
                while (getDomainSearchStatus() == DomainSearchStatus.Started) {
                    try {
                        domainSearchCondition.await();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
                while (getDomainSearchStatus() == DomainSearchStatus.Searching) {
                    rubberImage.createGraphics().clearRect(formerSx1, formerSy1, formerSx2, formerSy2);
                    listener.searching(sx1, sy1, sx2, sy2, ViewTransform.this, rubberImage);
                    int tx1 = min(min(sx1, sx2), formerSx1);
                    int ty1 = min(min(sy1, sy2), formerSy1);
                    int tx2 = max(max(sx1, sx2), formerSx2);
                    int ty2 = max(max(sy1, sy2), formerSy2);
                    panel.repaint(tx1, ty1, tx2, ty2);
                    formerSx1 = min(sx1, sx2);
                    formerSy1 = min(sy1, sy2);
                    formerSx2 = max(sx1, sx2);
                    formerSy2 = max(sy1, sy2);
                    try {
                        domainSearchCondition.await();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                if (getDomainSearchStatus() == DomainSearchStatus.Searched) {
                    rubberImage.createGraphics().clearRect(formerSx1, formerSy1, formerSx2, formerSy2);
                    listener.searched(sx1, sy1, sx2, sy2, ViewTransform.this);
                }
            } finally {
                setDomainSearchStatus(DomainSearchStatus.None);
                domainSearchLock.unlock();
            }
        }

        public DomainSelectTask(DomainSelectListener listener) {
            this.listener = listener;
            innerFuture = executorService.submit(this);
        }
    }
    private long lastDomainSelectNanoTime = System.nanoTime();

    public void domainSelect(DomainSelectListener listener) {
        long nt = System.nanoTime();
        if (nt - lastDomainSelectNanoTime > 200000000) {
            new DomainSelectTask(listener);
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
        domainSearchLock.lock();
        try {
            if (getDomainSearchStatus() == DomainSearchStatus.Searching);
            sx2 = e.getX();
            sy2 = e.getY();
            domainSearchCondition.signalAll();
            return;
        } finally {
            domainSearchLock.unlock();
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
                        viewMoveOperation = ViewMoveOperationStatus.None;
                        panel.repaint();
                        return;
                }
                break;
            case MouseEvent.BUTTON1:
                domainSearchLock.lock();
                try {
                    switch (getDomainSearchStatus()) {
                        case Started:
                            sx1 = e.getX();
                            sy1 = e.getY();
                            setDomainSearchStatus(DomainSearchStatus.Searching);
                            break;
                        case Searching:
                            sx2 = e.getX();
                            sy2 = e.getY();
                            setDomainSearchStatus(DomainSearchStatus.Searched);
                            domainSearchCondition.signalAll();
                            break;
                    }
                } finally {
                    domainSearchLock.unlock();
                }
        }

    }

    public void selectViewZoom() {
        domainSelect(new ZoomDomainSearchListener());
    }

    public void selectViewZoom(Color rubberColor) {
        domainSelect(new ZoomDomainSearchListener(rubberColor));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON2:
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
        domainSearchLock.lock();
        try {
            switch (domainSearchStatus) {
                case Searching:
                    sx2 = e.getX();
                    sy2 = e.getY();
                    domainSearchCondition.signalAll();
                    break;
            }
        } finally {
            domainSearchLock.unlock();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        domainSearchLock.lock();
        try {
            switch (domainSearchStatus) {
                case Searching:
                    int tx1 = min(sx1, sx2);
                    int tx2 = max(sx1, sx2);
                    int ty1 = min(sy1, sy2);
                    int ty2 = max(sy1, sy2);
                    rubberImage.createGraphics().clearRect(tx1, ty1, tx2 - tx1, ty2 - ty1);
                    panel.repaint(tx1, ty1, tx2 - tx1, ty2 - ty1);
                    break;
            }
        } finally {
            domainSearchLock.unlock();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double r = Math.pow(1 + wheelRatio, e.getWheelRotation());
        viewScale(e.getX(), e.getY(), r);
        panel.repaint();
    }
    double wheelRatio = 0.05;

    public void setWheelRatio(double wheelRatio) {
        this.wheelRatio = wheelRatio;
    }

    synchronized public void setModelBound(double x1, double y1, double x2, double y2) {
        this.minX = x1;
        this.maxX = x2;
        this.minY = y1;
        this.maxY = y2;
    }

    public Point2D inverseTransform(double scrX, double scrY, Point2D dstPt) throws NoninvertibleTransformException {
        forInverseTransform.setLocation(scrX, scrY);
        return inverseTransform(forInverseTransform, dstPt);
    }
    private Point2D forInverseTransform = new Point2D.Double();

    public double inverseTransLength(double length) {
        return length / getScaleX();
    }

    synchronized public void setMargin(double top, double down, double left, double right) {
        topMargin = top;
        downMargin = down;
        leftMargin = left;
        rightMargin = right;
    }

    @Override
    public void paintPanel(Graphics2D g2) {
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(modelImage, null, panel);
        g2.drawImage(rubberImage, null, panel);
    }

    class ModelImageGenerateTask implements Runnable {

        ModelImageWriter writer;
        private boolean stopped;

        @Override
        public void run() {
            modelImageLock.lock();
            try{
                while(!isStop())
                try {
                    modelImageCondition.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }

            }finally{
                modelImageLock.unlock();
            }
        }

        private boolean isStop() {
            modelImageLock.lock();
            try{
                return stopped;
            }finally{
                modelImageLock.unlock();
            }
        }
        Thread t;
        public void start(){
            
        }
        public void stop(){

        }
    }
    ReentrantLock modelImageLock = new ReentrantLock();
    Condition modelImageCondition = modelImageLock.newCondition();

    public void repaintPanel() {
    }

    public enum ViewMarkerType {

        Rectangle, UpTriangle, DownTriangle, Round, X, Cross;
    }

    public ViewTransform(double x1, double y1, double x2, double y2) {
        this.minX = x1;
        this.maxX = x2;
        this.minY = y1;
        this.maxY = y2;
//        viewWhole();
    }

    public ViewTransform() {
        super();
    }

    synchronized public void viewMove(double dx, double dy) {
        AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);//);
        preConcatenate(tx);
    }

    synchronized public void viewScale(double centerX, double centerY, double t) {
        AffineTransform tx = AffineTransform.getTranslateInstance(centerX, centerY);
        tx.scale(t, t);
        tx.translate(-centerX, -centerY);
        preConcatenate(tx);
    }

    synchronized public void viewZoom(double zx1, double zy1, double zx2, double zy2) {
        this.setToIdentity();
        double dx = Math.abs(zx2 - zx1);
        double dy = Math.abs(zy2 - zy1);
        translate((panel.getWidth()) / 2, panel.getHeight() / 2);
        double t = Math.min((panel.getWidth() - leftMargin - rightMargin) / dx, (panel.getHeight() - topMargin - downMargin) / dy);
        scale(t, -t);
        translate(-(zx1 + zx2) / 2, -(zy1 + zy2) / 2);
    }

    synchronized public void viewWhole() {
        viewZoom(minX, minY, maxX, maxY);
    }

//    public AffineTransform viewTranslate() {
//        return AffineTransform.getTranslateInstance(getTranslateX(), getTranslateY());
//    }
    public Shape viewMarker(double x, double y, double size, ViewMarkerType type) {
        modelPtMarker.setLocation(x, y);
        transform(modelPtMarker, screenPtMarker);
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
    }
    private Point2D modelPtMarker = new Point2D.Double(),  modelPt2Marker = new Point2D.Double(),  screenPtMarker = new Point2D.Double();
    private Rectangle2D rectMarker = new Rectangle2D.Double();
    private Path2D pathMarker = new Path2D.Double();
    private Ellipse2D ellMarker = new Ellipse2D.Double();

    public Shape viewMarker(Point p, double size, ViewMarkerType type) {
        return viewMarker(p.getX(), p.getY(), size, type);
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
                        Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(ViewTransform.class.getName()).log(Level.SEVERE, null, ex);
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
