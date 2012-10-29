package yaskoam.obi

import java.awt.Color

/**
 * @author Q-YAA
 */
class RgbaConverter(rgb: Int) {

    private var rgbValue = rgb

    def this(color: Color) = this(color.getRGB)

    def this(r: Int, g: Int, b: Int) = this(new Color(r, g, b))

    def setColor(color: Color) {
        this.rgbValue = color.getRGB
    }

    def setRGB(rgb: Int) {
        rgbValue = rgb
    }

    def setRGB(r: Int, g: Int, b: Int) {
        setRed(r)
        setGreen(g)
        setBlue(b)
    }

    def setRed(r: Int) {
        validateValue(r)
        rgbValue = ((r & 0xFF) << 16) | ((getGreen & 0xFF) << 8) | ((getBlue & 0xFF) << 0)
    }

    def setGreen(g: Int) {
        validateValue(g)
        this.rgbValue = ((getRed & 0xFF) << 16) | ((g & 0xFF) << 8) | ((getBlue & 0xFF) << 0)
    }

    def setBlue(b: Int) {
        validateValue(b)
        this.rgbValue = ((getRed & 0xFF) << 16) | ((getGreen & 0xFF) << 8) | ((b & 0xFF) << 0)
    }

    def getColor: Color = new Color(getRgb)

    def getRgb: Int = this.rgbValue

    def getRed: Int = (getRgb >> 16) & 0xFF

    def getGreen: Int = (getRgb >> 8) & 0xFF

    def getBlue: Int = (getRgb >> 0) & 0xFF

    private def validateValue (value: Int) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Your value is illegal : '" + value + "'. Ligall value mus be between 0 and 255")
        }
    }
}
