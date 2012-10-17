package yaskoam.obi.furier

import yaskoam.obi.image.{ImageUtil, ImageContainerPanel}
import swing.{FlowPanel, Button}
import swing.event.ButtonClicked
import java.awt.image.BufferedImage
import org.apache.commons.math3.transform.{TransformType, DftNormalization, FastFourierTransformer}
import org.apache.commons.math3.complex.Complex
import java.awt.Color
import javax.swing.JOptionPane

/**
 * @author Q-YAA
 */
class FourierControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val fourierTransformButton = new Button("Furier Transformation")

    contents += fourierTransformButton

    listenTo(fourierTransformButton)

    reactions += {
        case ButtonClicked(`fourierTransformButton`) => transformImageList()
    }

    def transformImageList() {

        for (imagePanel <- imageContainerPanel.getSelectedImagePanels) {
            //transformImage(imagePanel.getImage)
//            val radius = JOptionPane.showInputDialog("Input radius: ").toDouble
//            val angle = JOptionPane.showInputDialog("Input angle: ").toDouble
//            val smooth = JOptionPane.showInputDialog("Input smooth (%): ").toDouble
//
//            doDeconvolution(imagePanel.getImage, radius, angle, smooth)
            transformImage(imagePanel.getImage)
        }

        imageContainerPanel.repaint()
    }

    def transformImage(image: BufferedImage) {
        val channels = ImageUtil.getSeparatedChannels(image)

        var doubleChannel1 = prepareToFft(channels._1, image.getWidth, image.getHeight)
        var doubleChannel2 = prepareToFft(channels._2, image.getWidth, image.getHeight)
        var doubleChannel3 = prepareToFft(channels._3, image.getWidth, image.getHeight)

        var complexChannel1 = executeFft(doubleChannel1, TransformType.FORWARD)
        var complexChannel2 = executeFft(doubleChannel2, TransformType.FORWARD)
        var complexChannel3 = executeFft(doubleChannel3, TransformType.FORWARD)

//        complexChannel1 = fourierTransformer.transform(complexChannel1, TransformType.INVERSE)
//        complexChannel2 = fourierTransformer.transform(complexChannel2, TransformType.INVERSE)
//        complexChannel3 = fourierTransformer.transform(complexChannel3, TransformType.INVERSE)

//        ImageUtil.setChannelsToImage(image, (
//            convertToSquareInt(doubleChannel1, channels._1(0).length, channels._1.length),
//            convertToSquareInt(doubleChannel2, channels._1(0).length, channels._1.length),
//            convertToSquareInt(doubleChannel3, channels._1(0).length, channels._1.length))
//        )

        ImageUtil.setChannelsToImage(image, (
            visualizeFft(complexChannel1, image.getWidth, image.getHeight),
            visualizeFft(complexChannel2, image.getWidth, image.getHeight),
            visualizeFft(complexChannel3, image.getWidth, image.getHeight))
        )
    }

    private def prepareToFft(channel: Array[Array[Int]], width: Int, height: Int): Array[Double] = {
        val fftArray = Array.ofDim[Double](width * height)
        for (x <- 0 until width) {
            for (y <- 0 until height) {
                fftArray((x + 1) * y) = channel(x)(y).toDouble
            }
        }

        fftArray
    }

    private def convertToSquareInt(array: Array[Double], width: Int, height: Int): Array[Array[Int]] = {
        val squareIntArray = Array.ofDim[Int](height, width)

        for (i <- 0 until height) {
            for (j <- 0 until width) {
                squareIntArray(i)(j) = array((i + 1) * j).toInt
            }
        }

        squareIntArray
    }

    private def log2(x: Double) = math.log(x) / math.log(2)

    private def visualizeFft(fft: Array[Complex], width: Int, height: Int): Array[Array[Int]] = {
        val resultChannel = Array.ofDim[Int](width, height)

        // Find maximum
        var maxAmpl = 0d
        var curAmpl = 0d

        for (i <- 0 until width * height) {
            // Extract Amplitude
            curAmpl = math.sqrt(math.pow(fft(i).getReal, 2) + math.pow(fft(i).getImaginary, 2))
            curAmpl = math.log(1 + curAmpl)
            if (curAmpl > maxAmpl) {
                maxAmpl = curAmpl
            }
        }

        // Build image
        for (y <- 0 until height) {
            for (x <- 0 until width) {
                // Normalize
                curAmpl = math.sqrt(math.pow(fft(y * width + x).getReal, 2) + math.pow(fft(y * width + x).getImaginary, 2))
                // Log scale
                curAmpl = 255 * math.log(1 + curAmpl) / maxAmpl
                resultChannel(x)(y) = curAmpl.toInt
            }
        }

        resultChannel
    }

    def doDeconvolution(image: BufferedImage, radius: Double, angle: Double, smooth: Double) {
        val width = image.getWidth
        val height = image.getHeight

        // Create kernel
        val kernelMatrix = buildKernel(width, height, radius, angle)
        val kernelFft = executeFft(kernelMatrix, TransformType.FORWARD)

        val channels = ImageUtil.getSeparatedChannels(image)

        ImageUtil.setChannelsToImage(image, (
            doDeconvolutionForChannel(channels._1, width, height, kernelFft, radius, angle, smooth),
            doDeconvolutionForChannel(channels._2, width, height, kernelFft, radius, angle, smooth),
            doDeconvolutionForChannel(channels._3, width, height, kernelFft, radius, angle, smooth))
        )
    }

    private def doDeconvolutionForChannel(channel: Array[Array[Int]], width: Int, height: Int,
        kernelFft: Array[Complex], radius: Double, angle: Double, smooth: Double): Array[Array[Int]] = {

        // Read given channel
        val arrayForFft = prepareToFft(channel, width, height)
        val chanelFft = executeFft(arrayForFft, TransformType.FORWARD)

        deconvolutionByWiener(chanelFft, width, height, kernelFft, smooth)
        val complexChannel = executeFft(chanelFft, TransformType.INVERSE)

        complexArrayToChannel1(complexChannel, width, height)
    }

    def complexArrayToChannel1(complexArray: Array[Complex], width: Int, height: Int): Array[Array[Int]] = {
        val result = Array.ofDim[Int](width, height)
        for (x <- 0 until width) {
            for (y <- 0 until height) {
                result(x)(y) = complexArray((x + 1) * y).abs.toInt

                if (result(x)(y) > 255) {
                    result(x)(y) = 255
                }
                else if (result(x)(y) < 0) {
                    result(x)(y) = 0
                }
            }
        }

        result
    }

    private def multiplyRealFfts(outFft: Array[Complex], kernelFft: Array[Complex], width: Int, height: Int) {
        for (y <- 0 until height) {
            for (x <- 0 until width) {
                val index = y * width + x
                val value = kernelFft(index).getReal
                outFft(index) = new Complex(outFft(index).getReal * value, outFft(index).getImaginary * value)
            }
        }
    }

    private def executeFft(input: Array[Double], fftType: TransformType): Array[Complex] = {
        val newInputArray = Array.ofDim[Double](math.pow(2, log2(input.length).toInt + 1).toInt)
        for (i <- 0 until input.length) {
            newInputArray(i) = input(i)
        }

        val fftTransformer = new FastFourierTransformer(DftNormalization.STANDARD)
        fftTransformer.transform(newInputArray, fftType)
    }

    private def executeFft(input: Array[Complex], fftType: TransformType): Array[Complex] = {
        val transformer = new FastFourierTransformer(DftNormalization.STANDARD)
        transformer.transform(input, fftType)
    }

    private def deconvolutionByWiener(
        chanelFft: Array[Complex], width: Int, height: Int, kernelFft: Array[Complex], smooth: Double): Array[Complex] = {

        val K = math.pow(1.07, smooth) / 10000.0
        val N: Int = (width / 2 + 1) * height

        val filteredImageFft = Array.ofDim[Complex](chanelFft.length)

        for (i <- 0 until N) {
            val energyValue =
                math.pow(kernelFft(i).getReal, 2) + math.pow(kernelFft(i).getImaginary, 2)

            val wienerValue = kernelFft(i).getReal / (energyValue + K)

            filteredImageFft(i) =
                new Complex(chanelFft(i).getReal * wienerValue, chanelFft(i).getImaginary * wienerValue)
        }

        filteredImageFft
    }

    private def buildKernel(width: Int, height: Int, radius: Double, angle: Double): Array[Double] = {

        val kernelTempMatrix = Array.ofDim[Double](width * height)
        val kernelImage = ImageUtil.buildKernelImageForMotionBlur(radius, angle)

        val size = kernelImage.getWidth

        // Fill kernel
        var sumKernelElements = 0d
        for (x <- 0 until width) {
            for (y <- 0 until height) {
                val index = y * width + x
                var value = 0
                // if we are in the kernel area (of small kernelImage), then take pixel values. Otherwise keep 0
                if (math.abs(x - width / 2) < (size - 2) / 2 && math.abs(y - height / 2) < (size - 2) / 2) {
                    val xLocal = x - (width - size) / 2
                    val yLocal = y - (height - size) / 2
                    value = new Color(kernelImage.getRGB(xLocal, yLocal)).getRed
                }

                kernelTempMatrix(index) = value
                sumKernelElements = sumKernelElements + math.abs(value)
            }
        }

        // Zero-protection
        if (sumKernelElements == 0) {
            sumKernelElements = 1
        }

        // Normalize
        val k = 1 / sumKernelElements
        for (i <- 0 until width * height) {
            kernelTempMatrix(i) = kernelTempMatrix(i) * k
        }

        val outKernel = Array.ofDim[Double](width * height)

        // Translate kernel, because we don't use centered FFT (by multiply input image on pow(-1,x+y))
        // so we need to translate kernel by width/2 to the left and by height/2 to the up
        for (x <- 0 until width) {
            for (y <- 0 until height) {
                val xTranslated = (x + width / 2) % width
                val yTranslated = (y + height / 2) % height
                outKernel(y * width + x) = kernelTempMatrix(yTranslated * width + xTranslated)
            }
        }

        outKernel
    }
}
