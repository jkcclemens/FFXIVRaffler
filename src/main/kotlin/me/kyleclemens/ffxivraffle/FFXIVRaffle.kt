/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffle

import com.apple.eawt.AppReOpenedListener
import com.apple.eawt.Application
import com.apple.eawt.QuitStrategy
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.WindowConstants

class FFXIVRaffle {
    companion object {
        fun cleanUp() {
            // TODO
        }

        fun getOS(): OS {
            return with(System.getProperty("os.name").toLowerCase()) {
                when {
                    this.contains("mac") -> OS.MAC
                    this.contains("linux") -> OS.LINUX
                    this.contains("windows") -> OS.WINDOWS
                    else -> OS.OTHER
                }
            }
        }

    }
}

fun main(args: Array<String>) {
    val os = FFXIVRaffle.getOS()
    if (os == OS.MAC) {
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: UnsupportedLookAndFeelException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        val application = Application.getApplication()
        application.addAppEventListener(AppReOpenedListener {
            val frame = GUIUtils.getOpenedFrames()["YouTube Playlist Manager"] ?: return@AppReOpenedListener
            frame.pack()
            frame.isVisible = true
        })
        //    val manageIDsForm = ManageIDsForm()
        if (os == OS.MAC) {
            val app = Application.getApplication()
            app.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS)
            app.setQuitHandler { event, response ->
                FFXIVRaffle.cleanUp()
                System.exit(0)
            }
        }
        val mainForm = Raffle()
        GUIUtils.openWindow(
            mainForm,
            "FFXIV Raffler",
            { frame ->
                val menuBar = GUIUtils.createMenuBar(frame)
                frame.jMenuBar = menuBar
                if (os == OS.MAC) {
                    val app = Application.getApplication()
                    app.setDefaultMenuBar(menuBar)
                }
                frame.addWindowListener(object : WindowAdapter() {
                    override fun windowOpened(e: WindowEvent) {
                        //                    manageIDsForm.playlistIDField.requestFocus()
                    }

                    override fun windowClosed(e: WindowEvent) {
                        FFXIVRaffle.cleanUp()
                    }

                    override fun windowDeactivated(e: WindowEvent) {
                        this.windowClosed(e)
                    }
                })
            },
            WindowConstants.HIDE_ON_CLOSE
        )
    }

}
