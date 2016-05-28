/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import java.util.Date
import java.util.EnumSet
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class RaffleChooser(val raffles: List<Raffle.RaffleStats>) : WithMainPanel {
    override val mainPanel: JPanel
        get() = this.raffleChooserPanel
    lateinit var raffleChooserPanel: JPanel
    lateinit var raffleComboBox: JComboBox<Raffle.RaffleStats>
    lateinit var durationLabel: JLabel
    lateinit var startLabel: JLabel
    lateinit var endLabel: JLabel
    lateinit var rollsLabel: JLabel
    lateinit var participantsScrollPane: JScrollPane
    lateinit var participantsTextArea: JTextArea
    lateinit var chooseRaffleButton: JButton

    init {
        with(this.participantsScrollPane) {
            this.border = null
            this.viewport.isOpaque = false
        }
        this.raffles.forEach { this.raffleComboBox.addItem(it) }
        this.raffleComboBox.addItemListener {
            this.updateStats()
        }
        this.chooseRaffleButton.addActionListener {
            SwingUtilities.getWindowAncestor(this.mainPanel).dispose()
        }
        this.updateStats()
    }

    private fun updateStats() {
        val raffle = this.raffleComboBox.selectedItem as Raffle.RaffleStats
        this.durationLabel.text = this.computeDiff(raffle.start, raffle.end)
        this.startLabel.text = raffle.start.toString()
        this.endLabel.text = raffle.end.toString()
        this.rollsLabel.text = raffle.rolls.toString()
        this.participantsTextArea.text = raffle.participants.map { it.displayName }.joinToString("\n")
    }

    private fun computeDiff(date1: Date, date2: Date): String {
        val diffInMillis = date2.time - date1.time
        val units = EnumSet.allOf(TimeUnit::class.java).toList().reversed()
        var millisRest = diffInMillis
        return units
            .associate { unit ->
                val diff = unit.convert(millisRest, TimeUnit.MILLISECONDS)
                val diffInMillisForUnit = unit.toMillis(diff)
                millisRest -= diffInMillisForUnit
                unit to diff
            }
            .map {
                if (it.value == 0L) null
                else {
                    "${it.value} ${it.key.name.toLowerCase().let { name -> name.substring(0, name.length - if (it.value == 1L) 1 else 0) }}"
                }
            }
            .filterNotNull()
            .joinToString(" ")
    }
}
