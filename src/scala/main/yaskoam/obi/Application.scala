package yaskoam.obi

import swing.SimpleSwingApplication
import javax.swing.UIManager

/**
 * @author yaskoam
 */
object Application extends SimpleSwingApplication {
    UIManager.setLookAndFeel("org.jb2011.lnf.beautyeye.BeautyEyeLookAndFeelCross")
    UIManager.put("RootPane.setupButtonVisible", false)

    def top = new ApplicationMainFrame
}