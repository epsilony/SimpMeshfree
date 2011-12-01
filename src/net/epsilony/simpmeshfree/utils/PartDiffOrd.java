/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 * 偏微分导数的偏微分阶数信息</br>
 * @see PartDiffOrdSettable
 * @author epsilonyuan@gmail.com
 */
public abstract class PartDiffOrd {
    
    public static boolean isDeepEqual(PartDiffOrd t1, PartDiffOrd t2) {
        if (t1.sumOrder() != t2.sumOrder()) {
            return false;
        }
        if (t1.respectTosSize() != t2.respectTosSize()) {
            return false;
        }
        for (int i = 0; i < t1.respectTosSize(); i++) {
            if (t1.respectDimension(i) != t2.respectDimension(i)) {
                return false;
            }
            if (t1.respectOrder(i) != t2.respectOrder(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * get the original function's PartDiffOrd
     * @return 
     */
    public static PartDiffOrd ORI() {
        PartDiffOrd result = new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 0;
            }

            @Override
            public int respectTosSize() {
                return 0;
            }

            @Override
            public int respectDimension(int position) {
                throw new IllegalArgumentException();
            }

            @Override
            public int respectOrder(int position) {
                return 0;
            }
        };
        return result;
    }

    /**
     * get the PartDiffOrd of partial derivative respect to x(dimension 0)
     * @return a new instance
     */
    public static PartDiffOrd X() {
        return new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 1;
            }

            @Override
            public int respectTosSize() {
                return 1;
            }

            @Override
            public int respectDimension(int position) {
                if (position == 0) {
                    return 0;
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int respectOrder(int position) {
                if (position == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }

    /**
     * get the PartDiffOrd of partial derivative respect to y(dimension 1)
     * @return a new instance
     */
    public static PartDiffOrd Y() {
        PartDiffOrd result = new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 1;
            }

            @Override
            public int respectTosSize() {
                return 0;
            }

            @Override
            public int respectDimension(int position) {
                if (position == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int respectOrder(int position) {
                if (position == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        return result;
    }

    /**
     * get the PartDiffOrd of partial derivative respect to x twice (dimension 0, sum order 2)
     * @return a new instance
     */
    public static PartDiffOrd X2() {
        PartDiffOrd result = new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 2;
            }

            @Override
            public int respectTosSize() {
                return 1;
            }

            @Override
            public int respectDimension(int position) {
                if (position == 0) {
                    return 0;
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int respectOrder(int position) {
                if (position == 0) {
                    return 2;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        return result;
    }
    
    /**
     * get the PartDiffOrd of partial derivative respect to y twice(dimension 1)
     * @return a new instance
     */
    public static PartDiffOrd Y2() {
        PartDiffOrd result = new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 2;
            }

            @Override
            public int respectTosSize() {
                return 1;
            }

            @Override
            public int respectDimension(int position) {
                if (position == 0) {
                    return 1;
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public int respectOrder(int index) {
                if (index == 0) {
                    return 2;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        return result;
    }

    /**
     * get the PartDiffOrd of partial derivative respect to x(dimension 0) than to y(dimension 1)
     * @return 
     */
    public static PartDiffOrd XY() {
        PartDiffOrd result = new PartDiffOrd() {

            @Override
            public int sumOrder() {
                return 2;
            }

            @Override
            public int respectTosSize() {
                return 2;
            }

            @Override
            public int respectDimension(int position) {
                switch (position) {
                    case 0:
                        return 0;
                    case 1:
                        return 1;
                    default:
                        throw new IllegalArgumentException();
                }
            }

            @Override
            public int respectOrder(int position) {
                switch (position) {
                    case 0:
                    case 1:
                        return 1;
                    default:
                        return -1;
                }
            }
        };
        return result;
    }

    /**
     * get the sum order of partial derivative for example:</br>
     * <img src="http://epsilony.net/cgi-bin/mathtex.cgi?\frac{\partial{}^3}{\partial{}x^2\partial{}y}" alt="\frac{\partial{}^3}{\partial{}x^2\partial{}y}"></br>
     * the sum order of above partial derivative is 3
     * @return sum order of partial derivative/differential
     */
    public abstract int sumOrder();

    /**
     * the size of "respect to" dimensions size, for example:</br>
     * partial derivate of f respect to x, x, and y will return 2, that is x square and y
     * @return the size of "respect to" dimensions size which can not be larger than {@link #sumOrder} and of course can not be less than 0
     */
    public abstract int respectTosSize();
    
    /**
     * for example:</br>
     * <img src="http://epsilony.net/cgi-bin/mathtex.cgi?\frac{\partial{}^5}{\partial{x^2}\partial{z^3}}" alt="\frac{\partial{}^5}{\partial{x^2}\partial{z^3}}"></br>
     * will return 0 on position 0 (x is commonly dimension 0) and return 2 on position 1 (z is commonly dimension 2) 
     * @param position start from 0
     * @return start from 0 commonly x:0 y:1 z:2 ...
     */
    public abstract int respectDimension(int position);

    /**
     * for example:</br>
     * <img src="http://epsilony.net/cgi-bin/mathtex.cgi?\frac{\partial{}^5}{\partial{x^2}\partial{z^3}}" alt="\frac{\partial{}^5}{\partial{x^2}\partial{z^3}}"></br>
     * will return 2 on position 0 (respect to x at first by 2 times) and return 3 on position 1 (respect to z secondly by 3 times) 
     * @param position start from 0
     * @return order must be not less than 0...
     */
    public abstract int respectOrder(int position);
}
