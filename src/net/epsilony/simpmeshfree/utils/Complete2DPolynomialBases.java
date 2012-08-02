/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import net.epsilony.utils.geom.Coordinate;

/**
 * 完备多项式，对于order=n阶的完备多项式，{@link BivariateArrayFunction#value(double, double, double[]) value()}函数的输出应为：</br>
 * <img
 * src="http://epsilony.net/cgi-bin/mathtex.cgi?(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)"
 * alt="(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)">
 *
 * @author epsilonyuan@gmail.com
 */
public class Complete2DPolynomialBases implements BasesFunction {
    
    public static SomeFactory<BasesFunction> basesFunctionFactory(final int baseOrder){
        return new SomeFactory<BasesFunction>() {

            @Override
            public BasesFunction produce() {
                return complete2DPolynomialBase(baseOrder);
            }
        };
    }

    public static BasesFunction complete2DPolynomialBase(int baseOrder) {
        return new Complete2DPolynomialBases(baseOrder);
    }
    private int partDiffOrder;
    private BivariateArrayFunction bFun, bFunX, bFunY;
    private int baseOrder;

    public int getBaseOrder() {
        return baseOrder;
    }

    public Complete2DPolynomialBases(int baseOrder) {
        setBaseOrder(baseOrder);
    }

    @Override
    public double[][] values(Coordinate coord, double[][] results) {
        if (null == results) {
            switch (partDiffOrder) {
                case 0:
                    results = new double[1][getDim()];
                    break;
                case 1:
                    results = new double[3][getDim()];
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        double x = coord.x, y = coord.y;
        bFun.value(x, y, results[0]);
        if (partDiffOrder >= 1) {
            bFunX.value(x, y, results[1]);
            bFunY.value(x, y, results[2]);
        }
        return results;
    }

    @Override
    public int getDim() {
        return bFun.valueDimension();
    }

    @Override
    public void setDiffOrder(int order) {
         if (order < 0 || order >= 2) {
            throw new UnsupportedOperationException();
        }
        this.partDiffOrder = order;
    }

    private void setBaseOrder(int order) {
        if(order<1){
            throw new IllegalArgumentException("Base order should be >=1");
        }
        this.baseOrder=order;
        bFun = factory(order);
        if (order >= 1) {
            bFunX = partialXFactory(order);
            bFunY = partialYFactory(order);
        }
    }

    @Override
    public int getDiffOrder() {
        return partDiffOrder;
    }

    /**
     * 获取0到order阶完备多项式的实例
     *
     * @param baseOrder
     * @return a new instance of <img
     * src="http://epsilony.net/cgi-bin/mathtex.cgi?(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)"
     * alt="(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)">
     */
    public static BivariateArrayFunction factory(final int baseOrder) {
        switch (baseOrder) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 1:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        result[1] = x;
                        result[2] = y;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 2:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        result[1] = x;
                        result[2] = y;
                        result[3] = x * x;
                        result[4] = x * y;
                        result[5] = y * y;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 3:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        result[1] = x;
                        result[2] = y;
                        result[3] = x * x;
                        result[4] = x * y;
                        result[5] = y * y;
                        result[6] = x * result[3];
                        result[7] = x * result[4];
                        result[8] = x * result[5];
                        result[9] = y * result[5];
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (baseOrder < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        for (int i = 1, base = 1; i <= baseOrder; i++) {
                            for (int j = base; j < i + base; j++) {
                                result[j] = x * result[j - i];
                            }
                            result[i + base] = result[base - 1] * y;
                            base += i + 1;
                        }
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
        }
    }

    /**
     * 获取<img
     * src="http://epsilony.net/cgi-bin/mathtex.cgi?(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)"
     * alt="(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)">
     * 对x的偏导数
     *
     * @param baseOrder
     * @return 一个新的实例
     */
    public static BivariateArrayFunction partialXFactory(final int baseOrder) {
        switch (baseOrder) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 1:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 1;
                        result[2] = 0;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 2:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 1;
                        result[2] = 0;
                        result[3] = 2 * x;
                        result[4] = y;
                        result[5] = 0;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 3:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 1;
                        result[2] = 0;
                        result[3] = 2 * x;
                        result[4] = y;
                        result[5] = 0;
                        result[6] = 3 * x * x;
                        result[7] = 2 * x * y;
                        result[8] = y * y;
                        result[9] = 0;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (baseOrder < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 1;
                        result[2] = 0;

                        for (int i = 2, base = 3; i <= baseOrder; i++) {
                            result[base] = x * result[base - i];
                            for (int j = base + 1; j < i + base; j++) {
                                result[j] = y * result[j - i - 1];
                            }
                            result[i + base] = 0;
                            base += i + 1;
                        }
                        for (int i = 2, base = 3; i <= baseOrder; i++) {
                            for (int j = i; j >= 1; j--) {
                                result[base++] *= j;
                            }
                            base++;
                        }
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };

        }
    }

    /**
     * 获取<img
     * src="http://epsilony.net/cgi-bin/mathtex.cgi?(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)"
     * alt="(1,x,y,x^2,xy,y^2,x^3,x^2y,xy^2,y^3,\dots,x^n,x^{n-1}y,x^{n-2}y^2,\dots,y^n)">
     * 对y的偏导数
     *
     * @param baseOrder
     * @return 一个新的实例
     */
    public static BivariateArrayFunction partialYFactory(final int baseOrder) {
        switch (baseOrder) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 1:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 1;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 2:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 1;
                        result[3] = 0;
                        result[4] = x;
                        result[5] = 2 * y;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            case 3:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 1;
                        result[3] = 0;
                        result[4] = x;
                        result[5] = 2 * y;
                        result[6] = 0;
                        result[7] = x * x;
                        result[8] = 2 * x * y;
                        result[9] = 3 * y * y;
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (baseOrder < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 1;

                        for (int i = 2, base = 3; i <= baseOrder; i++) {
                            result[base] = 0;
                            for (int j = base + 1; j < i + base; j++) {
                                result[j] = x * result[j - i];
                            }
                            result[base + i] = y * result[base - 1];
                            base += i + 1;
                        }
                        for (int i = 2, base = 3; i <= baseOrder; i++) {
                            base++;
                            for (int j = 1; j <= i; j++) {
                                result[base++] *= j;
                            }

                        }
                        return result;
                    }
                    final int dimension = calculateDimension(baseOrder);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };

        }
    }

    public static int calculateDimension(int order) {
        if (order < 0) {
            throw new IllegalArgumentException();
        }
        return (order + 1) * (order + 2) / 2;
    }

    @Override
    public BasesFunction avatorInstance() {
        BasesFunction res=complete2DPolynomialBase(baseOrder);
        res.setDiffOrder(partDiffOrder);
        return res;
    }
}
