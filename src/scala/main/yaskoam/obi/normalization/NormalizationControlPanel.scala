package yaskoam.obi.normalization

import swing.{Button, FlowPanel}
import yaskoam.obi.image.{ImageUtil, ImageContainerPanel}
import java.awt.image.BufferedImage
import java.awt.Color
import swing.event.ButtonClicked
import scala.Array

/**
 * @author Q-YAA
 */
class NormalizationControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val histogramPixelsLimit = 0.01

    private val normalizationButton = new Button("Histogram aligment")

    contents += normalizationButton

    listenTo(normalizationButton)

    reactions += {
        case ButtonClicked(`normalizationButton`) => normalizeImageList()
    }


    def normalizeImageList() {

        for (imagePanel <- imageContainerPanel.getSelectedImagePanels) {
            normalizeImage(imagePanel.getImage)
        }

        imageContainerPanel.repaint()
    }

    def normalizeImage(image: BufferedImage) {
        val separatedChannels = ImageUtil.getSeparatedChannels(image)

        normalizeChannel(separatedChannels._1)
        normalizeChannel(separatedChannels._2)
        normalizeChannel(separatedChannels._3)

        ImageUtil.setChannelsToImage(image, separatedChannels)
    }

    private def normalizeChannel(channel: Array[Array[Int]]) {
        val histogram = ImageUtil.buildHistogram(channel)

        val min = calculateMinLevel(histogram)
        val max = calculateMaxLevel(histogram)

        val b = 255d / (max - min)
        val a = 0 - b * min

        for (x <- 0 until channel.size) {
            for (y <- 0 until channel(x).size) {

                if (channel(x)(y) < max && channel(x)(y) > min) {
                    channel(x)(y) = (a + channel(x)(y) * b).toInt
                }
            }
        }
    }

    private def calculateMinLevel(histogram: Array[Int]): Int = {
        var min = 0
        val pixelQuantity = calculatePixelsQuantity(histogram)
        var pixelsSum = 0

        while (pixelsSum <= pixelQuantity * histogramPixelsLimit) {
            pixelsSum = pixelsSum + histogram(min)
            min = min + 1
        }

        min - 1
    }

    private def calculateMaxLevel(histogram: Array[Int]): Int = {
        var max = histogram.length - 1
        val pixelQuantity = calculatePixelsQuantity(histogram)
        var pixelsSum = 0

        while (pixelsSum <= pixelQuantity * histogramPixelsLimit) {
            pixelsSum = pixelsSum + histogram(max)
            max = max - 1
        }

        max + 1
    }

    private def calculatePixelsQuantity(histogram: Array[Int]): Int = {
        var result = 0
        for (quantity <- histogram) {
            result = result + quantity
        }
        result
    }
}