/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.gui

import me.kyleclemens.ffxivloglib.ACTLog
import me.kyleclemens.ffxivloglib.FFXIVLog
import me.kyleclemens.ffxivloglib.Log
import me.kyleclemens.ffxivloglib.entry.FFXIVEntryType
import me.kyleclemens.ffxivloglib.entry.FFXIVLogEntry
import me.kyleclemens.ffxivloglib.entry.Name
import me.kyleclemens.ffxivraffler.extensions.friendlyName
import me.kyleclemens.ffxivraffler.log.act.ACTRaffle
import me.kyleclemens.ffxivraffler.log.paste.PasteLogParser
import me.kyleclemens.ffxivraffler.log.paste.Rolls
import me.kyleclemens.ffxivraffler.util.listeners.DefaultKeyListener
import java.awt.Component
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.event.ItemEvent
import java.awt.event.KeyEvent
import java.io.File
import java.util.Date
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.SwingUtilities

class Raffle : WithMainPanel {

    override val mainPanel: JPanel
        get() = this.rafflePanel
    lateinit var rafflePanel: JPanel
    lateinit var logScrollPane: JScrollPane
    lateinit var logTextArea: JTextArea
    lateinit var logTextAreaSeparator: JSeparator
    lateinit var processButton: JButton
    lateinit var targetField: JTextField
    lateinit var winnersScrollPane: JScrollPane
    lateinit var winnersTextPane: JTextPane
    lateinit var selfRollsCheckbox: JCheckBox
    lateinit var methodComboBox: JComboBox<String>
    lateinit var pastePanel: JPanel
    lateinit var actPanel: JPanel
    lateinit var logPanel: JPanel
    lateinit var pasteScrollPane: JScrollPane
    lateinit var pasteTextArea: JTextArea
    lateinit var networkFileLabel: JLabel
    lateinit var networkFileButton: JButton
    lateinit var senderField: JTextField
    lateinit var messageField: JTextField
    lateinit var typeComboBox: JComboBox<String>
    lateinit var logFileLabel: JLabel
    lateinit var logFileButton: JButton
    private val methodPanels: Map<Method, JPanel>
    private var _networkFile: File? = null
    private var networkFile: File?
        set(value) {
            if (value == null) return
            this.networkFileLabel.text = value.name
            this.repack()
            this._networkFile = value
            this.log = ACTLog(value, live = true)
        }
        get() = this._networkFile
    private var _logFile: File? = null
    private var logFile: File?
        set(value) {
            if (value == null) return
            this.logFileLabel.text = value.name
            this.repack()
            this._logFile = value
            this.log = FFXIVLog(value)
        }
        get() = this._logFile
    private var _log: Log? = null
    private var log: Log?
        set(value) {
            val log = this._log
            if (log is ACTLog) {
                log.stopThread()
            }
            this._log = value
        }
        get() = this._log
    val updaterThread: LogUpdaterThread

    private val frame: JFrame
        get() = SwingUtilities.getWindowAncestor(this.mainPanel) as JFrame

    private enum class Method(val optionString: String) {
        PASTE("Paste (Manual)"),
        ACT("ACT Network Log (Live)"),
        LOG("FFXIV Chat Log (Overflow)")
    }

    init {
        // Miscellaneous setup
        this.methodPanels = mapOf(
            Method.PASTE to this.pastePanel,
            Method.LOG to this.logPanel,
            Method.ACT to this.actPanel
        )

        // Bottom setup

        // Remove the border from the bottom scroll pane
        this.logScrollPane.border = null

        // Left-side setup

        // Make the scroll pane transparent
        with(this.winnersScrollPane) {
            this.viewport.isOpaque = false
            this.border = null
        }
        // Make the Process button process the log
        this.processButton.addActionListener { this.processLog() }
        // Make the target field process the log if Enter is pushed
        this.targetField.addKeyListener(object : DefaultKeyListener() {
            override fun keyPressed(e: KeyEvent?) {
                if (e == null || e.keyCode != KeyEvent.VK_ENTER) return
                this@Raffle.processLog()
            }
        })
        // Add each method to the combo box
        Method.values().forEach { this.methodComboBox.addItem(it.optionString) }
        // Have clicking on a method switch to that method
        this.methodComboBox.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) return@addItemListener
            when (it.item.toString()) {
                Method.PASTE.optionString -> this.switchMethod(Method.PASTE)
                Method.ACT.optionString -> this.switchMethod(Method.ACT)
                Method.LOG.optionString -> this.switchMethod(Method.LOG)
            }
        }

        // Paste panel setup

        // Remove the border from the pasted log scroll pane
        this.pasteScrollPane.border = null

        // ACT panel setup

        // Add an Any option to the type combo box (default)
        this.typeComboBox.addItem("Any")
        // Add each entry type to the type combo box, using a friendly name for each
        FFXIVEntryType.values()
            .map { it.friendlyName() }
            .forEach { this.typeComboBox.addItem(it) }
        // Create a log file chooser when clicking on the choose log button for the ACT method
        this.networkFileButton.addActionListener {
            val fd = FileDialog(this.frame)
            fd.isMultipleMode = false
            fd.setFilenameFilter { file, s -> s.toLowerCase().endsWith(".log") }
            fd.pack()
            fd.isVisible = true
            this.networkFile = fd.files.firstOrNull()
        }

        // Log panel setup

        // Create a log file chooser when clicking on the choose log button for the log method
        this.logFileButton.addActionListener {
            val fd = FileDialog(this.frame)
            fd.isMultipleMode = false
            fd.setFilenameFilter { file, s -> s.toLowerCase().endsWith(".log") }
            fd.pack()
            fd.isVisible = true
            this.networkFile = fd.files.firstOrNull()
        }

        // Start log updater thread
        this.updaterThread = LogUpdaterThread(this).apply { this.start() }
    }

    private fun repack() {
        // Remove a minimum size so pack can shrink the window
        this.frame.minimumSize = null
        // Pack everything in
        this.frame.pack()
        // Set the minimum size to the new shrunk size
        this.frame.minimumSize = frame.size
    }

    private fun switchMethod(method: Method) {
        // Create a set of components to show if the method isn't paste and show/hide them if necessary
        setOf<Component>(this.logScrollPane, this.logTextArea, this.logTextAreaSeparator)
            .forEach { this.adjustComponent(it, method != Method.PASTE) }
        // Enable the panel for this method
        this.enableComponent(this.methodPanels[method]!!)
        // Disable the panels for other methods
        this.methodPanels.filter { it.key != method }.forEach { this.disableComponent(it.value) }
        // Repack the window, since the size of each panel is different
        this.repack()
    }

    private fun enableComponent(component: Component) = this.adjustComponent(component, true)

    private fun disableComponent(component: Component) = this.adjustComponent(component, false)

    private fun adjustComponent(component: Component, enabled: Boolean) {
        component.apply { isEnabled = enabled }.apply { this.isVisible = enabled }
    }

    private val target: Int?
        get() {
            return try {
                this.targetField.text.toInt()
            } catch (ex: NumberFormatException) {
                GUIUtils.showErrorDialog("Invalid target number", "The target number was invalid.")
                null
            }
        }

    private fun processLog() {
        // Get the target value or return
        val target = this.target ?: return
        // Call the function for the selected method
        when (this.methodComboBox.selectedItem.toString()) {
            Method.PASTE.optionString -> this.pasteProcess(target)
            Method.ACT.optionString -> this.actProcess(target)
            Method.LOG.optionString -> {
                GUIUtils.showErrorDialog("Not yet implemented", "This method is not yet implemented.")
            }
        }
    }

    private fun pasteProcess(target: Int) {
        // Get the pasted log
        var log = this.pasteTextArea.text
        // If self-rolls are allowed, internally change the format of any self-rolls to match the regex
        if (this.selfRollsCheckbox.isSelected) {
            log = log.replace("You roll", "You rolls")
        }
        // Create the parser
        val parser = PasteLogParser(log)
        // Parse the log
        val rolls = parser.parse()
        // Check for entries
        if (rolls.entries.size < 1) {
            // Display an error and back out if no entries were found
            GUIUtils.showErrorDialog("No rolls detected", "There were no valid rolls detected in the log.")
            return
        }
        // Display all winners
        this.winnersTextPane.text = rolls.getWinnersFor(target).joinToString("\n")
    }

    private fun actProcess(target: Int) {
        // Make the ACTRaffle object to handle the raffles
        val act: ACTRaffle
        val stats = try {
            act = ACTRaffle(this, this.log)
            // Get the stats about each raffle
            act.raffleStats
        } catch(ex: IllegalStateException) {
            // If ACTRaffle throws any exceptions, display them and back out
            val message = (ex.message ?: "${ex.javaClass.simpleName}|${ex.javaClass.name}").split("|")
            GUIUtils.showErrorDialog(message[0], message[1])
            return
        }
        // Ask the user which raffle they want to process, if multiple were found. If only one was found, use it and
        // don't bother the user.
        val raffle = if (stats.size > 1) {
            // Create the dialog with the raffle chooser interface
            // Reverse the information about the raffles so the latest raffle if first in the list.
            val chooser = RaffleChooser(stats.reversed())
            val dialog = JDialog()
            dialog.title = "Choose raffle"
            // Set the modality so it blocks the code until the modal is closed
            dialog.modalityType = Dialog.ModalityType.DOCUMENT_MODAL
            // Prevent user from closing the dialog normally
            dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
            dialog.add(chooser.mainPanel)
            dialog.pack()
            // Display the dialog
            dialog.isVisible = true
            // Once the dialog is closed, get the selected raffle
            chooser.raffleComboBox.selectedItem as RaffleStats
        } else stats[0]
        // Display all winners
        this.winnersTextPane.text = Rolls(
            act.getRolls(raffle).filter {
                // Filter out self-rolls if necessary
                if (!this.selfRollsCheckbox.isSelected && it.key == "You") false else true
            }
        ).getWinnersFor(target).joinToString("\n")
    }

    data class RaffleStats(val entries: List<FFXIVLogEntry>, val rolls: Int, val start: Date, val end: Date, val participants: Set<Name>) {

        override fun toString(): String {
            return "Raffle at $start with $rolls roll${if (rolls == 1) "" else "s"} and ${participants.size} participant${if (participants.size == 1) "" else "s"}"
        }

    }

    class LogUpdaterThread internal constructor(val raffle: Raffle) : Thread() {

        var stop: Boolean = false
        private var previousLog: Log? = null

        override fun run() {
            // Run forever until this.stop becomes true
            while (true) {
                if (this.stop) break
                // Get the log
                val log = this.raffle.log
                // If the log is null, check again in one second
                if (log == null) {
                    // Sleep the thread for a second and continue
                    Thread.sleep(1000L)
                    continue
                }
                // If there wasn't a previous log, set it to the current log
                if (this.previousLog == null) {
                    this.previousLog = log
                }
                // If the previous log used by this thread is no longer the log the client has requested
                if (this.previousLog !== log) {
                    // Clear out the text area
                    this.raffle.logTextArea.text = ""
                    // Set the previous log to the current log
                    this.previousLog = log
                }
                // Get the entries, but only new entries if the log is an ACTLog
                val entries = if (log is ACTLog) log.newEntries else log.entries
                entries
                    // Create a copy of the list
                    .toList()
                    // Map it to a human-readable format
                    .map {
                        "[${it.type.friendlyName()}] ${if (it.sender != null) "<${it.sender?.displayName}> " else ""}${it.message}"
                    }
                    // Add each entry to the log area
                    .forEach {
                        this.raffle.logTextArea.text = this.raffle.logTextArea.text + "$it\n"
                    }
                // If the log isn't an ACTLog, which means it cannot have new entries, stop the thread.
                // FIXME: This will totally break displaying other logs after using a non-ACTLog
                if (log !is ACTLog) {
                    this.stop = true
                }
            }
        }

    }

}
