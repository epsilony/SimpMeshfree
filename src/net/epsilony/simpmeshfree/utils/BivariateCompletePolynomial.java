/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

/**
 *
 * @author epsilonyuan@gmail.com
 */
public class BivariateCompletePolynomial {

    public static BivariateArrayFunction factory(final int order) {
        switch (order) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        return result;
                    }
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (order < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 1;
                        for (int i = 1, base = 1; i <= order; i++) {
                            for (int j = base; j < i + base; j++) {
                                result[j] = x * result[j - i];
                            }
                            result[i + base] = result[base - 1] * y;
                            base += i + 1;
                        }
                        return result;
                    }
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
        }
    }

    public static BivariateArrayFunction partialXFactory(final int order) {
        switch (order) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        return result;
                    }
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (order < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 1;
                        result[2] = 0;

                        for (int i = 2, base = 3; i <= order; i++) {
                            result[base] = x * result[base - i];
                            for (int j = base + 1; j < i + base; j++) {
                                result[j] = y * result[j - i - 1];
                            }
                            result[i + base] = 0;
                            base += i + 1;
                        }
                        for (int i = 2, base = 3; i <= order; i++) {
                            for (int j = i; j >= 1; j--) {
                                result[base++] *= j;
                            }
                            base++;
                        }
                        return result;
                    }
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };

        }
    }

    public static BivariateArrayFunction partialYFactory(final int order) {
        switch (order) {
            case 0:
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        return result;
                    }
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

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
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };
            default:
                if (order < 0) {
                    throw new IllegalArgumentException();
                }
                return new BivariateArrayFunction() {

                    @Override
                    public double[] value(double x, double y, double[] result) {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 1;

                        for (int i = 2, base = 3; i <= order; i++) {
                            result[base] = 0;
                            for (int j = base + 1; j < i + base; j++) {
                                result[j] = x * result[j - i];
                            }
                            result[base + i] = y * result[base - 1];
                            base += i + 1;
                        }
                        for (int i = 2, base = 3; i <= order; i++) {
                            base++;
                            for (int j = 1; j <= i; j++) {
                                result[base++] *= j;
                            }

                        }
                        return result;
                    }
                    final int dimension = getArrayLength(order);

                    @Override
                    public int valueDimension() {
                        return dimension;
                    }
                };

        }
    }

    public static int getArrayLength(int order) {
        if (order < 0) {
            throw new IllegalArgumentException();
        }
        return (order + 1) * (order + 2) / 2;
    }
}
