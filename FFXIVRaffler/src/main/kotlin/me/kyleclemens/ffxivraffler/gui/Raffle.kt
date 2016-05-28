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
import me.kyleclemens.ffxivloglib.message.parts.NamePart
import me.kyleclemens.ffxivraffler.extensions.friendlyName
import me.kyleclemens.ffxivraffler.extensions.toEnumName
import me.kyleclemens.ffxivraffler.log.paste.PasteLogParser
import me.kyleclemens.ffxivraffler.log.paste.RaffleMap
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
        get() = GUIUtils.openedFrames["FFXIV Raffler"]!!

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
        // Left-side setup
        with(this.winnersScrollPane) {
            this.viewport.isOpaque = false
            this.border = null
        }
        this.processButton.addActionListener { this.processLog() }
        this.targetField.addKeyListener(object : DefaultKeyListener() {
            override fun keyPressed(e: KeyEvent?) {
                if (e == null || e.keyCode != KeyEvent.VK_ENTER) return
                this@Raffle.processLog()
            }
        })
        Method.values().forEach { this.methodComboBox.addItem(it.optionString) }
        this.methodComboBox.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) return@addItemListener
            when (it.item.toString()) {
                Method.PASTE.optionString -> this.switchMethod(Method.PASTE)
                Method.ACT.optionString -> this.switchMethod(Method.ACT)
                Method.LOG.optionString -> this.switchMethod(Method.LOG)
            }
        }
        // ACT panel setup
        this.typeComboBox.addItem("Any")
        FFXIVEntryType.values()
            .map { it.friendlyName() }
            .forEach { this.typeComboBox.addItem(it) }
        this.networkFileButton.addActionListener {
            val fd = FileDialog(this.frame)
            fd.isMultipleMode = false
            fd.setFilenameFilter { file, s -> s.toLowerCase().endsWith(".log") }
            fd.pack()
            fd.isVisible = true
            this.networkFile = fd.files.firstOrNull()
        }
        // Log panel setup
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
        this.frame.minimumSize = null
        this.frame.pack()
        this.frame.minimumSize = frame.size
    }

    private fun switchMethod(method: Method) {
        setOf<Component>(this.logScrollPane, this.logTextArea, this.logTextAreaSeparator)
            .forEach { this.adjustComponent(it, method != Method.PASTE) }
        this.enableComponent(this.methodPanels[method]!!)
        this.methodPanels.filter { it.key != method }.forEach { this.disableComponent(it.value) }
        val frame = GUIUtils.openedFrames["FFXIV Raffler"]
        if (frame != null) {
            this.repack()
        }
    }

    private fun enableComponent(component: Component) = this.adjustComponent(component, true)

    private fun disableComponent(component: Component) = this.adjustComponent(component, false)

    private fun adjustComponent(component: Component, enabled: Boolean) {
        component.apply { isEnabled = enabled }.apply { this.isVisible = enabled }
    }

    private fun processLog() {
        val target: Int =
            try {
                this.targetField.text.toInt()
            } catch (ex: NumberFormatException) {
                GUIUtils.showErrorDialog("Invalid target number", "The target number was invalid.")
                return
            }
        when (this.methodComboBox.selectedItem.toString()) {
            Method.PASTE.optionString -> {
                var log = this.pasteTextArea.text
                if (this.selfRollsCheckbox.isSelected) {
                    log = log.replace("You roll", "You rolls")
                }
                val parser = PasteLogParser(log)
                val rolls = parser.parse()
                if (rolls.entries.size < 1) {
                    GUIUtils.showErrorDialog("No rolls detected", "There were no valid rolls detected in the log.")
                    return
                }
                this.winnersTextPane.text = rolls.getWinnersFor(target).joinToString("\n")
            }
            Method.ACT.optionString -> {
                val sender = this.senderField.text.trim()
                val message = this.messageField.text.trim()
                val type = this.typeComboBox.selectedItem.toString().toEnumName()
                if (sender.isBlank() && message.isBlank() && type.equals(this.typeComboBox.getItemAt(0).toString(), ignoreCase = true)) {
                    GUIUtils.showErrorDialog("Separator too vague", "One of the separator fields must be specified, but none were.")
                    return
                }
                val log = this.log
                if (log == null) {
                    GUIUtils.showErrorDialog("No log file selected", "No log file was selected.")
                    return
                }
                val separators = log.entries.withIndex().filter {
                    val entrySender = it.value.sender
                    var matches = true
                    if (entrySender != null && sender.isNotBlank()) {
                        matches = matches && entrySender.displayName == sender
                    }
                    if (message.isNotBlank()) {
                        matches = matches && it.value.message.displayMessage.trim().equals(message, ignoreCase = true)
                    }
                    if (!type.equals(this.typeComboBox.getItemAt(0).toString(), ignoreCase = true)) {
                        matches = matches && it.value.type == FFXIVEntryType.valueOf(type)
                    }
                    return@filter matches
                }
                val raffleIndices = separators.withIndex()
                    .map {
                        if (it.index == separators.size - 1) null
                        else it.value.index to separators[it.index + 1].index
                    }
                    .filterNotNull()
                val stats = raffleIndices
                    .map {
                        log.entries.subList(it.first, it.second + 1)
                    }
                    .map {
                        RaffleStats(
                            entries = it,
                            rolls = it.count { it.type == FFXIVEntryType.RANDOM },
                            start = it.first().timestamp,
                            end = it.last().timestamp,
                            participants = it.filter { it.type == FFXIVEntryType.RANDOM }.map { this.getNameFromRandomEntry(it) }.filterNotNull().distinct().toSet()
                        )
                    }
                    .filter { it.participants.size != 0 }
                val raffle = if (stats.size > 1) {
                    val chooser = RaffleChooser(stats.reversed())
                    val dialog = JDialog()
                    dialog.title = "Choose raffle"
                    dialog.modalityType = Dialog.ModalityType.DOCUMENT_MODAL
                    dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
                    dialog.add(chooser.mainPanel)
                    dialog.pack()
                    dialog.isVisible = true
                    chooser.raffleComboBox.selectedItem as RaffleStats
                } else stats[0]
                this.winnersTextPane.text = Rolls(
                    raffle.entries
                        .filter { it.type == FFXIVEntryType.RANDOM }
                        .map {
                            this.getNameFromRandomEntry(it).displayName to it.message.parts.last().displayText.split("\u0003")[1].replace(".", "").toInt() // FIXME
                        }
                        .associateTo(RaffleMap(hashMapOf<String, Int>())) { it }
                        .filter {
                            if (!this.selfRollsCheckbox.isSelected && it.key == "You") false else true
                        }
                ).getWinnersFor(target).joinToString("\n")
            }
            Method.LOG.optionString -> {
                GUIUtils.showErrorDialog("Not yet implemented", "This method is not yet implemented.")
            }
        }
    }

    private fun getNameFromRandomEntry(entry: FFXIVLogEntry): Name {
        if (entry.type != FFXIVEntryType.RANDOM) {
            throw IllegalArgumentException("entry was not a random entry")
        }
        return (entry.message.parts.firstOrNull { it is NamePart } as NamePart?)?.name ?: Name("You", "You")
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
            while (true) {
                if (this.stop) break
                val log = this.raffle.log
                if (log == null) {
                    Thread.sleep(1000L)
                    continue
                }
                if (this.previousLog == null) {
                    this.previousLog = log
                }
                // if the previous log used by this thread is no longer the log the client has requested
                if (this.previousLog !== log) {
                    // clear out the text area
                    this.raffle.logTextArea.text = ""
                    this.previousLog = log
                }
                val entries = if (log is ACTLog) log.newEntries else log.entries
                entries
                    .toList()
                    .map {
                        "[${it.type.friendlyName()}] ${if (it.sender != null) "<${it.sender?.displayName}> " else ""}${it.message}"
                    }
                    .forEach {
                        this.raffle.logTextArea.text = this.raffle.logTextArea.text + "$it\n"
                    }
                if (log !is ACTLog) {
                    this.stop = true
                }
            }
        }

    }

}
