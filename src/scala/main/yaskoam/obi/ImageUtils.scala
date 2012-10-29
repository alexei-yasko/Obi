package yaskoam.obi

import java.awt.image.BufferedImage
import java.io.{IOException, File}
import javax.imageio.ImageIO
import java.awt.Color

/**
 * @author Q-YAA
 */
object ImageUtils {

    val ANAGLYPH_RED_CYAN_COLOR = 1
    val ANAGLYPH_RED_GREEN_COLOR = 2
    val ANAGLYPH_RED_BLUE_COLOR = 3
    val ANAGLYPH_YELLOW_BLUE_COLOR = 4

    def loadBufferedImage(path: String): BufferedImage = {
        try {
            val imageFile: File = new File(path)
            ImageIO.read(imageFile)
        }
        catch {
            case ex: IOException => {
                throw new IllegalStateException("Image file not found!", ex)
            }
        }
    }

    def loadBufferedImage(file: File): BufferedImage = {
        try {
            ImageIO.read(file)
        }
        catch {
            case ex: IOException => {
                throw new IllegalStateException("Image file not found!", ex)
            }
        }
    }

    def getSeparatedChannels(image: BufferedImage): (Array[Array[Int]], Array[Array[Int]], Array[Array[Int]]) = {
        val width = image.getWidth
        val height = image.getHeight

        val redChannel = Array.ofDim[Int](height, width)
        val greenChannel = Array.ofDim[Int](height, width)
        val blueChannel = Array.ofDim[Int](height, width)

        for (x <- 0 until width) {
            for (y <- 0 until height) {
                val color = new Color(image.getRGB(x, y))
                redChannel(y)(x) = color.getRed
                greenChannel(y)(x) = color.getGreen
                blueChannel(y)(x) = color.getBlue
            }
        }

        (redChannel, greenChannel, blueChannel)
    }

    def buildHistogram(channel: Array[Array[Int]]): Array[Int] = {
        val histogram = new Array[Int](256)

        for (i <- 0 until channel.size) {
            for (j <- 0 until channel(i).size) {
                histogram(channel(i)(j)) = histogram(channel(i)(j)) + 1
            }
        }

        histogram
    }

    def setChannelsToImage(
            image: BufferedImage, channels: (Array[Array[Int]], Array[Array[Int]], Array[Array[Int]])) {

        for (i <- 0 until image.getHeight) {
            for (j <- 0 until image.getWidth) {
                val color = new Color(channels._1(i)(j), channels._2(i)(j), channels._3(i)(j))
                image.setRGB(j, i, color.getRGB)
            }
        }
    }

    def getAnaglyphImage(left: BufferedImage, right: BufferedImage, anaglyphType: Int): BufferedImage = {
        val width = math.min(left.getWidth, right.getWidth)
        val height = math.min(left.getHeight, right.getHeight)

        val anaglyphImage = new BufferedImage(width, height, left.getType)

        for (x <- 0 until width) {
            for (y <- 0 until height) {
                val pxL = new RgbConverter(left.getRGB(x, y))
                val pxR = new RgbConverter(right.getRGB(x, y))
                anaglyphImage.setRGB(x, y, multyplayColor(pxL, pxR, math.abs(anaglyphType)))
            }
        }

        anaglyphImage
    }

    private def multyplayColor(left: RgbConverter, right: RgbConverter, anaglyphType: Int): Int = {

        val leftMatrix = Array(0d, 0d, 0d)
        val rightMatrix = Array(0d, 0d, 0d)

        anaglyphType match {
            case ANAGLYPH_RED_CYAN_COLOR => {
                leftMatrix(0) = 1d
                rightMatrix(1) = 1d
                rightMatrix(2) = 1d
            }

            case ANAGLYPH_RED_GREEN_COLOR => {
                leftMatrix(0) = 1d
                rightMatrix(1) = 1d
            }

            case ANAGLYPH_RED_BLUE_COLOR => {
                leftMatrix(0) = 1d
                rightMatrix(2) = 1d
            }

            case ANAGLYPH_YELLOW_BLUE_COLOR => {
                leftMatrix(0) = 1d
                leftMatrix(1) = 1d
                rightMatrix(2) = 1d
            }
        }

        val red = ((math.round(leftMatrix(0) * left.getRed + rightMatrix(0) * right.getRed)) & 0xFF) << 16
        val green = ((math.round(leftMatrix(1) * left.getGreen + rightMatrix(1) * right.getGreen)) & 0xFF) << 8
        val blue = ((math.round(leftMatrix(2) * left.getBlue + rightMatrix(2) * right.getBlue)) & 0xFF) << 0

        red.toInt | green.toInt | blue.toInt
    }
}
