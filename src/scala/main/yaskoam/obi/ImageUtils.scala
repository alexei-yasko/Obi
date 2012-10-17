package yaskoam.obi

import java.awt.image.BufferedImage
import java.io.{IOException, File}
import javax.imageio.ImageIO
import java.awt.{RenderingHints, Color}

/**
 * @author Q-YAA
 */
object ImageUtils {

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


    def buildKernelImageForMotionBlur(radius: Double, angle: Double): BufferedImage = {
        // motionLength plus 2*3 pixels to ensure that generated kernel will be fitted inside the image
        val motionLength = radius * 2
        val motionAngle = angle

        var size = (motionLength + 6).toInt
        size = size + (size % 2)

        val kernelImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)

        val graphics = kernelImage.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        graphics.setPaint(Color.BLACK)
        graphics.fillRect(0, 0, kernelImage.getWidth, kernelImage.getHeight)

        graphics.setColor(Color.WHITE)

        val center = 0.5 + size / 2d
        val motionAngleRad = math.Pi * motionAngle / 180

        graphics.drawLine(
            (center - motionLength * math.cos(motionAngleRad) / 2).toInt,
            (center - motionLength * math.sin(motionAngleRad) / 2).toInt,
            (center + motionLength * math.cos(motionAngleRad) / 2).toInt,
            (center + motionLength * math.sin(motionAngleRad) / 2).toInt
        )

        graphics.dispose()

        kernelImage
    }
}
