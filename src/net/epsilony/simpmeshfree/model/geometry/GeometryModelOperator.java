/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.geometry;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import net.epsilony.simpmeshfree.model.geometry.ModelElement.ModelElementType;
import net.epsilony.simpmeshfree.utils.JPanelPainter;
import net.epsilony.simpmeshfree.utils.ViewTransform;

/**
 *
 * @author epsilon
 */
public class GeometryModelOperator implements MouseListener, MouseMotionListener, JPanelPainter {

    GeometryModel gm;
    ModelElement.ModelElementType selectType;
    ViewTransform vt;
    int singleSelectSize = 5;
    int selectingMarkSize = 10;

    public Shape selectedShape() {
        Path2D path = new Path2D.Double();
        for (Object t : selected) {
            switch (((ModelElement) t).type()) {
                case ApproximatPoint:
                    path.append(vt.viewMarker((Point) t, selectingMarkSize, ViewTransform.ViewMarkerType.Rectangle), false);
                    path.append(vt.viewMarker((Point) t, selectingMarkSize * 0.62, ViewTransform.ViewMarkerType.Rectangle), false);
                    break;
                case Node:
                    path.append(vt.viewMarker((Point) t, selectingMarkSize, ViewTransform.ViewMarkerType.Round), false);
                    break;
                case BoundaryNode:
                    path.append(vt.viewMarker((Point) t, selectingMarkSize, ViewTransform.ViewMarkerType.Round), false);
                    path.append(vt.viewMarker((Point) t, selectingMarkSize * 0.62, ViewTransform.ViewMarkerType.Round), false);
                    break;
                case LineSegment:
                case CubicBezierSegment:
                case QuadBezierSegment:
                    ((Segment)t).addToPath(path);
                    break;
            }
        }
        return path.createTransformedShape(null);
    }

    @Override
    public void setViewTransform(ViewTransform vt) {
        this.vt = vt;
    }

    public void setGeometryModel(GeometryModel gm) {
        this.gm = gm;
    }


    public void setSelectType(ModelElementType selectType) {
        this.selectType = selectType;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
    private final Shape[] rubbers = new Shape[2];
    private final int[] rubbersColorIs=new int[]{0,0};
    private final int selectRubberI = 0;
    private final int singleSelRubberI = 1;
    private final Color[] colors=new Color[]{Color.red,Color.blue};
    int selectingColorI = 0;
    int selectedColorI = 1;

    public JPanel getPanel() {
        return vt.getPanel();
    }

    public void clearRubber(){
        Graphics2D g2=(Graphics2D)getPanel().getGraphics();
        g2.setXORMode(Color.white);
        for(int i=0;i<rubbers.length;i++){
            if(rubbers[i]!=null){
                g2.setColor(getRubberColor(i));
                g2.draw(rubbers[i]);
                rubbers[i]=null;
            }
        }
    }

    public void nullRubbers(){
        for(int i=0;i<rubbers.length;i++){
            rubbers[i]=null;
        }
    }


    private Color getRubberColor(int index){
        return colors[rubbersColorIs[index]];
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (geometryOperation == GeometryOperation.None) {
            return;
        }
        Graphics2D g2;
        switch (geometryOperation) {
            case SelectFirstSet:
                g2 = (Graphics2D) getPanel().getGraphics();
                g2.setXORMode(Color.white);
                g2.setColor(getRubberColor(selectRubberI));
                if (rubbers[selectRubberI] == null) {
                    rubbers[selectRubberI] = new Rectangle2D.Double(opFstX, opFstY, e.getX(), e.getY());
                    g2.draw(rubbers[selectRubberI]);
                } else {
                    g2.draw(rubbers[selectRubberI]);
                    ((Rectangle2D) rubbers[selectRubberI]).setRect(opFstX, opFstY, e.getX(), e.getY());
                    g2.draw(rubbers[selectRubberI]);
                }
                select(opFstX, opFstY, e.getX(), e.getY());
                g2.draw(selectedShape());
                break;
            case SingleSelectStarted:
                g2 = (Graphics2D) getPanel().getGraphics();
                g2.setXORMode(Color.white);
                g2.setColor(getRubberColor(singleSelRubberI));
                if (rubbers[singleSelRubberI] == null) {
                    rubbers[singleSelRubberI] = new Rectangle2D.Double(opFstX, opFstY, e.getX(), e.getY());
                    g2.draw(rubbers[singleSelRubberI]);
                } else {
                    g2.draw(rubbers[singleSelRubberI]);
                    ((Rectangle2D) rubbers[singleSelRubberI]).setRect(opFstX, opFstY, e.getX(), e.getY());
                    g2.draw(rubbers[singleSelRubberI]);
                }
                select(opFstX, opFstY, singleSelectSize);
                g2.draw(selectedShape());
        }
    }

    enum GeometryOperation {

        None,
        SelectStarted, SelectFirstSet,
        SingleSelectStarted
    }
    GeometryOperation geometryOperation;
    LinkedList selected = new LinkedList();

    public GeometryModelOperator(GeometryModel gm) {
        this.gm = gm;
    }
    private final Point2D forSelect = new Point2D.Double(),  forSelect2 = new Point2D.Double();

    private void select(int x1, int y1, int x2, int y2) {

        try {
            vt.inverseTransform(x1, y1, forSelect);
            vt.inverseTransform(x2, y2, forSelect2);


        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GeometryModelOperator.class.getName()).log(Level.SEVERE, null, ex);
            selected.clear();
            return;
        }
        switch (selectType) {
            case ApproximatPoint:
                gm.approximatePointSearch(selected, forSelect.getX(), forSelect.getX(), forSelect2.getX(), forSelect2.getY());
                return;
            case BoundaryNode:
            case Node:
                gm.nodeDomainSearch(forSelect.getX(), forSelect.getX(), forSelect2.getX(), forSelect2.getY(), selected);
                break;
            case SegmentRoute:
                gm.segmentRouteSearch(forSelect.getX(), forSelect.getX(), forSelect2.getX(), forSelect2.getY(), selected);
                ListIterator li = selected.listIterator();
                return;
            case Segment:
            case LineSegment:
            case CubicBezierSegment:
            case QuadBezierSegment:
                gm.segmentSearch(forSelect.getX(), forSelect.getX(), forSelect2.getX(), forSelect2.getY(), selected);
                break;
        }

        Class<? extends ModelElement> cl = null;
        switch (selectType) {
            case BoundaryNode:
                cl = BoundaryNode.class;
                break;
            case LineSegment:
                cl = LineSegment.class;
                break;
            case CubicBezierSegment:
                cl = CubicBezierSegment.class;
                break;
            case QuadBezierSegment:
                cl = QuadBezierSegment.class;
                break;
        }
        if (cl != null) {
            ListIterator li = selected.listIterator();
            Object n;
            while (li.hasNext()) {
                n = li.next();
                if (n.getClass() != cl) {
                    li.remove();
                }
            }
        }
    }

    private void select(int x1, int y1, int size) {
        select(x1 - size, y1 - size, x1 + size, y1 + size);
    }

    private void singleSelect(int x, int y) {
        select(x, y, singleSelectSize);
        if (selected.size() > 1) {
            Object t = selected.getFirst();
            selected.clear();
            selected.add(t);
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        nullRubbers();
        if (selected.size() > 0) {
            g2.setColor(colors[selectedColorI]);
            g2.draw(selectedShape());
        }
    }
    int opFstX, opFstY;

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                switch (geometryOperation) {
                    case SelectStarted:
                        opFstX = e.getX();
                        opFstY = e.getY();
                        geometryOperation = GeometryOperation.SelectFirstSet;
                        break;
                    case SelectFirstSet:
                        select(opFstX, opFstY, e.getX(), e.getY());
                        geometryOperation = GeometryOperation.None;
                        getPanel().repaint();
                        return;
                    case SingleSelectStarted:
                        singleSelect(e.getX(), e.getY());
                        if (selected.size() > 0) {
                            geometryOperation = GeometryOperation.None;
                            getPanel().repaint();
                            return;
                        }
                        break;
                }
                break;
            case MouseEvent.BUTTON2:
                switch (geometryOperation) {
                    case SelectStarted:
                    case SelectFirstSet:
                    case SingleSelectStarted:
                        geometryOperation = GeometryOperation.None;
                        getPanel().repaint();
                        return;
                }
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        clearRubber();
    }

    public static void main(String[] args) {
        Point p = Node.tempNode(0, 0);
        System.out.println("p.type() = " + p.type());
        Class<? extends Point> cl = p.getClass();
        System.out.println("cl = " + cl);
        boolean tb = cl == Node.class;
        System.out.println("tb = " + tb);

    }
}
