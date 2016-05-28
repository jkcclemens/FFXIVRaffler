/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.extensions

import me.kyleclemens.ffxivraffler.gui.WithMainPanel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JFrame
import javax.swing.WindowConstants

fun Throwable.toStackTraceString() = StringWriter().apply { this@toStackTraceString.printStackTrace(PrintWriter(this)) }.toString()

fun <E : Enum<E>> Enum<E>.friendlyName() = this.name
    .split("_")
    .map {
        if (it.length > 1) it[0].toUpperCase() + it.substring(1).toLowerCase() else it
    }
    .joinToString(" ")

fun String.toEnumName() = this.toUpperCase().replace(' ', '_')

val openedFrames: MutableMap<String, JFrame> = hashMapOf()

fun WithMainPanel.openAsFrame(name: String, run: (JFrame) -> Unit, onClose: Int, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
    val frame = JFrame(name)
    openedFrames[name] = frame
    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent?) {
            openedFrames.remove(name)
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

fun WithMainPanel.openAsFrame(name: String, run: (JFrame) -> Unit, beforeVisible: ((JFrame) -> Unit)? = null): JFrame {
    return this.openAsFrame(name, run, WindowConstants.DISPOSE_ON_CLOSE, beforeVisible)
}
