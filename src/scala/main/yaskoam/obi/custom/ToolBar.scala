package giis.labs.graphics.custom

import swing.{Action, SequentialContainer, Component}
import javax.swing.JToolBar


/**
 * Wrapper for the JToolBar class.
 */
class ToolBar extends Component with SequentialContainer.Wrapper {

    override lazy val peer: JToolBar = new JToolBar

    def add(action: Action) {
        peer.add(action.peer)
    }

    def add(component: Component) {
        peer.add(component.peer)
    }
}
