/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.util.listeners

import java.awt.event.ComponentEvent

abstract class DockedComponentListener : DefaultComponentListener() {

    abstract fun componentMovedOrResized(e: ComponentEvent)

    override fun componentMoved(e: ComponentEvent) {
        this.componentMovedOrResized(e)
    }

    override fun componentResized(e: ComponentEvent) {
        this.componentMovedOrResized(e)
    }

}
