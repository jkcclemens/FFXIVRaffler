/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.util.listeners

import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

abstract class DefaultComponentListener : ComponentListener {
    override fun componentResized(e: ComponentEvent?) {
    }

    override fun componentShown(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }

    override fun componentMoved(e: ComponentEvent?) {
    }
}
