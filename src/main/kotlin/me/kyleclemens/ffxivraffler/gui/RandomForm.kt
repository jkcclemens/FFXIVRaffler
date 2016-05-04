/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import me.kyleclemens.ffxivraffler.extensions.toStackTraceString
import java.security.SecureRandom
import java.util.Random
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class RandomForm(private val raffle: Raffle) : WithMainPanel {

    override val mainPanel: JPanel
        get() = this.randomPanel
    private val random: Random = SecureRandom.getInstanceStrong()
    lateinit var randomPanel: JPanel
    lateinit var minField: JTextField
    lateinit var maxField: JTextField
    lateinit var rollAndInsertButton: JButton
    lateinit var rollAndProcessButton: JButton

    init {
        this.rollAndInsertButton.addActionListener { this.rollAndInsert() }
        this.rollAndProcessButton.addActionListener {
            if (this.rollAndInsert()) {
                this.raffle.processButton.doClick()
            }
        }
    }

    private fun getMinBound() = this.minField.text.toInt()

    private fun getMaxBound() = this.maxField.text.toInt()

    private fun getRandomNumber(): Int {
        val max = this.getMaxBound()
        val min = this.getMinBound()
        if (min > max) {
            throw IllegalArgumentException("The minimum bound cannot be larger than the maximum bound.")
        }
        return this.random.nextInt(max - min + 1) + min
    }

    private fun rollAndInsert(): Boolean {
        val number: Int
        try {
            number = this.getRandomNumber()
        } catch(ex: NumberFormatException) {
            GUIUtils.showErrorDialog("Invalid minimum or maximum bound", "The minimum or maximum bound was an invalid number.")
            return false
        } catch(ex: IllegalArgumentException) {
            GUIUtils.showErrorDialog("Illegal argument", ex.message ?: ex.toStackTraceString())
            return false
        }
        this.raffle.targetField.text = number.toString()
        return true

    }

}
