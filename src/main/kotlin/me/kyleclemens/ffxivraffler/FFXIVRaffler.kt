/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler

import me.kyleclemens.ffxivraffler.gui.GUIUtils
import me.kyleclemens.ffxivraffler.gui.Raffle
import me.kyleclemens.ffxivraffler.util.OS
import me.kyleclemens.ffxivraffler.util.os.OSXHelper
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JMenuBar
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.WindowConstants

class FFXIVRaffler {
    companion object {
        fun cleanUp() {
            if (FFXIVRaffler.getOS() != OS.MAC) {
                System.exit(0)
            }
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
    val osxHelper = OSXHelper()
    if (os == OS.MAC) {
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        val listener = osxHelper.proxyClass("com.apple.eawt.AppReOpenedListener") { any, method, arrayOfAnys ->
            if (method.name == "appReOpened") {
                val frame = GUIUtils.openedFrames["FFXIV Raffler"] ?: return@proxyClass null
                frame.pack()
                frame.isVisible = true
            }
            return@proxyClass null
        }
        osxHelper.callMethod("addAppEventListener", listOf("com.apple.eawt.AppEventListener"), listener)
        val quitStrategyClass = "com.apple.eawt.QuitStrategy"
        osxHelper.callMethod("setQuitStrategy", listOf(quitStrategyClass), Class.forName(quitStrategyClass).getDeclaredField("CLOSE_ALL_WINDOWS").get(null))
        val quitHandlerClass = "com.apple.eawt.QuitHandler"
        val quitHandler = osxHelper.proxyClass(quitHandlerClass) { any, method, arrayOfAnys ->
            FFXIVRaffler.cleanUp()
            System.exit(0)
        }
        osxHelper.callMethod("setQuitHandler", listOf(quitHandlerClass), quitHandler)
    }
    val raffle = Raffle()
    GUIUtils.openWindow(
        raffle,
        "FFXIV Raffler",
        { frame ->
            val menuBar = GUIUtils.createMenuBar(frame, raffle)
            frame.jMenuBar = menuBar
            if (os == OS.MAC) {
                osxHelper.callMethod("setDefaultMenuBar", listOf(JMenuBar::class.java.canonicalName), menuBar)
            }
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent) {
                    raffle.targetField.requestFocus()
                }

                override fun windowClosed(e: WindowEvent) {
                    FFXIVRaffler.cleanUp()
                }

                override fun windowDeactivated(e: WindowEvent) {
                    if (os == OS.MAC) {
                        this.windowClosed(e)
                    }
                }
            })
        },
        WindowConstants.HIDE_ON_CLOSE,
        { it.minimumSize = Dimension(it.width, it.height) }
    )

}
