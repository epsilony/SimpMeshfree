/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.processor2D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.epsilony.math.analysis.GaussLegendreQuadrature;
import net.epsilony.math.radialbasis.RadialBasisFunction;
import net.epsilony.simpmeshfree.model2D.ApproximatePoint;
import net.epsilony.simpmeshfree.model2D.BoundaryCondition;
import net.epsilony.simpmeshfree.model2D.BoundaryCondition.BoundaryConditionType;
import net.epsilony.simpmeshfree.model2D.BoundaryNode;
import net.epsilony.simpmeshfree.model2D.Model;
import net.epsilony.simpmeshfree.model2D.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.model2D.Node;
import net.epsilony.simpmeshfree.model2D.Route;
import net.epsilony.simpmeshfree.model2D.Segment;
import net.epsilony.simpmeshfree.model2D.TriangleJni;
import net.epsilony.simpmeshfree.shapefun.ShapeFunction;
import net.epsilony.simpmeshfree.utils.ModelImagePainter;
import net.epsilony.simpmeshfree.utils.ModelPanelManager;
import net.epsilony.simpmeshfree.utils.ModelPanelManager.ViewMarkerType;
import net.epsilony.util.FlexCompRowMatrixSerializable;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.client.JPPFResultCollector;
import org.jppf.client.event.TaskResultEvent;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

/**
 *
 * @author epsilon
 */
public class WeakMethodProcessor implements ModelImagePainter, Serializable {

    WeakMethodCore modelCore;

    public WeakMethodProcessor(WeakMethodCore modelCore, Model gm) {
        this.modelCore = modelCore;
        this.gm = gm;
    }

    public void setTaskDivision(int taskDivision) {
        this.taskDivision = taskDivision;
    }
    private int taskDivision = 10;

    @Override
    public void paintModel(BufferedImage modelImage, ModelPanelManager manager) {
        Graphics2D g2 = modelImage.createGraphics();


        g2.setComposite(AlphaComposite.Clear);
        //g2.fillRect(0, 0, modelImage.getWidth(), modelImage.getHeight());

        AffineTransform tx = manager.getViewTransform();
        g2.setComposite(AlphaComposite.Src);
        Path2D path = new Path2D.Double();

        if (showNodes) {
            path.append(manager.viewMarker(nodes, nodesScreenSize, nodesScreenType), false);
            path.append(manager.viewMarker(boundaryNodes, boundaryNodesScreenSize * 2, boundaryNodesScreenType), false);
            g2.setColor(nodesColor);
            g2.draw(path.createTransformedShape(null));
        }

        path.reset();

        if (showTriangleDomain) {
//            System.out.println("length of Triangle" + triangleQuadratureDomains.size());
            g2.setColor(triangleDomainColor);
            for (double[] tri : triangleQuadratureDomains) {
                path.moveTo(tri[0], tri[1]);
                path.lineTo(tri[2], tri[3]);
                path.lineTo(tri[4], tri[5]);
                path.lineTo(tri[0], tri[1]);
                path.closePath();
            }

            g2.draw(path.createTransformedShape(tx));


        }
    }
    transient static Logger log = Logger.getLogger(WeakMethodProcessor.class);
    transient DenseVector bVector;
    LinkedList<BoundaryNode> boundaryNodes = new LinkedList<BoundaryNode>();
    double boundaryNodesScreenSize = 3;
    transient ViewMarkerType boundaryNodesScreenType = ViewMarkerType.X;
    Model gm;
    transient FlexCompRowMatrix kMat = null;
    transient UpperSymmBandMatrix matA;
    LinkedList<Node> nodes = new LinkedList<Node>();
    transient Color nodesColor = Color.RED;
    transient double nodesScreenSize = 4;
    transient ViewMarkerType nodesScreenType = ViewMarkerType.Round;
    int quadratureNum;
    RadialBasisFunction radialBasisFunction;
    ShapeFunction shapeFunction;
    transient boolean showNodes = true;
    transient boolean showTriangleDomain = true;
    SupportDomain supportDomain;
    transient TriangleJni triJni;
    transient Color triangleDomainColor = Color.lightGray;
    LinkedList<double[]> triangleQuadratureDomains = new LinkedList<double[]>();
    LinkedList<double[]> rectangleQuadratureDomains = new LinkedList<double[]>();

    public LinkedList<double[]> getRectangleQuadratureDomains() {
        return rectangleQuadratureDomains;
    }
    transient DenseVector xVector = new DenseVector(nodes.size() * 2);
//    ReentrantLock quadrateDomainsLock = new ReentrantLock();
//    int submitEnd;
    transient boolean forceSingleProcessor = false;
    transient boolean forceLocalProcessor = false;

    public void setForceSingleProcessor(boolean forceSingleProcesser) {
        this.forceSingleProcessor = forceSingleProcesser;
    }
    int kMatRow;
    int kMatCol;

    public void quadrateDomains() throws ArgumentOutsideDomainException, Exception {
        int sumProcess = Runtime.getRuntime().availableProcessors();
        int sumDomains = rectangleQuadratureDomains.size() + triangleQuadratureDomains.size();
        if (forceLocalProcessor || forceSingleProcessor) {
            initialKMatrix();
            log.info("Start quadrateDomains with multi threads");
            long time = System.nanoTime();
            taskDivision = sumProcess * 10;
            ExecutorService es;
            if (forceSingleProcessor) {
                es = Executors.newFixedThreadPool(1);
            } else {
                es = Executors.newFixedThreadPool(sumProcess);
            }

            int gap = (sumDomains) / taskDivision;
            if (gap < 10) {
                gap = 10;
            }
            LinkedList<QuadrateDomainsTask> tasks = new LinkedList<QuadrateDomainsTask>();
            int start = 0;
            int end = start + gap;
            DataProvider dataProvider = new MemoryMapDataProvider();
            dataProvider.setValue("model", this);

            while (start < sumDomains) {
                QuadrateDomainsTask task = new QuadrateDomainsTask(start, end, this);
                tasks.add(task);
                es.submit(task);

                start = end;
                end += gap;
                if (end > sumDomains) {
                    end = sumDomains;
                }
            }
            es.shutdown();
            boolean allDone = false;

            while (!allDone) {
                int sumQuadrated = 0;
                for (QuadrateDomainsTask task : tasks) {
                    if (task.finished.get()) {
                        sumQuadrated += -task.start + task.end;
                    }
                }
                log.info(String.format("Quadrating %d/%d %%%.0f", sumQuadrated, sumDomains, sumQuadrated / (double) sumDomains * 100));
                try {
                    allDone = es.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    log.error(ex);
                }
            }
            log.info("All tasks' done! Assembling");

            for (QuadrateDomainsTask task : tasks) {
                kMat.add(task.taskMatrix);
            }
            time = System.nanoTime() - time;
            log.info("Multi Thread quadrateDomains finished, time costs:" + time);
            return;
        } else {
            initialKMatrix();
            log.info("Start quadrateDomains with net grid!");
            long time = System.nanoTime();
            taskDivision = 5;

            int gap = sumDomains / taskDivision + 1;
            if (gap < 10) {
                gap = 10;
            }
            JPPFJob job = new JPPFJob();
            int start = 0;
            int end = start + gap;
            if (end > sumDomains) {
                end = sumDomains;
            }
            DataProvider dataProvider = new MemoryMapDataProvider();
            dataProvider.setValue("model", this);
            while (start < sumDomains) {
                QuadrateDomainsJPPFTask task = new QuadrateDomainsJPPFTask(start, end);
                job.addTask(task);
                start = end;
                end += gap;
                if (end > sumDomains) {
                    end = sumDomains;
                }
            }


            DomainQuadratureResultListerner domainQuadratureResultListerner = new DomainQuadratureResultListerner(job.getTasks().size(), kMat);
            job.setDataProvider(dataProvider);
            job.setResultListener(domainQuadratureResultListerner);
            JPPFClient client = new JPPFClient();
            client.submit(job);
            domainQuadratureResultListerner.waitForResults();
            time = System.nanoTime() - time;
            log.info("Grid net quadrateDomains finished, time costs:" + time);
        }
    }

//    DomainQuadratureResultListerner domainQuadratureResultListerner;
    static class DomainQuadratureResultListerner extends JPPFResultCollector {

        transient private FlexCompRowMatrix matrix;

        public DomainQuadratureResultListerner(int count, FlexCompRowMatrix matrix) {
            super(count);
            this.count = count;
            this.matrix = matrix;
        }
        int count;
        int sum = 0;

        @Override
        public void resultsReceived(TaskResultEvent event) {
            super.resultsReceived(event);
            sum += event.getResults().size();
            log.info(String.format("Recieved results: %d/%d %%%.0f", sum, count, sum / (double) count * 100));
            List<JPPFTask> results = event.getTaskList();
            Exception ex;
            for (JPPFTask result : results) {
                ex = result.getException();
                if (ex != null) {
                    log.error(ex);
                }
                ((FlexCompRowMatrixSerializable) result.getResult()).addTo(matrix);
            }
        }
    }

    public void setForceLocalProcessor(boolean forceLocalProcessor) {
        this.forceLocalProcessor = forceLocalProcessor;
    }

    static class QuadrateDomainsJPPFTask extends JPPFTask {

        QuadrateDomainsTask task;

        @Override
        public void run() {
            try {
//            Thread.setDefaultUncaughtExceptionHandler(eh);
                DataProvider dp = getDataProvider();
                try {
                    task.model = (WeakMethodProcessor) dp.getValue("model");
                } catch (Exception ex) {
                    if (null == log) {
                        log = Logger.getLogger(WeakMethodProcessor.class);
                    }
                    log.error(ex);
                    this.setException(ex);
                    return;
                }
                task.run();
                setResult(new FlexCompRowMatrixSerializable(task.taskMatrix));
            } catch (Throwable ex) {
                setException(new Exception(ex));
            }
        }

        QuadrateDomainsJPPFTask(int start, int end) {
            task = new QuadrateDomainsTask(start, end);
        }
    }

    static class QuadrateDomainsTask implements Runnable, Serializable {

        int start;
        int end;
        AtomicBoolean finished = new AtomicBoolean();
        transient FlexCompRowMatrix taskMatrix;

        QuadrateDomainsTask(int start, int end, WeakMethodProcessor model) {
            this.start = start;
            this.end = end;
            this.model = model;

        }

        QuadrateDomainsTask(int start, int end) {
            this.start = start;
            this.end = end;
        }
        WeakMethodProcessor model;

        @Override
        public void run() {
            if (null == log) {
                Logger.getLogger(WeakMethodProcessor.class);
            }
            taskMatrix = new FlexCompRowMatrix(model.kMatRow, model.kMatCol);

            if (start < model.rectangleQuadratureDomains.size()) {
                try {
                    WeakMethodProcessor.quadrateRectangleDomains(start, end, taskMatrix, model);
                } catch (ArgumentOutsideDomainException ex) {

                    log.error(ex);
                }
            }
            if (end > model.rectangleQuadratureDomains.size() && !model.triangleQuadratureDomains.isEmpty()) {
                int triStart;
                if (start < model.rectangleQuadratureDomains.size()) {
                    triStart = 0;
                } else {
                    triStart = start - model.rectangleQuadratureDomains.size();
                }
                int triEnd = end - model.rectangleQuadratureDomains.size();

                try {
                    WeakMethodProcessor.quadrateTriangleDomains(triStart, triEnd, taskMatrix, model);
                } catch (ArgumentOutsideDomainException ex) {
                    log.error(ex);
                }

                finished.set(true);
            }
        }
    }

    public void generateBoundaryNodeByApproximatePoints(double size, double flatness) {
        gm.generateApproximatePoints(size, flatness);
        for (Route route : gm.getRoutes()) {
            for (ApproximatePoint ap : route.GetApproximatePoints()) {
                BoundaryNode bn = new BoundaryNode(ap);
                nodes.add(bn);
                boundaryNodes.add(bn);
            }

        }
    }

    public void generateNodesByTriangle(double size, double flatness, String s, boolean needNeighbors, boolean resetNodesIndex) {
        log.info(String.format("Start generateNodesByTiangle%nsize=%6.3e flatness=%6.3e s=%s needNeighbors=%b resetNodesIndex %b", size, flatness, s, needNeighbors, resetNodesIndex));
        if (resetNodesIndex) {
            Node n = Node.tempNode(0, 0);
            n.getIndexManager().reset();
        }

        nodes.clear();
        boundaryNodes.clear();
        TriangleJni triangleJni = new TriangleJni();
        gm.generateApproximatePoints(size, flatness);
        triangleJni.complie(gm, s);
        nodes.addAll(triangleJni.getNodes(needNeighbors));
        for (Node n : nodes) {
            if (n.type() == ModelElementType.BoundaryNode) {
                boundaryNodes.add((BoundaryNode) n);
            }

        }
        triJni = triangleJni;
        log.info(String.format("End of generateNodesByTriangle%n nodes.size()=%d boundaryNodes.size()=%d", nodes.size(), boundaryNodes.size()));
    }

    public void generateQuadratureDomainsByTriangle() {
        log.info("Start generateQuadratureDomainsByTriangle()");
        triangleQuadratureDomains = triJni.getTriangleXYsList();
        log.info("End of generateQuadratureDomainsByTriangle()");
    }

    public void generateQuadratureDomainsByTriangle(double size, double flatness, String s) {
        log.info(String.format("Start generateQuadratureDomainsByTriangle(%6.3e, %6.3e, %s", size, flatness, s));
        TriangleJni triangleJni = new TriangleJni();
        gm.generateApproximatePoints(size, flatness);
        triangleJni.complie(gm, s);
        triangleQuadratureDomains =
                triangleJni.getTriangleXYsList();
        log.info("End of generateQuadratureDomainsByTriangle()");
    }

    public LinkedList<BoundaryNode> getBoundaryNodes() {
        return boundaryNodes;
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public int getQuadratureNum() {
        return quadratureNum;
    }

    public SupportDomain getSupportDomain() {
        return supportDomain;
    }

    public void initialKMatrix() {
        kMat = modelCore.initialKMatrix(nodes.size());
        kMatCol = kMat.numColumns();
        kMatRow = kMat.numRows();
    }

    public void quadrateRectangleDomains() throws ArgumentOutsideDomainException {
        quadrateRectangleDomains(0, rectangleQuadratureDomains.size(), kMat, this);
    }

    public static void quadrateRectangleDomains(int start, int end, FlexCompRowMatrix taskMatrix, WeakMethodProcessor model) throws ArgumentOutsideDomainException {
//        System.out.println(Thread.currentThread());
//        log.info(String.format("Start quadrateRectangleDomains(%d)", quadratureNum));
        if (null == log) {
            log = Logger.getLogger(WeakMethodProcessor.class);
        }
        //for synchronized computing
        SupportDomain localSupportDomain = model.supportDomain.CopyOf(false);
        ShapeFunction localShapeFunction = model.shapeFunction.CopyOf(false);
        RadialBasisFunction localRadialBasisFunction = model.radialBasisFunction.CopyOf(false);
        localShapeFunction.setRadialBasisFunction(localRadialBasisFunction);
        //end for synchronized computing

        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;

        double[] weights = null;
        double[] points = null;
        weights = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(model.quadratureNum);
        points = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(model.quadratureNum);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }

        ListIterator<double[]> iterator = model.rectangleQuadratureDomains.listIterator(start);
        int counter = start;
        while (iterator.hasNext() && counter < end) {
            double[] rects = iterator.next();
            counter++;
            double x1 = rects[0];
            double y1 = rects[1];
            double x2 = rects[2];
            double y2 = rects[3];
            double u;
            double v;
            double x;
            double y;
            double J = 0.25 * Math.abs((x2 - x1) * (y2 - y1));
            double w;
            for (int k = 0; k < weights.length; k++) {
                for (int l = 0; l < weights.length; l++) {
                    u = (points[k] + 1) / 2;
                    v = (points[l] + 1) / 2;
                    x = x1 + (x2 - x1) * u;
                    y = y1 + (y2 - y1) * v;
                    w = weights[k] * weights[l] * J;
                    nodesAverDistance = localSupportDomain.supportNodes(x, y, supportNodes);
                    localRadialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                    partialValues = localShapeFunction.shapePartialValues(supportNodes, x, y);
                    for (int m = 0; m < supportNodes.size(); m++) {
                        double mx = partialValues[0].get(m);
                        double my = partialValues[1].get(m);
                        for (int n = 0; n < supportNodes.size(); n++) {
                            int nIndex = supportNodes.get(n).getMatrixIndex();
                            int mIndex = supportNodes.get(m).getMatrixIndex();
                            if (mIndex > nIndex) {
                                continue;
                            }

                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);
                            model.modelCore.quadrateCore(mIndex, mx, my, nIndex, nx, ny, w, taskMatrix);
                        }

                    }
                }
            }
        }
//        log.info("End of quadrateRectangleDomains");
    }

    public void quadrateTriangleDomains() throws ArgumentOutsideDomainException {
        quadrateTriangleDomains(0, triangleQuadratureDomains.size(), kMat, this);
    }

    public static void quadrateTriangleDomains(int start, int end, FlexCompRowMatrix matrix, WeakMethodProcessor model) throws ArgumentOutsideDomainException {
//        log.info(String.format("Start quadrateTriangleDomainsByGrid(%d)", quadratureNum));
        if (null == log) {
            log = Logger.getLogger(WeakMethodProcessor.class);
        }
        SupportDomain localSupportDomain = model.supportDomain.CopyOf(false);
        ShapeFunction localShapeFunction = model.shapeFunction.CopyOf(false);
        RadialBasisFunction localRadialBasisFunction = model.radialBasisFunction.CopyOf(false);
        localShapeFunction.setRadialBasisFunction(localRadialBasisFunction);
        double nodesAverDistance;
        ArrayList<Node> supportNodes = new ArrayList<Node>(100);
        Vector[] partialValues;

        double[] weights = null;
        double[] points = null;
        weights = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(model.quadratureNum);
        points = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(model.quadratureNum);
        if (log.isDebugEnabled()) {
            log.debug("weights:" + Arrays.toString(weights));
            log.debug("area Coordinates: " + Arrays.toString(points));
        }

        ListIterator<double[]> iterator = model.triangleQuadratureDomains.listIterator(start);
        int counter = start;
        while (iterator.hasNext() && counter < end) {
            double[] triangleDomain = iterator.next();
            counter++;

            double x1 = triangleDomain[0];
            double y1 = triangleDomain[1];
            double x2 = triangleDomain[2];
            double y2 = triangleDomain[3];
            double x3 = triangleDomain[4];
            double y3 = triangleDomain[5];
            double u;
            double v;
            double x;
            double y;
            double J;
            double w;
            for (int k = 0; k < weights.length; k++) {
                for (int l = 0; l < weights.length; l++) {
                    u = (points[k] + 1) / 2;
                    v = (points[l] + 1) / 2;
                    x = x1 + u * x2 - u * x1 + u * x3 * v - u * x2 * v;
                    y = y1 + u * y2 - u * y1 + v * u * y3 - v * u * y2;
                    J = 0.25 * Math.abs(u * (x2 * y3 - x1 * y3 + x1 * y2 - x3 * y2 + x3 * y1 - x2 * y1));
                    w = weights[k] * weights[l] * J;
                    nodesAverDistance = localSupportDomain.supportNodes(x, y, supportNodes);
                    localRadialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                    partialValues = localShapeFunction.shapePartialValues(supportNodes, x, y);
                    for (int m = 0; m < supportNodes.size(); m++) {
                        double mx = partialValues[0].get(m);
                        double my = partialValues[1].get(m);
                        for (int n = 0; n < supportNodes.size(); n++) {
                            int nIndex = supportNodes.get(n).getMatrixIndex();
                            int mIndex = supportNodes.get(m).getMatrixIndex();
                            if (mIndex > nIndex) {
                                continue;
                            }

                            double nx = partialValues[0].get(n);
                            double ny = partialValues[1].get(n);
                            model.modelCore.quadrateCore(mIndex, mx, my, nIndex, nx, ny, w, matrix);
                        }

                    }
                }
            }
        }
//        log.info("End of quadrateTriangleDomainsByGrid");
    }

    public void applyEssentialBoundaryConditions() {
        log.info("Start applyAccurateEssentialBoundaryConditions");
        Segment segment;

        double segmentParm;

        LinkedList<BoundaryCondition> tempBCs;
        int rowcol;
        nodes.size();
        double[] txy = new double[2];
        byte tb;
        for (BoundaryNode bNode : boundaryNodes) {
            segment = bNode.getSegment();
            segmentParm = bNode.getSegmentParm();
            if (0 == segmentParm) {
                LinkedList<BoundaryCondition> bc1, bc2;
                bc1 = segment.getBoundaryConditions();
                bc2 = segment.getBack().getBoundaryConditions();
                if (bc1 != null || bc2 != null) {
                    tempBCs = new LinkedList<BoundaryCondition>();
                    if (bc1 != null) {
                        tempBCs.addAll(segment.getBoundaryConditions());
                    }

                    if (bc2 != null) {
                        tempBCs.addAll(segment.getBack().getBoundaryConditions());
                    }

                } else {
                    tempBCs = null;
                }

            } else {
                tempBCs = segment.getBoundaryConditions();
            }

            if (null == tempBCs) {
                continue;
            }

            for (BoundaryCondition bc : tempBCs) {
                if (bc.getType() != BoundaryConditionType.Essential) {
                    continue;
                }

                tb = bc.getValues(bNode.getSegmentParm(), txy);
                if (BoundaryCondition.NOT == tb) {
                    continue;
                }

                rowcol = bNode.getMatrixIndex();
                modelCore.essentialBoundaryConditionCore(txy, rowcol, tb, kMat, bVector);
            }

        }
        log.info("End of applyAccurateEssentialBoundaryConditions");
    }

//    abstract public void accurateEssentialCore(double[] values, int index, byte flag);
    public void setQuadratureNum(int quadN) {
        this.quadratureNum = quadN;
    }

    public void setRadialBasisFunction(RadialBasisFunction radialBasisFunction) {
        this.radialBasisFunction = radialBasisFunction;
    }

    public void setShapeFunction(ShapeFunction shapeFunction) {
        this.shapeFunction = shapeFunction;
    }

    public void setSupportDomain(SupportDomain supportDomain) {
        this.supportDomain = supportDomain;
    }

    public void natureBoundaryQuadrate(int n) throws ArgumentOutsideDomainException {
        log.info(String.format("Start natureBoundaryQuadrate(%d", n));
        LinkedList<Route> routes = gm.getRoutes();
        LinkedList<ApproximatePoint> aprxPts;
        ArrayList<Node> supportNodes = new ArrayList<Node>();
        ArrayList<BoundaryCondition> naturalBCs = new ArrayList<BoundaryCondition>(10);
        LinkedList<BoundaryCondition> tempBCs = null;
        LinkedList<BoundaryCondition> conNaturalBCs = new LinkedList<BoundaryCondition>();
        LinkedList<double[]> conNaturalValues = new LinkedList<double[]>();
        ApproximatePoint aprxStart;

        ApproximatePoint aprx;

        ApproximatePoint aprxFront;

        Segment segment = null;
        Vector shapeVector;

        double t;
        double w;
        double t1;
        double t2;
        double x;
        double y;
        double ds;
        double parmStart;
        double parmEnd;
        BoundaryCondition tempBC;

        double[] txy = new double[2];
        double[] quadratePoints = GaussLegendreQuadrature.getGaussLegendreQuadraturePoints(n);
        double[] quadrateCoefs = GaussLegendreQuadrature.getGaussLegendreQuadratureCoefficients(n);
        int i = 0;
        int j;
        int k;
        int row;
        for (Route route : routes) {
            aprxPts = route.GetApproximatePoints();
            aprxStart = aprxPts.getFirst();
            aprx = aprxStart;
            if (log.isDebugEnabled()) {
                log.debug(route);
                log.debug("route Approximate Point size=" + aprxPts.size());
            }

            do {
                if (aprx.getSegmentParm() == 0) {
                    segment = aprx.getSegment();
                    if (log.isDebugEnabled()) {
                        log.debug(segment);
                    }

                    tempBCs = segment.getBoundaryConditions();
                    naturalBCs.clear();
                    conNaturalBCs.clear();
                    if (null != tempBCs) {
                        for (BoundaryCondition bc : tempBCs) {
                            if (bc.getType() == BoundaryConditionType.Natural) {
                                naturalBCs.add(bc);
                            } else if (bc.getType() == BoundaryConditionType.ConNatural) {
                                conNaturalBCs.add(bc);
                            }

                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("conNaturalBCs.size() =" + conNaturalBCs.size());
                        log.debug("naturalBCs.size()=" + naturalBCs.size());
                    }

                    for (BoundaryCondition bc : conNaturalBCs) {
                        bc.getConNaturalValues(conNaturalValues);
                        for (double[] values : conNaturalValues) {
                            t = values[0];

                            supportDomain.boundarySupportNodes(segment, t, supportNodes);
                            segment.parameterPoint(t, txy);
                            x = txy[0];
                            y = txy[1];
                            shapeVector = shapeFunction.shapeValues(supportNodes, x, y);
                            for (k = 0; k < shapeVector.size(); k++) {
                                row = supportNodes.get(k).getMatrixIndex();
                                modelCore.natureConBoundaryQuadrateCore(values, row, shapeVector.get(k), bVector);
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("conNaturalBCs applied");
                        }
                    }
                    if (log.isDebugEnabled() && !conNaturalBCs.isEmpty()) {
                        log.debug("conNaturalBCs applied");
                    }
                }
                if (naturalBCs.size() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("applying natural bc");
                    }
                    parmStart = aprx.getSegmentParm();
                    aprxFront = aprx.getFront();
                    if (aprxFront.getSegmentParm() == 0) {
                        parmEnd = 1;
                    } else {
                        parmEnd = aprxFront.getSegmentParm();
                    }
                    t1 = -(parmStart - parmEnd) * 0.5;
                    t2 = (parmStart + parmEnd) * 0.5;

                    for (i = 0; i < n; i++) {
                        t = t2 + t1 * quadratePoints[i];
                        w = quadrateCoefs[i] * t1;
                        segment.parameterPoint(t, txy);
                        x = txy[0];
                        y = txy[1];
                        segment.parameterDifference(t, txy);
                        ds = Math.sqrt(txy[0] * txy[0] + txy[1] * txy[1]);
                        double nodesAverDistance = supportDomain.boundarySupportNodes(segment, t, supportNodes);
                        radialBasisFunction.setNodesAverageDistance(nodesAverDistance);
                        shapeVector = shapeFunction.shapeValues(supportNodes, x, y);
                        double coef = ds * w;
                        for (j = 0; j < naturalBCs.size(); j++) {
                            tempBC = naturalBCs.get(j);
                            if (0 != tempBC.getValues(t, txy)) {
                                for (k = 0; k < supportNodes.size(); k++) {
                                    row = supportNodes.get(k).getMatrixIndex();
                                    modelCore.natureBoundaryQudarateCore(txy, row, shapeVector.get(k), coef, bVector);

                                }
                            }
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("end of applying natural bc");
                    }
                }
                aprx = aprx.getFront();
            } while (aprx != aprxStart);
        }
        log.info("End of natureBoundaryQuadrate");
    }
}
