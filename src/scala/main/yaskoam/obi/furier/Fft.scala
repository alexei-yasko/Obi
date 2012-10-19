package yaskoam.obi.furier

import org.apache.commons.math3.complex.Complex

/**
 * @author Q-YAA
 */
class Fft {

    def fft2DDouble(matrix: Array[Array[Double]], width: Int, height: Int): Array[Array[Double]] = {
        val complexMatrix = convertDoubleMatrixToComplexMatrix(matrix, width, height)

        for (i <- 0 until height) {
            fft1DComplex(complexMatrix(i), width)
        }
        for (j <- 0 until width) {
            val column = Array.ofDim[Complex](height)

            for (i <- 0 until height) {
                column(i) = complexMatrix(i)(j)
            }

            fft1DComplex(column, height)

            for (i <- 0 until height) {
                complexMatrix(i)(j) = column(i)
            }
        }

        convertComplexMatrixToDoublePresentation(complexMatrix, width, height)
    }

    def fft1DDouble(array: Array[Double], length: Int): Array[Double] = {
        val complexArray = convertDoubleArrayToComplexArray(array, length)
        fft1DComplex(complexArray, length)
        convertComplexArrayToDoublePresentation(complexArray, length)
    }

    private def fft1DComplex(array: Array[Complex], length: Int) {
        if (length == 1) {
            return
        }

        val array0 = Array.ofDim[Complex](length / 2)
        val array1 = Array.ofDim[Complex](length / 2)

        for (i <- 0 until(length, 2)) {
            array0(i / 2) = array(i)
            array1(i / 2) = array(i + 1)
        }

        fft1DComplex(array0, length / 2)
        fft1DComplex(array1, length / 2)

        val angular = 2 * math.Pi / length

        var w = new Complex(1)
        val wn = new Complex(math.cos(angular), math.sin(angular))

        for (i <- 0 until length / 2) {
            array(i) = array0(i).add(w.multiply(array1(i)))
            array(i + length / 2) = array0(i).subtract(w.multiply(array1(i)))
            w = w.multiply(wn)
        }
    }

    private def convertDoubleMatrixToComplexMatrix(
        doubleMatrix: Array[Array[Double]], width: Int, height: Int): Array[Array[Complex]] = {

        val complexMatrix = Array.ofDim[Complex](height, width)

        for (i <- 0 until height) {
            complexMatrix(i) = convertDoubleArrayToComplexArray(doubleMatrix(i), width)
        }

        complexMatrix
    }


    private def convertComplexMatrixToDoublePresentation(
        complexMatrix: Array[Array[Complex]], width: Int, height: Int): Array[Array[Double]] = {

        val doubleMatrix = Array.ofDim[Double](height, width * 2)

        for (i <- 0 until height) {
            doubleMatrix(i) = convertComplexArrayToDoublePresentation(complexMatrix(i), width)
        }

        doubleMatrix
    }

    private def convertDoubleArrayToComplexArray(doubleArray: Array[Double], length: Int): Array[Complex] = {
        val complexArray = Array.ofDim[Complex](length)

        for (i <- 0 until length) {
            complexArray(i) = new Complex(doubleArray(i))
        }

        complexArray
    }

    private def convertComplexArrayToDoublePresentation(complexArray: Array[Complex], length: Int): Array[Double] = {
        val doubleArray = Array.ofDim[Double](length * 2)

        for (i <- 0 until length) {
            doubleArray(i * 2) = complexArray(i).getReal
            doubleArray(i * 2 + 1) = -1 * complexArray(i).getImaginary
        }

        doubleArray
    }
}
