/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler

import me.kyleclemens.ffxivraffler.gui.GUIUtils
import me.kyleclemens.ffxivraffler.gui.Raffle
import me.kyleclemens.ffxivraffler.util.OS
import me.kyleclemens.osx.HelperApplication
import me.kyleclemens.osx.HelperQuitResponse
import me.kyleclemens.osx.HelperQuitStrategy
import me.kyleclemens.osx.events.HelperAppReOpenedEvent
import me.kyleclemens.osx.events.HelperQuitEvent
import me.kyleclemens.osx.handlers.HelperQuitHandler
import me.kyleclemens.osx.listeners.HelperAppReOpenedListener
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.WindowConstants

class FFXIVRaffler {
    companion object {
        fun cleanUp() {
            if (FFXIVRaffler.getOS() != OS.OS_X) {
                System.exit(0)
            }
        }

        fun getOS(): OS {
            return with(System.getProperty("os.name").toLowerCase()) {
                when {
                    this.contains("mac") -> OS.OS_X
                    this.contains("linux") -> OS.LINUX
                    this.contains("windows") -> OS.WINDOWS
                    else -> OS.OTHER
                }
            }
        }

    }
}

fun main(args: Array<String>) {
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
    val os = FFXIVRaffler.getOS()
    if (os == OS.OS_X) {
        val osxApplication = HelperApplication()
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        osxApplication.addAppEventListener(object : HelperAppReOpenedListener {
            override fun appReOpened(event: HelperAppReOpenedEvent) {
                val frame = GUIUtils.openedFrames["FFXIV Raffler"] ?: return
                frame.pack()
                frame.isVisible = true
            }
        })
        osxApplication.setQuitStrategy(HelperQuitStrategy.CLOSE_ALL_WINDOWS)
        osxApplication.setQuitHandler(object : HelperQuitHandler {
            override fun handleQuitRequestWith(event: HelperQuitEvent, response: HelperQuitResponse) {
                FFXIVRaffler.cleanUp()
                System.exit(0)
            }
        })
    }
    val raffle = Raffle()
    GUIUtils.openWindow(
        raffle,
        "FFXIV Raffler",
        { frame ->
            val menuBar = GUIUtils.createMenuBar(frame, raffle)
            frame.jMenuBar = menuBar
            if (os == OS.OS_X) {
                val osxApplication = HelperApplication()
                osxApplication.setDefaultMenuBar(menuBar)
            }
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent) {
                    raffle.targetField.requestFocus()
                }

                override fun windowClosed(e: WindowEvent) {
                    FFXIVRaffler.cleanUp()
                }

                override fun windowDeactivated(e: WindowEvent) {
                    if (os == OS.OS_X) {
                        this.windowClosed(e)
                    }
                }
            })
        },
        WindowConstants.HIDE_ON_CLOSE,
        { it.minimumSize = Dimension(it.width, it.height) }
    )

}
