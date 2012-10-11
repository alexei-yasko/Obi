package yaskoam.obi.filter

import swing.{Button, FlowPanel}
import swing.event.ButtonClicked
import yaskoam.obi.image.{ImageContainerPanel, ImagePanel}

/**
 * @author Q-YAA
 */
class FilterControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val smoothFilterButton: Button = new Button("Smooth filter")
    private val edgeDetectionFilterButton: Button = new Button("Edge detection filter")
    private val boofCVMedianFilterButton: Button = new Button("BloofCV median filter")
    private val embossFilterButton = new Button("Emboss filter")
    private val acutanceFilterButton = new Button("Acutance filter")
    private val invertFilterButton = new Button("Invert filter")

    contents += smoothFilterButton
    contents += edgeDetectionFilterButton
    contents += boofCVMedianFilterButton
    contents += embossFilterButton
    contents += acutanceFilterButton
    contents += invertFilterButton

    listenTo(
        smoothFilterButton,
        edgeDetectionFilterButton,
        boofCVMedianFilterButton,
        embossFilterButton,
        acutanceFilterButton,
        invertFilterButton
    )

    reactions += {
        case ButtonClicked(`smoothFilterButton`) =>
            filterImages(filterImageWithMatrix(FilterUtils.SMOOTH_FILTER_MATRIX_3x3, _))

        case ButtonClicked(`edgeDetectionFilterButton`) =>
            filterImages(filterImageWithMatrix(FilterUtils.EDGE_DETECTION_FILTER_MATRIX_3x3, _))

        case ButtonClicked(`boofCVMedianFilterButton`) =>
            filterImages(boofCVMedianFilter(_))

        case ButtonClicked(`embossFilterButton`) =>
            filterImages(filterImageWithMatrix(FilterUtils.EMBOSS_FILTER_MATRIX_3x3, _))

        case ButtonClicked(`acutanceFilterButton`) =>
            filterImages(filterImageWithMatrix(FilterUtils.ACUTANCE_FILTER_MATRIX_3x3, _))

        case ButtonClicked(`invertFilterButton`) =>
            filterImages(boofCVInvert(_))
    }

    def filterImages(function: ImagePanel => Unit) {
        imageContainerPanel.getSelectedImagePanels.foreach(imagePanel => function(imagePanel))
    }

    def filterImageWithMatrix(filterMatrix: FilterMatrix, imagePanel: ImagePanel) {
        val filteredImage = FilterUtils.filterImageWithMatrix(imagePanel.getImage, filterMatrix)
        imagePanel.reloadImage(filteredImage)
    }

    def boofCVMedianFilter(imagePanel: ImagePanel) {
        val filterRadius = 3
        val filteredImage = FilterUtils.boofCVMedianFilter(imagePanel.getImage, filterRadius)
        imagePanel.reloadImage(filteredImage)
    }

    def boofCVInvert(imagePanel: ImagePanel) {
        val filteredImage = FilterUtils.boofCVInvertFilter(imagePanel.getImage, 255)
        imagePanel.reloadImage(filteredImage)
    }
}
