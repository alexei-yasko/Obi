package yaskoam.obi.furier

import yaskoam.obi.image.{ImagePanel, ImageContainerPanel}
import swing.{CheckBox, ComboBox, FlowPanel, Button}
import swing.event.ButtonClicked
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D
import yaskoam.obi.ImageUtils

/**
 * @author Q-YAA
 */
class FourierControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val forwardFourierTransformButton = new Button("Forward furier transformation")
    private val inverseFourierTransformButton = new Button("Inverse furier transformation")
    private val fourierVisualizeTypeComboBox = new ComboBox[String](List("Amplitude", "Phase", "Real"))
    private val fourierVisualizeWithLog2CheckBox = new CheckBox("Log2 scale.")

    contents += forwardFourierTransformButton
    contents += inverseFourierTransformButton
    contents += fourierVisualizeTypeComboBox
    contents += fourierVisualizeWithLog2CheckBox

    listenTo(forwardFourierTransformButton, inverseFourierTransformButton)

    reactions += {
        case ButtonClicked(`forwardFourierTransformButton`) => forwardTransformImageList()
        case ButtonClicked(`inverseFourierTransformButton`) => inverseTransformImageList()
    }

    private def forwardTransformImageList() {
        for (imagePanel <- imageContainerPanel.getSelectedImagePanels) {
            forwardTransformImage(imagePanel)
        }

        imageContainerPanel.repaint()
    }

    private def inverseTransformImageList() {
        for (imagePanel <- imageContainerPanel.getSelectedImagePanels) {
            inverseTransformImage(imagePanel)
        }

        imageContainerPanel.repaint()
    }

    private def forwardTransformImage(imagePanel: ImagePanel) {
        val image = imagePanel.getImage
        val width = image.getWidth
        val height = image.getHeight

        val channels = ImageUtils.getSeparatedChannels(image)

        val fftChannel1 = forwardTransform(convertChannelToChannelWithPowerOfTwoWidth(channels._1, width, height))
        val fftChannel2 = forwardTransform(convertChannelToChannelWithPowerOfTwoWidth(channels._2, width, height))
        val fftChannel3 = forwardTransform(convertChannelToChannelWithPowerOfTwoWidth(channels._3, width, height))

        imagePanel.fftImageChannels_=(fftChannel1, fftChannel2, fftChannel3)

        ImageUtils.setChannelsToImage(image, (
            visualizeFftNew(fftChannel1, width, height),
            visualizeFftNew(fftChannel2, width, height),
            visualizeFftNew(fftChannel3, width, height))
        )
    }

    private def convertChannelToChannelWithPowerOfTwoWidth(
        channel: Array[Array[Int]], width: Int, height: Int): Array[Array[Int]] = {

        val newWidth = math.pow(2, log2(width).toInt + 1).toInt
        val newHeight = math.pow(2, log2(height).toInt + 1).toInt

        val newChannel = Array.ofDim[Int](newHeight, newWidth)

        for (i <- 0 until height) {
            System.arraycopy(channel(i), 0, newChannel(i), 0, width)
        }

        newChannel
    }

    private def forwardTransform(input: Array[Array[Int]]): Array[Array[Double]] = {
        val height = input.length
        val width = input(0).length
        val transformationArray = Array.ofDim[Double](input.length, width)

        for (i <- 0 until height) {
            for (j <- 0 until width) {
                transformationArray(i)(j) = input(i)(j).toDouble
            }
        }

        new Fft().fft2DDouble(transformationArray, width, height)
    }

    private def inverseTransformImage(imagePanel: ImagePanel) {
        val image = imagePanel.getImage
        val fftChannels = imagePanel.fftImageChannels

        val width = fftChannels._1(0).length / 2
        val height = fftChannels._1.length

        if (fftChannels != null) {

            for (i <- 0 until height) {
                for (j <- 0 until width) {
                    fftChannels._1(i)(j) = fftChannels._1(i)(j * 2) + fftChannels._1(i)(j * 2 + 1)
                    fftChannels._2(i)(j) = fftChannels._2(i)(j * 2) + fftChannels._2(i)(j * 2 + 1)
                    fftChannels._3(i)(j) = fftChannels._3(i)(j * 2) + fftChannels._3(i)(j * 2 + 1)
                }
            }

            val imageChannel1 = inverseTransform(fftChannels._1, width, height)
            val imageChannel2 = inverseTransform(fftChannels._2, width, height)
            val imageChannel3 = inverseTransform(fftChannels._3, width, height)

            imagePanel.fftImageChannels_=(imageChannel1, imageChannel2, imageChannel3)

            ImageUtils.setChannelsToImage(image, (
                convertDoubleMatrixToIntMatrix(imageChannel1, width, height),
                convertDoubleMatrixToIntMatrix(imageChannel2, width, height),
                convertDoubleMatrixToIntMatrix(imageChannel3, width, height))
            )
        }
    }

    private def inverseTransform(input: Array[Array[Double]], width: Int, height: Int): Array[Array[Double]] = {
        val transformationArray = Array.ofDim[Double](height, 2 * width)

        for (i <- 0 until height) {
            for (j <- 0 until width) {
                transformationArray(i)(j) = input(i)(j)
            }
        }

        val fft = new DoubleFFT_2D(height, width)
        fft.realInverseFull(transformationArray, true)

        val resultArray = Array.ofDim[Double](height, width)
        for (i <- 0 until height) {
            for (j <- 0 until width) {
                resultArray(i)(j) = transformationArray(height - i - 1)(width * 2 - j * 2 - 2) +
                    transformationArray(height - i - 1)(width * 2 - j * 2 - 1)
            }
        }

        resultArray
    }

    private def convertDoubleMatrixToIntMatrix(
        doubleMatrix: Array[Array[Double]], width: Int, height: Int): Array[Array[Int]] = {

        for (row <- doubleMatrix) yield {
            for (element <- row) yield {
                element.toInt
            }
        }
    }

    private def reorder(input: Array[Array[Double]], width: Int, height: Int): Array[Array[Double]] = {
        val tmp = Array.ofDim[Double](height, width)

        val w = width / 2
        val h = height / 2

        val reorderedArray = Array.ofDim[Double](height, width)

        for (i <- 0 until h) {
            for (j <- 0 until w) {
                tmp(i)(j) = input(i)(j)
            }
        }
        for (i <- 0 until h) {
            for (j <- 0 until w) {
                reorderedArray(i)(j) = input(i + h)(j + w)
            }
        }

        for (i <- 0 until h) {
            for (j <- 0 until w) {
                reorderedArray(i + h)(j + w) = tmp(i)(j)
            }
        }

        for (i <- 0 until h) {
            for (j <- 0 until w) {
                tmp(i)(j) = input(i + h)(j)
            }
        }
        for (i <- 0 until h) {
            for (j <- 0 until w) {
                reorderedArray(i + h)(j) = input(i)(j + w)
            }
        }

        for (i <- 0 until h) {
            for (j <- 0 until w) {
                reorderedArray(i)(j + w) = tmp(i)(j)
            }
        }

        reorderedArray
    }

    private def visualizeFftNew(matrix: Array[Array[Double]], imageWidth: Int, imageHeight: Int): Array[Array[Int]] = {
        val resultChannel = Array.ofDim[Int](imageHeight, imageWidth)

        var min = Double.MaxValue
        var max = Double.MinValue

        val reorderedMatrix = reorder(matrix, matrix(0).length, matrix.length)

        for (i <- 0 until matrix.length) {
            for (j <- 0 until matrix(0).length / 2) {

                var result = fourierVisualizeTypeComboBox.selection.index match {
                    case 0 => math.hypot(reorderedMatrix(i)(2 * j), reorderedMatrix(i)(2 * j + 1))
                    case 1 => math.atan2(reorderedMatrix(i)(2 * j), reorderedMatrix(i)(2 * j + 1))
                    case 2 => reorderedMatrix(i)(2 * j)
                }

                if (fourierVisualizeWithLog2CheckBox.selected) {
                    result = log2(result)
                }

                if (max < result) {
                    max = result
                }
                if (min > result) {
                    min = result
                }

                reorderedMatrix(i)(2 * j) = result
                reorderedMatrix(i)(2 * j + 1) = result
            }
        }

        val fromHeight = (reorderedMatrix.length - imageHeight) / 2
        val fromWidth = (reorderedMatrix(0).length - imageWidth) / 2

        for (i <- 0 until imageHeight) {
            for (j <- 0 until imageWidth) {
                val test: Double = (reorderedMatrix(fromHeight + i)(fromWidth + j) - min) / (max - min)
                resultChannel(i)(j) = (255 * test).toInt
            }
        }

        resultChannel
    }

    private def log2(x: Double) = math.log(x) / math.log(2)
}