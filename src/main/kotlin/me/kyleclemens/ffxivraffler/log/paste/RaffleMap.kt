/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.log.paste

class RaffleMap<K, V>(private val map: MutableMap<K, V>) : MutableMap<K, V> by map {

    override fun put(key: K, value: V): V? {
        if (key in this.map) return this.map[key]
        return this.map.put(key, value)
    }

    override fun toString() = this.map.toString()

}
