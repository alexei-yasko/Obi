package yaskoam.obi.image

import swing.Panel
import java.awt.image.BufferedImage
import swing.event.{Key, MouseClicked}
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import java.io.File
import yaskoam.obi.ImageUtils
import java.awt.{BasicStroke, Color}

/**
 * @author yaskoam
 */
class ImagePanel extends Panel {

    private val borderThickness = 8

    private var image: BufferedImage = null
    private var fftChannels: (Array[Array[Double]], Array[Array[Double]], Array[Array[Double]]) = null

    private var isSelected = false

    listenTo(mouse.clicks)

    reactions += {
        case MouseClicked(source, point, modifiers, 2, triggersPopup) => executeAndRepaint(loadImage)
        case MouseClicked(source, point, Key.Modifier.Control, 1, triggersPopup) => executeAndRepaint(setSelectionState)
    }

    def reloadImage(image: BufferedImage) {
        executeAndRepaint(() => this.image = image)
    }

    def getImage: BufferedImage = image

    def isImagePanelSelected = isSelected

    def fftImageChannels = fftChannels

    def fftImageChannels_=(channel1: Array[Array[Double]], channel2: Array[Array[Double]], channel3: Array[Array[Double]]) {
        fftChannels = (channel1, channel2, channel3)
    }

    override protected def paintComponent(graphics: _root_.scala.swing.Graphics2D) {
        super.paintComponent(graphics)

        graphics.setStroke(new BasicStroke(borderThickness))

        if (isSelected) {
            graphics.setColor(Color.RED)
        }
        else {
            graphics.setColor(Color.BLACK)
        }

        graphics.drawRect(0, 0, size.width, size.height)

        if (image != null) {
            graphics.clearRect(
                borderThickness, borderThickness, size.width - borderThickness * 2, size.height - borderThickness * 2)
            graphics.drawImage(image, borderThickness, borderThickness,
                size.width - borderThickness * 2, size.height - borderThickness * 2, null)
            graphics.dispose()
        }
    }

    private def loadImage() {
        val fileChooser = new JFileChooser(".")
        fileChooser.setFileFilter(new FileFilter {
            def accept(file: File): Boolean = isAllowedImageFile(file)

            def getDescription: String = "Image files"
        })

        val result: Int = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val file: File = fileChooser.getSelectedFile
            image = ImageUtils.loadBufferedImage(file)
        }
    }

    private def isAllowedImageFile(file: File): Boolean = {
        val fileName: String = file.getName
        fileName.endsWith(".jpg") || fileName.endsWith(".gif") || fileName.endsWith(".png")
    }

    private def setSelectionState() {
        isSelected = !isSelected
    }

    private def executeAndRepaint(function: () => Unit) {
        function()
        repaint()
    }

    //private def imagePositionX(imageWidth: Int): Int = bounds.x + size.imageWidth / 2 - imageWidth / 2

    //private def imagePositionY(imageHeight: Int): Int = bounds.y + size.imageHeight / 2 - imageHeight / 2
}
