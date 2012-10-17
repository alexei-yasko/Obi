package yaskoam.obi.normalization

import swing.{Button, FlowPanel}
import yaskoam.obi.image.ImageContainerPanel
import java.awt.image.BufferedImage
import swing.event.ButtonClicked
import scala.Array
import yaskoam.obi.ImageUtils

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
        val separatedChannels = ImageUtils.getSeparatedChannels(image)

        normalizeChannel(separatedChannels._1)
        normalizeChannel(separatedChannels._2)
        normalizeChannel(separatedChannels._3)

        ImageUtils.setChannelsToImage(image, separatedChannels)
    }

    private def normalizeChannel(channel: Array[Array[Int]]) {
        val histogram = ImageUtils.buildHistogram(channel)

        val min = calculateMinLevel(histogram)
        val max = calculateMaxLevel(histogram)

        val b = 255d / (max - min)
        val a = 0 - b * min

        for (i <- 0 until channel.size) {
            for (j <- 0 until channel(i).size) {

                if (channel(i)(j) < max && channel(i)(j) > min) {
                    channel(i)(j) = (a + channel(i)(j) * b).toInt
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