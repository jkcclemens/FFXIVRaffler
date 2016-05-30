/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.log.act

import me.kyleclemens.ffxivloglib.Log
import me.kyleclemens.ffxivloglib.entry.FFXIVEntryType
import me.kyleclemens.ffxivloglib.entry.FFXIVLogEntry
import me.kyleclemens.ffxivloglib.entry.Name
import me.kyleclemens.ffxivloglib.message.parts.NamePart
import me.kyleclemens.ffxivraffler.extensions.toEnumName
import me.kyleclemens.ffxivraffler.gui.Raffle
import me.kyleclemens.ffxivraffler.impl.SeparatorEntry
import me.kyleclemens.ffxivraffler.log.paste.RaffleMap

class ACTRaffle(val raffle: Raffle, val aLog: Log?) {

    val log: Log

    init {
        if (this.aLog == null) {
            throw IllegalStateException("No log file selected|No log file was selected.")
        }
        this.log = this.aLog
    }

    val separatorIndices: List<Pair<Int, Int>>
        get() {
            // Take the inputs from the user and create an entry for them, just to make life easier for us
            val separator = SeparatorEntry(
                sender = this.raffle.senderField.text.trim(),
                message = this.raffle.messageField.text.trim(),
                // Convert the enum type name from a friendly name back to the original name
                type = this.raffle.typeComboBox.selectedItem.toString().toEnumName()
            )
            // Verify the inputs
            this.verifyInputs(separator)
            return this.log.entries
                // Give each entry an index so we can search between the matching entries
                .withIndex()
                // Find all entries that match with the separator
                .filter { this.entryMatchesSeparator(it.value, separator) }
                // Create pairs of indices. [0, 5, 10] would produce [[0, 5], [5, 10]], for example.
                .let { separators ->
                    return@let separators.withIndex().map {
                        // If it's the last index, it doesn't have a next one to pair with, so return null (filtered)
                        if (it.index == separators.size - 1) null
                        // Otherwise, pair it with the next index
                        else it.value.index to separators[it.index + 1].index
                    }
                }
                // Filter out the last index (null)
                .filterNotNull()
        }

    val raffleStats: List<Raffle.RaffleStats>
        get() {
            return this.separatorIndices
                // Turn each index pair into a list of the entries between the indices
                .map {
                    this.log.entries.subList(it.first, it.second + 1)
                }
                // Generate various stats about the entries
                .map {
                    Raffle.RaffleStats(
                        entries = it,
                        rolls = it.count { it.type == FFXIVEntryType.RANDOM },
                        start = it.first().timestamp,
                        end = it.last().timestamp,
                        participants = it.filter { it.type == FFXIVEntryType.RANDOM }.map { this.getNameFromRandomEntry(it) }.filterNotNull().distinct().toSet()
                    )
                }
                // Filter out all raffles with no participants (two separators in a row, etc.)
                .filter { it.participants.size != 0 }
        }

    fun getRolls(stats: Raffle.RaffleStats): Map<String, Int> {
        return stats.entries
            // Find all the random rolls
            .filter { it.type == FFXIVEntryType.RANDOM }
            // Map them to a name and roll
            .map {
                this.getNameFromRandomEntry(it).displayName to it.message.parts.last().displayText.let { it.substring(0, it.length - 1) }.toInt() // FIXME: better way to get roll
            }
            // Convert the pairs to a RaffleMap, which only accepts the first roll
            .associateTo(RaffleMap(hashMapOf<String, Int>())) { it }
    }

    private fun verifyInputs(separator: SeparatorEntry) {
        // Explode the separator into its parts
        val (sender, message, type) = separator
        // Ensure at least one field is filled out
        if (sender.isBlank() && message.isBlank() && type.equals(this.raffle.typeComboBox.getItemAt(0).toString(), ignoreCase = true)) {
            throw IllegalStateException("Separator too vague|One of the separator fields must be specified, but none were.")
        }
    }

    private fun entryMatchesSeparator(entry: FFXIVLogEntry, separator: SeparatorEntry): Boolean {
        // Get the sender of the entry
        val entrySender = entry.sender
        // Explode the separator into its parts
        val (sender, message, type) = separator
        // Start our matching status off as true, then attempt to disprove
        var matches = true
        // Check if the sender matches, if a sender was specified
        if (entrySender != null && sender.isNotBlank()) {
            matches = matches && entrySender.displayName.equals(sender, ignoreCase = true)
        }
        // Check if the message matches, if a message was specified
        if (message.isNotBlank()) {
            matches = matches && entry.message.displayMessage.trim().equals(message, ignoreCase = true)
        }
        // Check if the type matches, if a type was specified
        if (!type.equals(this.raffle.typeComboBox.getItemAt(0).toString(), ignoreCase = true)) {
            matches = matches && entry.type == FFXIVEntryType.valueOf(type)
        }
        return matches
    }

    private fun getNameFromRandomEntry(entry: FFXIVLogEntry, def: Name = Name("You", "You")): Name {
        // Ensure the entry type is a random roll
        if (entry.type != FFXIVEntryType.RANDOM) {
            throw IllegalArgumentException("entry was not a random entry")
        }
        // Get the first part of the message, which should be a NamePart, and then get the name.
        // If this is a self-roll, return def, since self-rolls do not contain NameParts.
        return (entry.message.parts.firstOrNull { it is NamePart } as NamePart?)?.name ?: def
    }

}
