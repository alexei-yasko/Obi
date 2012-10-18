package yaskoam.obi.furier

import yaskoam.obi.image.{ImagePanel, ImageContainerPanel}
import swing.{FlowPanel, Button}
import swing.event.ButtonClicked
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D
import yaskoam.obi.ImageUtils

/**
 * @author Q-YAA
 */
class FourierControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val forwardFourierTransformButton = new Button("Forward furier transformation")
    private val inverseFourierTransformButton = new Button("Inverse furier transformation")

    contents += forwardFourierTransformButton
    contents += inverseFourierTransformButton

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

        val fftChannel1 = forwardTransform(channels._1, width, height)
        val fftChannel2 = forwardTransform(channels._2, width, height)
        val fftChannel3 = forwardTransform(channels._3, width, height)

        imagePanel.fftImageChannels_=(fftChannel1, fftChannel2, fftChannel3)

        ImageUtils.setChannelsToImage(image, (
            visualizeFft(fftChannel1, width, height),
            visualizeFft(fftChannel2, width, height),
            visualizeFft(fftChannel3, width, height))
        )
    }

    private def forwardTransform(input: Array[Array[Int]], width: Int, height: Int): Array[Array[Double]] = {
        val transformationArray = Array.ofDim[Double](height, 2 * width)

        for (i <- 0 until height) {
            for (j <- 0 until width) {
                transformationArray(i)(j) = input(i)(j).toDouble
            }
        }

        val fft = new DoubleFFT_2D(height, width)
        fft.realForwardFull(transformationArray)

        val resultArray = Array.ofDim[Double](height, width)
        for (i <- 0 until height) {
            for (j <- 0 until width) {
                resultArray(i)(j) = transformationArray(i)(j * 2) + transformationArray(i)(j * 2 + 1)
            }
        }

        resultArray
    }

    private def inverseTransformImage(imagePanel: ImagePanel) {
        val image = imagePanel.getImage
        val width = image.getWidth
        val height = image.getHeight

        val fftChannels = imagePanel.fftImageChannels

        if (fftChannels != null) {
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

    private def visualizeFft(fftMatrix: Array[Array[Double]], width: Int, height: Int): Array[Array[Int]] = {
        val resultChannel = Array.ofDim[Int](height, width)

        // Find maximum
        var maxAmplitude = 0d
        var currentAmplitude = 0d

        for (i <- 0 until height) {
            for (j <- 0 until width) {
                currentAmplitude = fftMatrix(i)(j).abs
                currentAmplitude = math.log(1 + currentAmplitude)
                if (currentAmplitude > maxAmplitude) {
                    maxAmplitude = currentAmplitude
                }
            }
        }

        // Build image
        for (i <- 0 until height) {
            for (j <- 0 until width) {
                // Normalize
                currentAmplitude = fftMatrix(i)(j).abs
                // Log scale
                currentAmplitude = (255 * math.log(1 + currentAmplitude) / maxAmplitude)
                if (currentAmplitude > 255) currentAmplitude = 255
                resultChannel(i)(j) = currentAmplitude.toInt
            }
        }

        resultChannel
    }


    //
    //    private def prepareToFft(channel: Array[Array[Int]], width: Int, height: Int): Array[Double] = {
    //        val fftArray = Array.ofDim[Double](width * height)
    //        for (x <- 0 until width) {
    //            for (y <- 0 until height) {
    //                fftArray((x + 1) * y) = channel(x)(y).toDouble
    //            }
    //        }
    //
    //        fftArray
    //    }
    //
    //    private def convertFromIntMatrixToDouble(intMatrix: Array[Array[Int]]): Array[Array[Double]] = {
    //        for (row <- intMatrix) yield {
    //            for (element <- row) yield {
    //                element.toDouble
    //            }
    //        }
    //    }
    //
    //    private def visualizeFftNew(fftMatrix: Array[Array[Double]], width: Int, height: Int): Array[Array[Int]] = {
    //        val resultChannel = Array.ofDim[Int](height, width)
    //
    //        // Find maximum
    //        var maxAmpl = 0d
    //        var curAmpl = 0d
    //
    //        for (i <- 0 until height) {
    //            for (j <- 0 until width) {
    //                // Extract Amplitude
    //                curAmpl = fftMatrix(i)(j).abs
    //                curAmpl = math.log(1 + curAmpl)
    //                if (curAmpl > maxAmpl) {
    //                    maxAmpl = curAmpl
    //                }
    //            }
    //        }
    //
    //        // Build image
    //        for (i <- 0 until height) {
    //            for (j <- 0 until width) {
    //                // Normalize
    //                curAmpl = fftMatrix(i)(j).abs
    //                // Log scale
    //                curAmpl = 255 * math.log(1 + curAmpl) / maxAmpl
    //                resultChannel(i)(j) = curAmpl.toInt
    //            }
    //        }
    //
    //        resultChannel
    //    }
    //
    //    //    private def visualizeFft(fftMatrix: Array[Complex], width: Int, height: Int): Array[Array[Int]] = {
    //    //        val resultChannel = Array.ofDim[Int](width, height)
    //    //
    //    //        // Find maximum
    //    //        var maxAmpl = 0d
    //    //        var curAmpl = 0d
    //    //
    //    //        for (i <- 0 until width * height) {
    //    //            // Extract Amplitude
    //    //            curAmpl = math.sqrt(math.pow(fftMatrix(i).getReal, 2) + math.pow(fftMatrix(i).getImaginary, 2))
    //    //            curAmpl = math.log(1 + curAmpl)
    //    //            if (curAmpl > maxAmpl) {
    //    //                maxAmpl = curAmpl
    //    //            }
    //    //        }
    //    //
    //    //        // Build image
    //    //        for (y <- 0 until height) {
    //    //            for (x <- 0 until width) {
    //    //                // Normalize
    //    //                curAmpl = math.sqrt(math.pow(fftMatrix(y * width + x).getReal, 2) + math.pow(fftMatrix(y * width + x).getImaginary, 2))
    //    //                // Log scale
    //    //                curAmpl = 255 * math.log(1 + curAmpl) / maxAmpl
    //    //                resultChannel(x)(y) = curAmpl.toInt
    //    //            }
    //    //        }
    //    //
    //    //        resultChannel
    //    //    }
    //
    //    def doDeconvolution(image: BufferedImage, radius: Double, angle: Double, smooth: Double) {
    //        val width = image.getWidth
    //        val height = image.getHeight
    //
    //        // Create kernel
    //        val kernelMatrix = buildKernel(width, height, radius, angle)
    //        val kernelFft = executeFft(kernelMatrix, width, height)
    //
    //        val channels = ImageUtil.getSeparatedChannels(image)
    //
    //        ImageUtil.setChannelsToImage(image, (
    //                doDeconvolutionForChannel(channels._1, width, height, kernelFft, radius, angle, smooth),
    //                doDeconvolutionForChannel(channels._2, width, height, kernelFft, radius, angle, smooth),
    //                doDeconvolutionForChannel(channels._3, width, height, kernelFft, radius, angle, smooth))
    //        )
    //    }
    //
    //    private def doDeconvolutionForChannel(channel: Array[Array[Int]], width: Int, height: Int,
    //            kernelFft: Array[Double], radius: Double, angle: Double, smooth: Double): Array[Array[Int]] = {
    //
    //        // Read given channel
    //        val matrixForFft = prepareToFft(channel, width, height)
    //        val chanelFft = executeFft(matrixForFft, width, height)
    //
    //        deconvolutionByWiener(chanelFft, width, height, kernelFft, smooth)
    //        val resultFlatChanel = executeInverseFft(chanelFft, width, height)
    //
    //        flatToSquereChannel(resultFlatChanel, width, height)
    //    }
    //
    //    def flatToSquereChannel(flatChannel: Array[Double], width: Int, height: Int): Array[Array[Int]] = {
    //        val squereChannel = Array.ofDim[Int](height, width)
    //
    //        for (i <- 0 until height) {
    //            for (j <- 0 until width) {
    //                squereChannel(i)(j) = flatChannel(i * height + j).toInt
    //                if (squereChannel(i)(j) > 255) {
    //                    squereChannel(i)(j) = 255
    //                }
    //                else if (squereChannel(i)(j) < 0) {
    //                    squereChannel(i)(j) = 0
    //                }
    //
    //            }
    //        }
    //
    //        squereChannel
    //    }
    //
    //    //    def complexArrayToChannel1(complexArray: Array[Complex], width: Int, height: Int): Array[Array[Int]] = {
    //    //        val result = Array.ofDim[Int](width, height)
    //    //        for (x <- 0 until width) {
    //    //            for (y <- 0 until height) {
    //    //                result(x)(y) = complexArray((x + 1) * y).abs.toInt
    //    //
    //    //                if (result(x)(y) > 255) {
    //    //                    result(x)(y) = 255
    //    //                }
    //    //                else if (result(x)(y) < 0) {
    //    //                    result(x)(y) = 0
    //    //                }
    //    //            }
    //    //        }
    //    //
    //    //        result
    //    //    }
    //
    //    //    private def multiplyRealFfts(outFft: Array[Complex], kernelFft: Array[Complex], width: Int, height: Int) {
    //    //        for (y <- 0 until height) {
    //    //            for (x <- 0 until width) {
    //    //                val index = y * width + x
    //    //                val value = kernelFft(index).getReal
    //    //                outFft(index) = new Complex(outFft(index).getReal * value, outFft(index).getImaginary * value)
    //    //            }
    //    //        }
    //    //    }
    //
    //    private def executeFft(input: Array[Double], width: Int, height: Int): Array[Double] = {
    //        val newInputArray = Array.ofDim[Double](2 * width * height)
    //
    //        for (i <- 0 until width * height) {
    //            newInputArray(i) = input(i)
    //        }
    //
    //        val fftMatrix = new DoubleFFT_2D(height, width)
    //        fftMatrix.realForwardFull(newInputArray)
    //
    //        for (i <- 0 until width * height) {
    //            System.out.println(newInputArray(i))
    //        }
    //
    //        for (i <- 0 until height) {
    //            for (j <- 0 until width) {
    //                input(i * width + j) = newInputArray(i * width + 2 * j)
    //            }
    //        }
    //
    //        input
    //    }
    //
    //    private def executeInverseFft(input: Array[Double], width: Int, height: Int): Array[Double] = {
    //        val newInputArray = Array.ofDim[Double](2 * width * height)
    //
    //        for (i <- 0 until width * height) {
    //            newInputArray(i) = input(i)
    //        }
    //
    //        val fftMatrix = new DoubleFFT_2D(height, width)
    //
    //        fftMatrix.realInverseFull(newInputArray, true)
    //
    //        for (i <- 0 until height) {
    //            for (j <- 0 until width) {
    //                input(i * width + j) = newInputArray(i * width + 2 * j)
    //            }
    //        }
    //
    //        input
    //    }
    //
    //    //    private def executeFft(input: Array[Double], fftType: TransformType): Array[Complex] = {
    //    //        val newInputArray = Array.ofDim[Double](math.pow(2, log2(input.length).toInt + 1).toInt)
    //    //        for (i <- 0 until input.length) {
    //    //            newInputArray(i) = input(i)
    //    //        }
    //    //
    //    //        val fftTransformer = new FastFourierTransformer(DftNormalization.STANDARD)
    //    //        fftTransformer.transform(newInputArray, fftType)
    //    //    }
    //
    //    //    private def executeFft(input: Array[Complex], fftType: TransformType): Array[Complex] = {
    //    //        val transformer = new FastFourierTransformer(DftNormalization.STANDARD)
    //    //        transformer.transform(input, fftType)
    //    //    }
    //
    //    private def deconvolutionByWiener(
    //            chanelFft: Array[Double], width: Int, height: Int, kernelFft: Array[Double], smooth: Double): Array[Double] = {
    //
    //        val K = math.pow(1.07, smooth) / 10000.0
    //        val N: Int = (width / 2 + 1) * height
    //
    //        val filteredImageFft = Array.ofDim[Double](chanelFft.length)
    //
    //        for (i <- 0 until N) {
    //            val energyValue = math.pow(kernelFft(i), 2)
    //
    //            val wienerValue = kernelFft(i) / (energyValue + K)
    //
    //            filteredImageFft(i) = chanelFft(i) * wienerValue
    //        }
    //
    //        filteredImageFft
    //    }
    //
    //    private def buildKernel(width: Int, height: Int, radius: Double, angle: Double): Array[Double] = {
    //
    //        val kernelTempMatrix = Array.ofDim[Double](width * height)
    //        val kernelImage = ImageUtil.buildKernelImageForMotionBlur(radius, angle)
    //
    //        val size = kernelImage.getWidth
    //
    //        // Fill kernel
    //        var sumKernelElements = 0d
    //        for (x <- 0 until width) {
    //            for (y <- 0 until height) {
    //                val index = y * width + x
    //                var value = 0
    //                // if we are in the kernel area (of small kernelImage), then take pixel values. Otherwise keep 0
    //                if (math.abs(x - width / 2) < (size - 2) / 2 && math.abs(y - height / 2) < (size - 2) / 2) {
    //                    val xLocal = x - (width - size) / 2
    //                    val yLocal = y - (height - size) / 2
    //                    value = new Color(kernelImage.getRGB(xLocal, yLocal)).getRed
    //                }
    //
    //                kernelTempMatrix(index) = value
    //                sumKernelElements = sumKernelElements + math.abs(value)
    //            }
    //        }
    //
    //        // Zero-protection
    //        if (sumKernelElements == 0) {
    //            sumKernelElements = 1
    //        }
    //
    //        // Normalize
    //        val k = 1 / sumKernelElements
    //        for (i <- 0 until width * height) {
    //            kernelTempMatrix(i) = kernelTempMatrix(i) * k
    //        }
    //
    //        val outKernel = Array.ofDim[Double](width * height)
    //
    //        // Translate kernel, because we don't use centered FFT (by multiply input image on pow(-1,x+y))
    //        // so we need to translate kernel by width/2 to the left and by height/2 to the up
    //        for (x <- 0 until width) {
    //            for (y <- 0 until height) {
    //                val xTranslated = (x + width / 2) % width
    //                val yTranslated = (y + height / 2) % height
    //                outKernel(y * width + x) = kernelTempMatrix(yTranslated * width + xTranslated)
    //            }
    //        }
    //
    //        outKernel
    //    }
}
