package yaskoam.obi

import java.awt.image.BufferedImage
import java.io.{IOException, File}
import javax.imageio.ImageIO

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
}
