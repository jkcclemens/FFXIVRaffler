/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

interface WithMainPanel {

    companion object {
        val openedFrames: MutableMap<String, JFrame> = hashMapOf()
    }

    val mainPanel: JPanel

    fun openAsFrame(name: String, run: (JFrame) -> Unit, onClose: Int, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
        val frame = JFrame(name)
        WithMainPanel.openedFrames[name] = frame
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                WithMainPanel.openedFrames.remove(name)
            }
        })
        run(frame)
        frame.contentPane = this.mainPanel
        frame.defaultCloseOperation = onClose
        frame.pack()
        if (beforeVisible != null) {
            beforeVisible(frame)
        }
        frame.isVisible = true
        return frame
    }

    fun openAsFrame(name: String, run: (JFrame) -> Unit, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
        return this.openAsFrame(name, run, WindowConstants.DISPOSE_ON_CLOSE, beforeVisible)
    }

}
