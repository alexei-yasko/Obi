package yaskoam.obi.image

import java.awt.image.BufferedImage
import java.awt.{RenderingHints, Color}

/**
 * @author Q-YAA
 */
object ImageUtil {

    def getSeparatedChannels(image: BufferedImage): (Array[Array[Int]], Array[Array[Int]], Array[Array[Int]]) = {
        val width = image.getWidth
        val height = image.getHeight

        val redChannel = Array.ofDim[Int](width, height)
        val greenChannel = Array.ofDim[Int](width, height)
        val blueChannel = Array.ofDim[Int](width, height)

        for (x <- 0 until image.getWidth) {
            for (y <- 0 until image.getHeight) {
                val color = new Color(image.getRGB(x, y))
                redChannel(x)(y) = color.getRed
                greenChannel(x)(y) = color.getGreen
                blueChannel(x)(y) = color.getBlue
            }
        }

        (redChannel, greenChannel, blueChannel)
    }

    def buildHistogram(channel: Array[Array[Int]]): Array[Int] = {
        val histogram = new Array[Int](256)

        for (x <- 0 until channel.size) {
            for (y <- 0 until channel(x).size) {
                histogram(channel(x)(y)) = histogram(channel(x)(y)) + 1
            }
        }

        histogram
    }

    def setChannelsToImage(
        image: BufferedImage, channels: (Array[Array[Int]], Array[Array[Int]], Array[Array[Int]])) {

        for (x <- 0 until image.getWidth) {
            for (y <- 0 until image.getHeight) {
                val color = new Color(channels._1(x)(y), channels._2(x)(y), channels._3(x)(y))
                image.setRGB(x, y, color.getRGB)
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
