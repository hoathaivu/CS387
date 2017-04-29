package utilities;

/**
 *
 * @author hoa
 *
 */
public class MyVector {
    
    private double[] data;
    private final int size = 2;

    public MyVector(double x, double y) {
        data = new double[2];
        data[0] = x;
        data[1] = y;
    }

    public double get(int pos) {
        return data[pos];
    }

    public double getLength() {
        return Math.sqrt(this.dot(this));
    }

    //return result of current vector + given vector
    public MyVector add(MyVector v) {
        if (v.size != 2)
            throw new IllegalArgumentException();
        else {
            double[] new_data = new double[2];
            for (int i = 0; i < 2; i++)
                new_data[i] = data[i] + v.get(i);
            MyVector temp = new MyVector(new_data[0], new_data[1]);

            return temp;
        }
    }

    //return result of current vector - given vector
    public MyVector minus(MyVector v) {
        return add(v.multiply(-1));
    }

    //return result of dot product of current vector and given vector
    public double dot(MyVector v) {
        if (v.size != 2)
            throw new IllegalArgumentException();
        else {
            double sum = 0;
            for (int i = 0; i < 2; i++)
                sum = sum + data[i] * v.data[i];
            return sum;
        }
    }

    //return result of factor * current vector
    public MyVector multiply(double factor) {
        double[] new_data = new double[2];
        for (int i = 0; i < 2; i++)
            new_data[i] = data[i] * factor;
        MyVector temp = new MyVector(new_data[0], new_data[1]);

        return temp;
    }

    public MyVector divide(double factor) {
        double[] new_data = new double[2];
        for (int i = 0; i < 2; i++)
            new_data[i] = data[i] / factor;
        MyVector temp = new MyVector(new_data[0], new_data[1]);

        return temp;
    }
}
