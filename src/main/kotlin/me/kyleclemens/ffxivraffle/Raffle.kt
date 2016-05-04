/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffle

import me.kyleclemens.ffxivraffle.log.LogParser
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane

class Raffle : WithMainPanel {

    lateinit var rafflePanel: JPanel
    lateinit var logTextArea: JTextArea
    lateinit var processButton: JButton
    lateinit var targetField: JTextField
    lateinit var winnersTextPane: JTextPane
    lateinit var selfRollsCheckbox: JCheckBox

    init {
        this.processButton.addActionListener { this.processLog() }
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
            log = log.replace("You roll", "You rolled")
        }
        val parser = LogParser(log)
        val rolls = parser.parse()
        this.winnersTextPane.text = rolls.getWinnersFor(target).joinToString("\n")
    }

    override fun getMainPanel(): JPanel {
        return this.rafflePanel
    }

}
