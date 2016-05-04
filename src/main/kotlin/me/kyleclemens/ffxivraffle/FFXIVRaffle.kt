/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffle

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import javax.swing.JMenuBar
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.WindowConstants

class FFXIVRaffle {
    companion object {
        fun cleanUp() {
            if (FFXIVRaffle.getOS() != OS.MAC) {
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
    val os = FFXIVRaffle.getOS()
    if (os == OS.MAC) {
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        val applicationClass = Class.forName("com.apple.eawt.Application")
        val application = applicationClass.getDeclaredMethod("getApplication")(null)
        val listenerClass = Class.forName("com.apple.eawt.AppReOpenedListener")
        val listener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass), InvocationHandler { any, method, arrayOfAnys ->
            if (method.name == "appReOpened") {
                val frame = GUIUtils.openedFrames["FFXIV Raffler"] ?: return@InvocationHandler null
                frame.pack()
                frame.isVisible = true
            }
            return@InvocationHandler null
        })
        applicationClass.getDeclaredMethod("addAppEventListener", Class.forName("com.apple.eawt.AppEventListener"))(application, listener)
        val quitStrategyClass = Class.forName("com.apple.eawt.QuitStrategy")
        applicationClass.getDeclaredMethod("setQuitStrategy", quitStrategyClass)(application, quitStrategyClass.getDeclaredField("CLOSE_ALL_WINDOWS").get(null))
        val quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler")
        val quitHandler = Proxy.newProxyInstance(quitHandlerClass.classLoader, arrayOf(quitHandlerClass), { any, method, arrayOfAnys ->
            FFXIVRaffle.cleanUp()
            System.exit(0)
        })
        applicationClass.getDeclaredMethod("setQuitHandler", quitHandlerClass)(application, quitHandler)
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
                    FFXIVRaffle.cleanUp()
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
