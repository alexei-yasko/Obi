package yaskoam.obi

import anaglyph.AnaglyphControlPanel
import filter.FilterControlPanel
import furier.FourierControlPanel
import giis.labs.graphics.custom.ToolBar
import image.{ImageContainerPanel, ImagePanel}
import normalization.NormalizationControlPanel
import recognition.RecognitionControlPanel
import segmentation.SegmentationControlPanel
import swing._
import java.awt.Dimension

/**
 * @author yaskoam
 */
class ApplicationMainFrame extends MainFrame {

    private val defaultWidth = 900
    private val defaultHeight = 600

    private val imageContainerPanel = new ImageContainerPanel {
        addImagePanel(new ImagePanel)
        addImagePanel(new ImagePanel)
    }

    private val normalizationControlPanel = new NormalizationControlPanel(imageContainerPanel)
    private val filterControlPanel = new FilterControlPanel(imageContainerPanel)
    private val fourierControlPanel = new FourierControlPanel(imageContainerPanel)
    private val anaglyphControlPanel = new AnaglyphControlPanel(imageContainerPanel)
    private val segmentationControlPanel = new SegmentationControlPanel(imageContainerPanel)
    private val recognitionControlPanel = new RecognitionControlPanel(imageContainerPanel)

    private val toolBar = new ToolBar
    //toolBar.add(filterControlPanel)
    //toolBar.add(normalizationControlPanel)
    //toolBar.add(fourierControlPanel)
    //toolBar.add(anaglyphControlPanel)
    //toolBar.add(segmentationControlPanel)
    toolBar.add(recognitionControlPanel)

    title = "Filters Application"
    size = new Dimension(defaultWidth, defaultHeight)
    preferredSize = new Dimension(defaultWidth, defaultHeight)
    centerOnScreen()

    contents = new BorderPanel() {
        add(toolBar, BorderPanel.Position.North)
        add(imageContainerPanel, BorderPanel.Position.Center)
    }
}
