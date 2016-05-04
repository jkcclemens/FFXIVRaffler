/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.log.paste

import java.util.HashMap

class Rolls(val entries: Map<String, Int>) {

    fun getWinnersFor(target: Int): Set<String> {
        val distances = this.entries.mapValues { Math.abs(it.value - target) } as HashMap
        val winners = hashSetOf<String>()
        var firstDistance: Int? = null
        do {
            val min = distances.minBy { it.value } ?: return winners
            val thisDistance = min.value
            if (firstDistance == null) {
                firstDistance = thisDistance
            }
            if (thisDistance == firstDistance) {
                winners.add(min.key)
            }
            distances.remove(min.key)
        } while (thisDistance == firstDistance)
        return winners
    }

}
