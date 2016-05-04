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
    if (os == OS.MAC) {
        val osxHelper = OSXHelper()
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
    val mainForm = Raffle()
    GUIUtils.openWindow(
        mainForm,
        "FFXIV Raffler",
        { frame ->
            val menuBar = GUIUtils.createMenuBar(frame)
            frame.jMenuBar = menuBar
            if (os == OS.MAC) {
                val applicationClass = Class.forName("com.apple.eawt.Application")
                val application = applicationClass.getDeclaredMethod("getApplication")(null)
                applicationClass.getDeclaredMethod("setDefaultMenuBar", JMenuBar::class.java)(application, menuBar)
            }
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowOpened(e: WindowEvent) {
                    //                    manageIDsForm.playlistIDField.requestFocus()
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
        WindowConstants.HIDE_ON_CLOSE
    )

}
