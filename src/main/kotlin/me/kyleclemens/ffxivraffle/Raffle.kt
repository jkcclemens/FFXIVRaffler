/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffle

import me.kyleclemens.ffxivraffle.log.paste.LogParser
import me.kyleclemens.ffxivraffle.util.DefaultKeyListener
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane

class Raffle : WithMainPanel {

    override val mainPanel: JPanel
        get() = this.rafflePanel
    lateinit var rafflePanel: JPanel
    lateinit var logTextArea: JTextArea
    lateinit var processButton: JButton
    lateinit var targetField: JTextField
    lateinit var winnersTextPane: JTextPane
    lateinit var selfRollsCheckbox: JCheckBox

    init {
        this.processButton.addActionListener { this.processLog() }
        this.targetField.addKeyListener(object : DefaultKeyListener() {
            override fun keyPressed(e: KeyEvent?) {
                if (e == null || e.keyCode != KeyEvent.VK_ENTER) return
                this@Raffle.processLog()
            }
        })
        this.winnersTextPane.isOpaque = false
    }

    private fun processLog() {
        val target: Int =
            try {
                this.targetField.text.toInt()
            } catch (ex: NumberFormatException) {
                GUIUtils.showErrorDialog("Invalid target number", "The target number was invalid.")
                return
            }
        var log = this.logTextArea.text
        if (this.selfRollsCheckbox.isSelected) {
            log = log.replace("You roll", "You rolls")
        }
        val parser = LogParser(log)
        val rolls = parser.parse()
        if (rolls.entries.size < 1) {
            GUIUtils.showErrorDialog("No rolls detected", "There were no valid rolls detected in the log.")
            return
        }
        this.winnersTextPane.text = rolls.getWinnersFor(target).joinToString("\n")
    }

}
