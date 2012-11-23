package yaskoam.obi.segmentation

import java.awt.image.BufferedImage
import yaskoam.obi.filter.{FilterMatrix, FilterUtils}
import yaskoam.obi.ImageUtils
import java.util

/**
 * @author Q-YAA
 */
class CunnyDetector(lowThreshold: Int, heightThreshold: Int, gaussianSigma: Double) {

    private val GAUSSIAN_RADIUS = 5

    private val STRONG_EDGE_PIXEL_VALUE = 255
    private val WEAK_EDGE_PIXEL_VALUE = 50

    private var history = List[BufferedImage]()

    def detect(sourceImage: BufferedImage): BufferedImage = {
        val bwSourceImage = ImageUtils.toBlackAndWhiteImage(sourceImage)
        val filteredImage = FilterUtils.boofCVGaussianFilter(bwSourceImage, gaussianSigma, GAUSSIAN_RADIUS)

        history = filteredImage :: history

        val xyGradient = calculateSobelGradients(filteredImage)
        val imageMatrix = calculateGradientModule(xyGradient._1, xyGradient._2)

        history = displayMatrix(imageMatrix) :: history

        nonMaximumSuppression(xyGradient._1, xyGradient._2, imageMatrix)

        history = displayMatrix(imageMatrix) :: history

        doubleThresholding(lowThreshold, heightThreshold, imageMatrix)

        history = displayMatrix(imageMatrix) :: history

        edgeTrackingByHysteresis(imageMatrix)

        history = displayMatrix(imageMatrix) :: history

        displayMatrix(imageMatrix)
    }

    def getHistory = history.reverse

    def calculateSobelGradients(image: BufferedImage): (Array[Array[Int]], Array[Array[Int]]) = {
        //        val gxMatrix = new FilterMatrix(3, 3, Array[Float](
        //            -1f, 0f, 1f,
        //            -2f, 0f, 2f,
        //            -1f, 0f, 1f)
        //        )
        //
        //        val gyMatrix = new FilterMatrix(3, 3, Array[Float](
        //            -1f, -2f, -1f,
        //            0f, 0f, 0f,
        //            1f, 2f, 1f)
        //        )

        val gxMatrix = new FilterMatrix(3, 3, Array[Float](
            0f, -1f, 0f,
            -1f, 4f, -1f,
            0f, -1f, 0f)
        )

        val gyMatrix = new FilterMatrix(3, 3, Array[Float](
            -1f, -1f, -1f,
            -1f, 8f, -1f,
            -1f, -1f, -1f)
        )

        val gxImage = FilterUtils.filterImageWithMatrix(image, gxMatrix)
        val gyImage = FilterUtils.filterImageWithMatrix(image, gyMatrix)

        (ImageUtils.getSeparatedChannels(gxImage)._1, ImageUtils.getSeparatedChannels(gyImage)._1)
    }

    def calculateGradientModule(xGradient: Array[Array[Int]], yGradient: Array[Array[Int]]): Array[Array[Int]] = {
        val gradientModule = Array.ofDim[Int](xGradient.size, xGradient(0).size)

        for (y <- 0 until gradientModule(0).size) {
            for (x <- 0 until gradientModule.size) {
                gradientModule(x)(y) = (math.sqrt(math.pow(xGradient(x)(y), 2) + math.pow(yGradient(x)(y), 2))).toInt
            }
        }

        gradientModule
    }

    def calculateGradientDirection(xPixel: Int, yPixel: Int): Int = {
        val angle = math.atan(yPixel.toDouble / xPixel.toDouble) * 180 / math.Pi
        (math.round(angle / 45d) * 45).toInt
    }

    def displayMatrix(gradientModule: Array[Array[Int]]): BufferedImage = {
        for (y <- 0 until gradientModule(0).size) {
            for (x <- 0 until gradientModule.size) {

                if (gradientModule(x)(y) > 255) {
                    gradientModule(x)(y) = 255
                }
            }
        }

        val resultImage = new BufferedImage(gradientModule(0).size, gradientModule.size, BufferedImage.TYPE_INT_ARGB)
        ImageUtils.setChannelsToImage(resultImage, (gradientModule, gradientModule, gradientModule))

        resultImage
    }

    def nonMaximumSuppression(
        xGradient: Array[Array[Int]], yGradient: Array[Array[Int]], gradientModule: Array[Array[Int]]) {

        for (y <- 1 until gradientModule(0).size - 1) {
            for (x <- 1 until gradientModule.size - 1) {

                val gradientDirection = calculateGradientDirection(xGradient(x)(y), yGradient(x)(y))

                if (gradientDirection == 0 && (gradientModule(x)(y) < gradientModule(x)(y - 1)
                    || gradientModule(x)(y) < gradientModule(x)(y + 1))) {

                    gradientModule(x)(y) = 0
                }
                else if (gradientDirection == 45 && (gradientModule(x)(y) < gradientModule(x + 1)(y - 1)
                    || gradientModule(x)(y) < gradientModule(x - 1)(y + 1))) {

                    gradientModule(x)(y) = 0
                }
                else if (gradientDirection == 90 && (gradientModule(x)(y) < gradientModule(x - 1)(y)
                    || gradientModule(x)(y) < gradientModule(x + 1)(y))) {

                    gradientModule(x)(y) = 0
                }
                else if (gradientDirection == 135 && (gradientModule(x)(y) < gradientModule(x - 1)(y - 1)
                    || gradientModule(x)(y) < gradientModule(x + 1)(y + 1))) {

                    gradientModule(x)(y) = 0
                }
            }
        }
    }

    def doubleThresholding(lowerBound: Int, upperBound: Int, gradientModule: Array[Array[Int]]) {
        for (y <- 1 until gradientModule(0).size - 1) {
            for (x <- 1 until gradientModule.size - 1) {

                if (gradientModule(x)(y) < lowerBound) {
                    gradientModule(x)(y) = 0
                }
                else if (gradientModule(x)(y) > upperBound) {
                    gradientModule(x)(y) = STRONG_EDGE_PIXEL_VALUE
                }
                else {
                    gradientModule(x)(y) = WEAK_EDGE_PIXEL_VALUE
                }
            }
        }
    }

    def edgeTrackingByHysteresis(source: Array[Array[Int]]) {
        val height = source(0).size - 1
        val weight = source.size - 1

        val candidates = new util.ArrayDeque[(Int, Int)](50000)
        val labelMatrix = Array.ofDim[Boolean](weight, height)

        for (y <- 1 until height) {
            for (x <- 1 until weight) {

                if (source(x)(y) == STRONG_EDGE_PIXEL_VALUE && !labelMatrix(x)(y)) {
                    candidates.addLast((x, y))

                    // Process all candidates recursively through holding queue
                    while (!candidates.isEmpty) {
                        val candidate = candidates.pollFirst()

                        val isInvalidPoint = (candidate._1 < 1) || (candidate._1 >= weight) ||
                            (candidate._2 < 1) || (candidate._2 >= height)

                        val isLowThresholdPoint = !isInvalidPoint && source(candidate._1)(candidate._2) == 0
                        val isVisitedPoint = !isInvalidPoint && labelMatrix(candidate._1)(candidate._2)
                        val isProcessedPoint = !isInvalidPoint && !isLowThresholdPoint && !isVisitedPoint

                        // Do not process invalid points
                        // Ignore points below the LOW threshold
                        // If pixel location not already visited, visit neighbors
                        if (isProcessedPoint) {
                            labelMatrix(candidate._1)(candidate._2) = true

                            // Visit all neighbours
                            candidates.addLast((candidate._1 - 1, candidate._2 - 1))
                            candidates.addLast((candidate._1, candidate._2 - 1))
                            candidates.addLast((candidate._1 + 1, candidate._2 - 1))
                            candidates.addLast((candidate._1 - 1, candidate._2))
                            candidates.addLast((candidate._1 + 1, candidate._2))
                            candidates.addLast((candidate._1 - 1, candidate._2 + 1))
                            candidates.addLast((candidate._1, candidate._2 + 1))
                            candidates.addLast((candidate._1 + 1, candidate._2 + 1))
                        }
                    }
                }
            }
        }

        for (y <- 1 until height) {
            for (x <- 1 until weight) {

                if (labelMatrix(x)(y)) {
                    source(x)(y) = STRONG_EDGE_PIXEL_VALUE
                }
                else {
                    source(x)(y) = 0
                }
            }
        }
    }

    def segmentation(source: Array[Array[Int]]) {
        val height = source(0).size - 1
        val weight = source.size - 1

        for (y <- 1 until weight) {

            var state = false
            var currentColor = 100

            var intervals = List[(Int, Int)]()

            for (x <- 1 until height) {

                if (!state && source(y)(x) == STRONG_EDGE_PIXEL_VALUE && source(y + 1)(x) == 0) {
                    state = true
                    intervals = (y, x) :: intervals
                }
                else if (state && source(y)(x) == STRONG_EDGE_PIXEL_VALUE && source(y + 1)(x) == 0) {
                    state = false
                    intervals = (y, x) :: intervals
                }
            }

            intervals = intervals.reverse

            var previousPoint: (Int, Int) = null
            for (point <- intervals) {

                if (previousPoint != null) {

                    for (x <- previousPoint._2 until point._2) {
                        source(point._1)(x) = currentColor
                    }

                    currentColor = getNextColor(currentColor)
                }

                previousPoint = point
            }
        }
    }

    def getNextColor(color: Int): Int = {
        val newColor = color + 25

        if (newColor > 255) {
            100
        }
        else {
            newColor
        }
    }
}