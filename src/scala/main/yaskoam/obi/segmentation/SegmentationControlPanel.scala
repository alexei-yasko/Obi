package yaskoam.obi.segmentation

import yaskoam.obi.image.ImageContainerPanel
import swing.{Label, Slider, Button, FlowPanel}
import swing.event.ButtonClicked
import yaskoam.obi.ImageDialog

/**
 * @author Q-YAA
 */
class SegmentationControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val cunnyDetectorButton = new Button("Cunny detector")
    private val debugCunnyDetectorButton = new Button("Debug cunny detector")

    private val lowThresholdSlider = new Slider {
        min = 0
        max = 255
        value = 20

        labels = Map(0 -> new Label("0"), 50 -> new Label("50"), 100 -> new Label("100"),
            150 -> new Label("150"), 200 -> new Label("200"), 255 -> new Label("255"))
        paintLabels = true
    }
    private val heightThresholdSlider = new Slider {
        min = 0
        max = 255
        value = 80

        labels = Map(0 -> new Label("0"), 50 -> new Label("50"), 100 -> new Label("100"),
            150 -> new Label("150"), 200 -> new Label("200"), 255 -> new Label("255"))
        paintLabels = true
    }

    contents += cunnyDetectorButton
    contents += debugCunnyDetectorButton
    contents += lowThresholdSlider
    contents += heightThresholdSlider

    listenTo(cunnyDetectorButton, debugCunnyDetectorButton)

    reactions += {
        case ButtonClicked(`cunnyDetectorButton`) => applyCunnyDetection()
        case ButtonClicked(`debugCunnyDetectorButton`) => debugCunnyDetection()
    }

    private def applyCunnyDetection() {
        imageContainerPanel.getSelectedImagePanels.foreach(imagePanel => {
            val resultImage =
                new CunnyDetector(lowThresholdSlider.value + 1, heightThresholdSlider.value + 1).detect(imagePanel.getImage)

            val imageDialog = new ImageDialog(resultImage, imageContainerPanel)
            imageDialog.open()
        })
    }

    private def debugCunnyDetection() {
        imageContainerPanel.getSelectedImagePanels.foreach(imagePanel => {
            val cannyDetector = new CunnyDetector(lowThresholdSlider.value + 1, heightThresholdSlider.value + 1)
            cannyDetector.detect(imagePanel.getImage)

            for (image <- cannyDetector.getHistory) {
                val imageDialog = new ImageDialog(image, imageContainerPanel)
                imageDialog.open()
            }
        })
    }
}
