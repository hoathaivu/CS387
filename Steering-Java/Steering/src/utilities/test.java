package utilities;
/**
 *
 * @author hoa
 * 
 */
public class test {
    public static void main(String args[]) throws Exception {
        double[] dat = {1.2, 3.0, -7.6, 10.11};
        MyVector vec = new MyVector(dat);

        for (int i = 0; i < vec.size(); i++)
            System.out.println(vec.get(i));

        double[] data = {0.8, -3.0, -1.4, 0.89};
        MyVector new_vec = vec.add(new MyVector(data));

        for (int i = 0; i < vec.size(); i++)
            System.out.println(new_vec.get(i));
    }
}
