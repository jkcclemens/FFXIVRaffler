/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.log.paste

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class RaffleMapSpec : Spek({
    given("an empty raffle map of strings to integers") {
        val raffleMap = RaffleMap(hashMapOf<String, Int>())
        on("adding a pair") {
            beforeEach {
                raffleMap.clear()
                raffleMap["a"] = 1
            }

            it("should contain one pair") {
                assertEquals(1, raffleMap.size)
            }
            it("should contain the same pair that was added") {
                assertEquals(1, raffleMap["a"])
            }
        }
        on("adding another, different pair") {
            beforeEach {
                raffleMap.clear()
                raffleMap["a"] = 1
                raffleMap["b"] = 2
            }

            it("should contain two pairs") {
                assertEquals(2, raffleMap.size)
            }
            it("should contain the new pair") {
                assertEquals(2, raffleMap["b"])
            }
            it("should contain the old pair") {
                assertEquals(1, raffleMap["a"])
            }
        }
        on("adding another pair, with a conflicting key") {
            beforeEach {
                raffleMap.clear()
                raffleMap["a"] = 1
                raffleMap["b"] = 2
                raffleMap["a"] = 3
            }

            it("should contain two pairs") {
                assertEquals(2, raffleMap.size)
            }
            it("should not update the conflicting key-pair") {
                assertEquals(1, raffleMap["a"])
            }
            it("should still contain the second pair") {
                assertEquals(2, raffleMap["b"])
            }
        }
    }
})
