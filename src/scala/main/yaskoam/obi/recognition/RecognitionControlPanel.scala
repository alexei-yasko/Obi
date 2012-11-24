package yaskoam.obi.recognition

import swing.{Button, FlowPanel}
import swing.event.ButtonClicked
import yaskoam.obi.image.ImageContainerPanel
import boofcv.abst.feature.detect.interest.InterestPointDetector
import boofcv.factory.feature.describe.FactoryDescribeRegionPoint
import boofcv.abst.feature.associate.GeneralAssociation
import boofcv.factory.feature.associate.FactoryAssociation
import boofcv.alg.feature.associate.ScoreAssociateEuclideanSq
import boofcv.struct.feature.{TupleDescQueue, TupleDesc_F64}
import boofcv.struct.image.ImageFloat32
import boofcv.factory.feature.detect.interest.FactoryInterestPoint
import georegression.struct.point.Point2D_F64
import boofcv.struct.FastQueue
import java.awt.image.BufferedImage
import boofcv.core.image.ConvertBufferedImage
import boofcv.gui.feature.AssociationPanel
import boofcv.gui.image.ShowImages
import java.awt.Dimension

/**
 * @author Q-YAA
 */
class RecognitionControlPanel(imageContainerPanel: ImageContainerPanel) extends FlowPanel {

    private val detector: InterestPointDetector[ImageFloat32] = FactoryInterestPoint.fastHessian(0, 2, 200, 2, 9, 4, 4)
    private val describe = FactoryDescribeRegionPoint.surfm(true, classOf[ImageFloat32])
    private val associate: GeneralAssociation[TupleDesc_F64] =
        FactoryAssociation.greedy(new ScoreAssociateEuclideanSq, 2, -1, true)

    private val recognizeButton = new Button("Recognize")

    contents += recognizeButton

    listenTo(recognizeButton)

    reactions += {
        case ButtonClicked(`recognizeButton`) => recognize(
            imageContainerPanel.getSelectedImagePanels(0).getImage, imageContainerPanel.getSelectedImagePanels(1).getImage)
    }

    def describeImage(image: ImageFloat32): (java.util.List[Point2D_F64], FastQueue[TupleDesc_F64]) = {
        // just pointing out that orientation does not need to be passed into the descriptor
        if (describe.requiresOrientation()) {
            throw new RuntimeException("SURF should compute orientation itself!")
        }

        // detect interest points
        detector.detect(image)
        // specify the image to process
        describe.setImage(image)

        val locations = new java.util.ArrayList[Point2D_F64]()
        val descriptions = new TupleDescQueue(describe.getDescriptionLength, true)

        for (i <- 0 until detector.getNumberOfFeatures) {
            // information about hte detected interest point
            val p = detector.getLocation(i)
            val scale = detector.getScale(i)

            // extract the SURF description for this region
            val desc = describe.process(p.x, p.y, 0, scale, null)

            // save everything for processing later on
            descriptions.add(desc)
            locations.add(p)
        }

        (locations, descriptions)
    }

    def recognize(bufferedImage1: BufferedImage, bufferedImage2: BufferedImage) {

        val image1: ImageFloat32 = ConvertBufferedImage.convertFrom(bufferedImage1, null)
        val image2: ImageFloat32 = ConvertBufferedImage.convertFrom(bufferedImage2, null)

        val imageDescription1 = describeImage(image1)
        val imageDescription2 = describeImage(image2)

        // Associate features between the two images
        associate.associate(imageDescription1._2, imageDescription2._2)

        // display the results
        val panel = new AssociationPanel(20)
        panel.setAssociation(imageDescription1._1, imageDescription2._1, associate.getMatches)

        panel.setImages(bufferedImage1, bufferedImage2)
        panel.setPreferredSize(new Dimension(800, 600))
        ShowImages.showWindow(panel, "Associated Features")
    }
}
