/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.model;

import java.util.LinkedList;

/**
 *
 * @author epsilon
 */
public abstract class PartialDiffType {

    public static int register(PartialDiffType type) {
        int i = 0;
        for (PartialDiffType t : registered) {
            if (isDeepEqual(t, type)) {
                type.id = i;
                return i;
            }
            i++;
        }
        type.id = i;
        registered.add(type);
        return i;
    }

    public static boolean isDeepEqual(PartialDiffType t1, PartialDiffType t2) {
        if (t1.getSumPartialOrder() != t2.getSumPartialOrder()) {
            return false;
        }
        if (t1.getPartialOrdersSize() != t2.getPartialOrdersSize()) {
            return false;
        }
        for (int i = 0; i < t1.getPartialOrdersSize(); i++) {
            if (t1.getPartialDimension(i) != t2.getPartialDimension(i)) {
                return false;
            }
            if (t1.getPartialOrder(i) != t2.getPartialOrder(i)) {
                return false;
            }
        }
        return true;
    }

    public static PartialDiffType ORI() {
        PartialDiffType result = new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 0;
            }

            @Override
            public int getPartialOrdersSize() {
                return 0;
            }

            @Override
            public int getPartialDimension(int index) {
                return -1;
            }

            @Override
            public int getPartialOrder(int index) {
                return -1;
            }
        };
        register(result);
        return result;
    }

    public static PartialDiffType X() {
        return new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 1;
            }

            @Override
            public int getPartialOrdersSize() {
                return 1;
            }

            @Override
            public int getPartialDimension(int index) {
                if (index == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
    }

    public static PartialDiffType Y() {
        PartialDiffType result = new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 1;
            }

            @Override
            public int getPartialOrdersSize() {
                return 0;
            }

            @Override
            public int getPartialDimension(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    return -1;
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
        register(result);
        return result;
    }

    public static PartialDiffType X2() {
        PartialDiffType result = new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 2;
            }

            @Override
            public int getPartialOrdersSize() {
                return 1;
            }

            @Override
            public int getPartialDimension(int index) {
                if (index == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 2;
                } else {
                    return -1;
                }
            }
        };
        register(result);
        return result;
    }

    public static PartialDiffType Y2() {
        PartialDiffType result = new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 2;
            }

            @Override
            public int getPartialOrdersSize() {
                return 1;
            }

            @Override
            public int getPartialDimension(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    return -1;
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 2;
                } else {
                    return -1;
                }
            }
        };
        register(result);
        return result;
    }

    public static PartialDiffType XY() {
        PartialDiffType result = new PartialDiffType() {

            @Override
            public int getSumPartialOrder() {
                return 2;
            }

            @Override
            public int getPartialOrdersSize() {
                return 2;
            }

            @Override
            public int getPartialDimension(int index) {
                switch (index) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                    default:
                        return -1;
                }
            }

            @Override
            public int getPartialOrder(int index) {
                switch (index) {
                    case 0:
                    case 1:
                        return 1;
                    default:
                        return -1;
                }
            }
        };
        register(result);
        return result;
    }
    private int id = -1;
    static LinkedList<PartialDiffType> registered = new LinkedList<>();

    public int getId() {
        return id;
    }

    public abstract int getSumPartialOrder();

    public abstract int getPartialOrdersSize();

    public abstract int getPartialDimension(int index);

    public abstract int getPartialOrder(int index);
    
}
