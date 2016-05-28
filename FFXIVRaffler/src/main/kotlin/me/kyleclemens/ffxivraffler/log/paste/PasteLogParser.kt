/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.log.paste

class PasteLogParser(val rawLog: String) {

    companion object {
        val randomRegex = Regex("(?:\\[\\d{1,2}:\\d{1,2}\\])?Random! ([\\w ']+) rolls a (\\d+).")
        val newlineRegex = Regex("\r?\n")
    }

    fun parse(): Rolls {
        return Rolls(
            this.rawLog
                .split(PasteLogParser.newlineRegex)
                .map { PasteLogParser.randomRegex.find(it) }
                .filterNotNull()
                .associateTo(RaffleMap(hashMapOf<String, Int>())) { it.groupValues[1] to it.groupValues[2].toInt() }
        )
    }

}
