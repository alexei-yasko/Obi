import java.util.Arrays;

import org.junit.Test;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import yaskoam.obi.furier.Fft;

/**
 * @author Q-YAA
 */
public class FftTest {

    @Test
    public void testFft1D() {
        double[] array = {
            146, 253, 100, 14, 93, 8, 2, 3
        };

        double[] myResultArray = new Fft().fft1DDouble(array, array.length);

        double[] resultArray = new double[array.length * 2];
        System.arraycopy(array, 0, resultArray, 0, array.length);
        new DoubleFFT_1D(array.length).realForwardFull(resultArray);

        System.out.println("My result: " + Arrays.toString(myResultArray));
        System.out.println("Result: " + Arrays.toString(resultArray));

        double[] newArray = new double[array.length * 2];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = resultArray[i * 2] + resultArray[i * 2 + 1];
        }

        new DoubleFFT_1D(array.length).realInverseFull(newArray, true);
        for (int i = 0; i < array.length; i++) {
            array[array.length - i - 1] = newArray[i * 2] + newArray[i * 2 + 1];
        }
        System.out.println("Result inverse: " + Arrays.toString(array));
    }

    @Test
    public void testFft2D() {
        double[][] matrix = {
            {146, 253, 100, 19},
            {111, 243, 127, 222},
            {162, 117, 54, 1},
            {215, 27, 19, 189},
        };

        double[][] myResultMatrix = new Fft().fft2DDouble(matrix, matrix[0].length, matrix.length);

        double[][] resultMatrix = new double[matrix.length][matrix[0].length * 2];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, resultMatrix[i], 0, matrix[0].length);
        }
        new DoubleFFT_2D(matrix.length, matrix[0].length).realForwardFull(resultMatrix);

        System.out.println("My result: ");
        for (double[] aMyResultMatrix : myResultMatrix) {
            System.out.println(Arrays.toString(aMyResultMatrix));
        }

        System.out.println("Result: ");
        for (double[] aResultMatrix : resultMatrix) {
            System.out.println(Arrays.toString(aResultMatrix));
        }
    }
}
