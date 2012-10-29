package yaskoam.obi

import image.ImagePanel
import swing.{Component, Dialog}
import java.awt.image.BufferedImage
import java.awt.Dimension

/**
 * @author Q-YAA
 */
class ImageDialog(image: BufferedImage, parent: Component) extends Dialog {

    private val resultImagePanel = new ImagePanel
    resultImagePanel.setImage(image)

    contents = resultImagePanel

    modal = false

    size_=(new Dimension(image.getWidth, image.getHeight))
    preferredSize_=(new Dimension(image.getWidth, image.getHeight))

    setLocationRelativeTo(parent)
}
