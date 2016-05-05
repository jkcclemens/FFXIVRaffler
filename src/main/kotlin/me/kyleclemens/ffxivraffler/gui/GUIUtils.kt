/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import me.kyleclemens.ffxivraffler.extensions.toStackTraceString
import me.kyleclemens.ffxivraffler.util.listeners.DockedComponentListener
import java.awt.Dialog
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.HashMap
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

object GUIUtils {

    private var _defaultMenuBar: JMenuBar? = null
    val defaultMenuBar: JMenuBar
        get() {
            if (this._defaultMenuBar == null) {
                throw IllegalStateException("Default menu bar not yet initialized")
            }
            return this._defaultMenuBar ?: throw IllegalStateException("Another thread set to null")
        }
    val openedFrames = HashMap<String, JFrame>()

    fun createMenuBar(frame: JFrame, raffle: Raffle): JMenuBar {
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
        val randomNumberGeneratorItem = JMenuItem("Random number generator")
        randomNumberGeneratorItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().menuShortcutKeyMask)
        randomNumberGeneratorItem.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                this@GUIUtils.createRandomNumberWindow(raffle)
            }
        })
        fileMenu.add(closeItem)
        fileMenu.add(randomNumberGeneratorItem)
        menuBar.add(fileMenu)
        this._defaultMenuBar = menuBar
        return menuBar
    }

    fun createRandomNumberWindow(raffle: Raffle) {
        val randomForm = RandomForm(raffle)
        val raffleFrame = SwingUtilities.getWindowAncestor(raffle.mainPanel) as JFrame
        if ("Random number generator" in this@GUIUtils.openedFrames) {
            this@GUIUtils.openedFrames["Random number generator"]!!.toFront()
            return
        }
        GUIUtils.openWindow(
            randomForm,
            "Random number generator",
            beforeVisible = { it.location = Point(raffleFrame.x + raffleFrame.width, raffleFrame.y) },
            run = { frame ->
                frame.isResizable = false
                val raffleComponentListener = object : DockedComponentListener() {
                    override fun componentMovedOrResized(e: ComponentEvent) {
                        frame.location = Point(raffleFrame.x + raffleFrame.width, raffleFrame.y)
                    }
                }
                raffleFrame.addComponentListener(raffleComponentListener)
                frame.addWindowListener(object : WindowAdapter() {
                    override fun windowOpened(e: WindowEvent) {
                        randomForm.rollAndInsertButton.requestFocus()
                    }

                    override fun windowClosed(e: WindowEvent?) {
                        raffleFrame.removeComponentListener(raffleComponentListener)
                    }
                })
                frame.addComponentListener(object : DockedComponentListener() {
                    override fun componentMovedOrResized(e: ComponentEvent) {
                        raffleFrame.location = Point(frame.x - raffleFrame.width, frame.y)
                    }
                })
                val pane = frame.rootPane
                pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Escape");
                pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().menuShortcutKeyMask), "CtrlR")
                pane.actionMap.put("Escape", object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        if (frame.defaultCloseOperation == WindowConstants.HIDE_ON_CLOSE) {
                            frame.isVisible = false
                        } else {
                            frame.dispose()
                        }
                    }
                })
                pane.actionMap.put("CtrlR", object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        raffleFrame.toFront()
                    }

                })
            })
    }

    fun openWindow(wmp: WithMainPanel, frameName: String, run: (JFrame) -> Unit, onClose: Int, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
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
        if (beforeVisible != null) {
            beforeVisible(frame)
        }
        frame.isVisible = true
        return frame
    }

    fun openWindow(wmp: WithMainPanel, frameName: String, run: (JFrame) -> Unit, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
        return this.openWindow(wmp, frameName, run, WindowConstants.DISPOSE_ON_CLOSE, beforeVisible)
    }

    fun showDialog(dialog: JDialog): Dialog {
        dialog.pack()
        dialog.isVisible = true
        return dialog
    }

    fun showErrorDialog(t: Throwable) {
        this.showErrorDialog(t.javaClass.simpleName, t.toStackTraceString())
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
