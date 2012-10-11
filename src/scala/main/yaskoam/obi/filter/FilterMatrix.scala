package yaskoam.obi.filter

/**
 * @author yaskoam
 */
class FilterMatrix(val height: Int, val width: Int, val matrix: Array[Float]) {

    require(matrix.length == (height * width), "Passed parameters of the filter matrix are wrong!")

    def getHeight: Int = height

    def getMatrix: Array[Float] = matrix

    def getWidth: Int = width
}
