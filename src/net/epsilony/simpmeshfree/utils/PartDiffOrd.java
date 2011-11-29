/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.util.LinkedList;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public abstract class PartDiffOrd {

    public static int register(PartDiffOrd type) {
        int i = 0;
        for (PartDiffOrd t : registered) {
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

    public static boolean isDeepEqual(PartDiffOrd t1, PartDiffOrd t2) {
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

    public static PartDiffOrd ORI() {
        PartDiffOrd result = new PartDiffOrd() {

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
                throw new IllegalArgumentException();
            }

            @Override
            public int getPartialOrder(int index) {
                throw new IllegalArgumentException();
            }
        };
        register(result);
        return result;
    }

    public static PartDiffOrd X() {
        return new PartDiffOrd() {

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
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }

    public static PartDiffOrd Y() {
        PartDiffOrd result = new PartDiffOrd() {

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
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        register(result);
        return result;
    }

    public static PartDiffOrd X2() {
        PartDiffOrd result = new PartDiffOrd() {

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
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 2;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        register(result);
        return result;
    }

    public static PartDiffOrd Y2() {
        PartDiffOrd result = new PartDiffOrd() {

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
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int getPartialOrder(int index) {
                if (index == 0) {
                    return 2;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        register(result);
        return result;
    }

    public static PartDiffOrd XY() {
        PartDiffOrd result = new PartDiffOrd() {

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
                        throw new IllegalArgumentException();
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
    static LinkedList<PartDiffOrd> registered = new LinkedList<>();

    public int getId() {
        return id;
    }

    public abstract int getSumPartialOrder();

    public abstract int getPartialOrdersSize();

    public abstract int getPartialDimension(int index);

    public abstract int getPartialOrder(int index);
}
