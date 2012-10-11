package yaskoam.obi.filter

import java.awt.image.{ConvolveOp, Kernel, BufferedImage}
import boofcv.alg.filter.blur.BlurImageOps
import boofcv.alg.filter.basic.GrayImageOps
import boofcv.struct.image.{MultiSpectral, ImageUInt8}
import boofcv.core.image.ConvertBufferedImage

/**
 * @author yaskoam
 */
object FilterUtils {

    val SMOOTH_FILTER_MATRIX_3x3: FilterMatrix =
        new FilterMatrix(3, 3, Array[Float](
            1 / 9F, 1 / 9F, 1 / 9F,
            1 / 9F, 1 / 9F, 1 / 9F,
            1 / 9F, 1 / 9F, 1 / 9F)
        )

    val EDGE_DETECTION_FILTER_MATRIX_3x3: FilterMatrix =
        new FilterMatrix(3, 3, Array[Float](
            0F, -1F, 0F,
            -1F, 4F, -1F,
            0F, -1F, 0F)
        )

    val EMBOSS_FILTER_MATRIX_3x3: FilterMatrix =
        new FilterMatrix(3, 3, Array[Float](
            -2F, -1F, 0F,
            -1F, 1F, 1F,
            0F, 1F, 2F)
        )

    val ACUTANCE_FILTER_MATRIX_3x3: FilterMatrix =
        new FilterMatrix(3, 3, Array[Float](
            -0.1F, -0.1F, -0.1F,
            -0.1F, 1.8F, -0.1F,
            -0.1F, -0.1F, -0.1F)
        )

    def filterImageWithMatrix(originImage: BufferedImage, filterMatrix: FilterMatrix): BufferedImage = {
        val kernel: Kernel = new Kernel(filterMatrix.getWidth, filterMatrix.getHeight, filterMatrix.getMatrix)
        val convolveOp: ConvolveOp = new ConvolveOp(kernel)

        val filteredImage: BufferedImage =
            new BufferedImage(originImage.getWidth, originImage.getHeight, originImage.getType)

        convolveOp.filter(originImage, filteredImage)
    }

    def boofCVMedianFilter(image: BufferedImage, radius: Int): BufferedImage =
        boofCVFilter(image, BlurImageOps.median(_, _, radius))

    def boofCVInvertFilter(image: BufferedImage, maxColors: Int): BufferedImage =
        boofCVFilter(image, GrayImageOps.invert(_, maxColors, _))


    private def boofCVFilter(image: BufferedImage, filter: (ImageUInt8, ImageUInt8) => Unit): BufferedImage = {
        val originImage = ConvertBufferedImage.convertFromMulti(image, null, classOf[ImageUInt8])

        val filteredImage = new MultiSpectral[ImageUInt8](
            classOf[ImageUInt8], originImage.width, originImage.height, originImage.getNumBands)

        for (i <- 0 until originImage.getNumBands) {
            filter(originImage.getBand(i), filteredImage.getBand(i))
        }

        ConvertBufferedImage.convertTo(filteredImage, null)
    }
}
