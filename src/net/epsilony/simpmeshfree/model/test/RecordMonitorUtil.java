/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import net.epsilony.simpmeshfree.model.WeakformProcessorMonitors;
import net.epsilony.simpmeshfree.model.WeakformProcessorMonitors.Recorder;
import net.epsilony.simpmeshfree.model.WeakformProcessorMonitors.ShapeFunctionRecordNode;
import net.epsilony.utils.geom.Coordinate;

/**
 *
 * @author epsilon
 */
public class RecordMonitorUtil implements Serializable{

    
    WeakformProcessorMonitors.Recorder recorder;
    private ArrayList<List<ShapeFunctionRecordNode>> sortedShapeFunRecords;

    public RecordMonitorUtil(Recorder recorder) {
        this.recorder = recorder;
    }

    public static class QpComp implements Comparator<WeakformProcessorMonitors.ShapeFunctionRecordNode> {

        @Override
        public int compare(ShapeFunctionRecordNode o1, ShapeFunctionRecordNode o2) {
            Coordinate c1 = o1.qp.coordinate;
            Coordinate c2 = o2.qp.coordinate;
            int sx = (int) Math.signum(c1.x - c2.x);
            if (sx != 0) {
                return sx;
            }
            int sy = (int) Math.signum(c1.y - c2.y);
            if (sy != 0) {
                return sy;
            }
            int sz = (int) Math.signum(c1.z - c2.z);
            return sz;
        }
    }

    public List<List<ShapeFunctionRecordNode>> sortByQuadraturePoint() {
        ArrayList<List<ShapeFunctionRecordNode>> res = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            res.add(new LinkedList<ShapeFunctionRecordNode>());
            ArrayList<List<ShapeFunctionRecordNode>> record = recorder.shapeFunRecords.get(i);
            List<ShapeFunctionRecordNode> resRecord = res.get(i);
            for (List<ShapeFunctionRecordNode> list : record) {
                resRecord.addAll(list);
            }
            Collections.sort(resRecord, new QpComp());
        }
        this.sortedShapeFunRecords = res;
        return res;
    }

    public ArrayList<List<ShapeFunctionRecordNode>> getSortedShapeFunRecords(boolean justGet) {
        if (!justGet||sortedShapeFunRecords==null) {
            sortByQuadraturePoint();
        }
        return sortedShapeFunRecords;
    }
    
    public static void saveToFile(String fileName,List<ShapeFunctionRecordNode> shapeFunRecords) throws FileNotFoundException, IOException{
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(fileName) );
        out.writeObject(shapeFunRecords);
    }
    
    public static List<ShapeFunctionRecordNode> getFromFile(String fileName) throws FileNotFoundException, FileNotFoundException, IOException, ClassNotFoundException{
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));
        List<ShapeFunctionRecordNode> res=(List<ShapeFunctionRecordNode>) in.readObject();
        return res;
    }
}
