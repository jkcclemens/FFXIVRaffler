/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffle

import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class ErrorDialog private constructor() : JDialog() {

    lateinit var contentPane: JPanel
    lateinit var buttonOK: JButton
    lateinit var exceptionLabel: JLabel
    lateinit var messageTextArea: JTextArea

    constructor(title: String, message: String) : this() {
        this.exceptionLabel.text = title
        this.messageTextArea.text = message
    }

    constructor(t: Throwable) : this() {
        this.exceptionLabel.text = t.javaClass.simpleName
        this.exceptionLabel.toolTipText = t.javaClass.name
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        this.messageTextArea.text = t.message + "\n\n" + sw.toString()
    }

    init {
        this.setContentPane(this.contentPane)
        this.isModal = true
        this.getRootPane().defaultButton = this.buttonOK
        this.buttonOK.addActionListener { e -> this.onOK() }
    }

    private fun onOK() {
        this.dispose()
    }
}
