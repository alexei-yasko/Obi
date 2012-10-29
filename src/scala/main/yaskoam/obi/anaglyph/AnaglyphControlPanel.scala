package yaskoam.obi.anaglyph

import yaskoam.obi.image.ImageContainerPanel
import swing.{FlowPanel, Button}
import swing.event.ButtonClicked
import yaskoam.obi.ImageDialog

/**
 * @author Q-YAA
 */
class AnaglyphControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val createAnaglyphButton = new Button("Create anaglyph")

    contents += createAnaglyphButton

    listenTo(createAnaglyphButton)
    reactions += {
        case ButtonClicked(`createAnaglyphButton`) => createAnaglyph()
    }

    private def createAnaglyph() {

        if (imageContainerPanel.getSelectedImagePanels.length == 2) {
            val leftImage = imageContainerPanel.getSelectedImagePanels.reverse.head.getImage
            val rightImage = imageContainerPanel.getSelectedImagePanels.head.getImage

            val resultImageDialog = new ImageDialog(leftImage, this)
            resultImageDialog.open()
        }
    }
}
