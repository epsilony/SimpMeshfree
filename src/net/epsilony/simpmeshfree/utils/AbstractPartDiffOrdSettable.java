/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 * 一个{@link PartDiffOrdSettable}实现的例子，可以支持<img src="http://epsilony.net/cgi-bin/mathtex.cgi?\frac{\partial}{\partial{}x}">,<img src="http://epsilony.net/cgi-bin/mathtex.cgi?\frac{\partial}{\partial{}y}">该类可能并不很实用，粘贴复制到是很实用。
 * @author epsilonyuan@gmail.com
 */
public class AbstractPartDiffOrdSettable implements PartDiffOrdSettable{

    protected AbstractPartDiffOrdSettable() {
    }

    private int opt, oriIndex, partialXIndex, partialYIndex;

        @Override
        public void setOrders(PartDiffOrd[] types) {
            /*
             * opt 类似于unix like 系统的文件权限，原值(无偏微分）时为1
             * 偏x为2,偏y为4，偏xx为8,偏xy为16,偏yy为32...
             * 以此类推预设的输出有两个数，一个为偏x导数，另一个为偏y导数时opt为6
             */
            opt = 0;
            for (int i = 0; i < types.length; i++) {
                PartDiffOrd type = types[i];
                switch (type.sumOrder()) {
                    case 0:
                        //输出原函数值的序号
                        oriIndex = i;
                        opt += 1;
                        break;
                    case 1:
                        switch (type.respectDimension(0)) {
                            case 0:
                                opt += 2;
                                partialXIndex = i;
                                break;
                            case 1:
                                opt += 4;
                                partialYIndex = i;
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }
}
