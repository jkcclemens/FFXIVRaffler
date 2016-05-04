/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import java.awt.Dialog
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap
import javax.swing.AbstractAction
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.KeyStroke
import javax.swing.WindowConstants

object GUIUtils {

    val openedFrames = HashMap<String, JFrame>()

    fun createMenuBar(frame: JFrame): JMenuBar {
        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        val closeItem = JMenuItem("Close")
        closeItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().menuShortcutKeyMask)
        closeItem.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (frame.defaultCloseOperation == WindowConstants.HIDE_ON_CLOSE) {
                    frame.isVisible = false
                } else {
                    frame.dispose()
                }
            }
        })
        fileMenu.add(closeItem)
        menuBar.add(fileMenu)
        return menuBar
    }

    fun openWindow(wmp: WithMainPanel, frameName: String, run: (JFrame) -> Unit, onClose: Int): JFrame {
        val frame = JFrame(frameName)
        this.openedFrames[frameName] = frame
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                this@GUIUtils.openedFrames.remove(frameName)
            }
        })
        run(frame)
        frame.contentPane = wmp.mainPanel
        frame.defaultCloseOperation = onClose
        frame.pack()
        frame.isVisible = true
        return frame
    }

    fun openWindow(wmp: WithMainPanel, frameName: String, run: (JFrame) -> Unit): JFrame {
        return this.openWindow(wmp, frameName, run, WindowConstants.DISPOSE_ON_CLOSE)
    }

    fun showDialog(dialog: JDialog): Dialog {
        dialog.pack()
        dialog.isVisible = true
        return dialog
    }

    fun showErrorDialog(t: Throwable) {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        this.showErrorDialog(t.javaClass.simpleName, sw.toString())
    }

    fun showErrorDialog(title: String, message: String) {
        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }

}
