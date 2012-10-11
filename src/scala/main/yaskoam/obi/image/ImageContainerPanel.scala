package yaskoam.obi.image

import swing.{Orientation, BoxPanel}

/**
 * @author Q-YAA
 */
class ImageContainerPanel extends BoxPanel(Orientation.Horizontal) {

    private var imagePanelList = List[ImagePanel]()

    def addImagePanel(imagePanel: ImagePanel) {
        imagePanelList = imagePanel :: imagePanelList
        contents += imagePanel
    }

    def getSelectedImagePanels: List[ImagePanel] = imagePanelList.filter(imagePanel => imagePanel.isImagePanelSelected)
}
