/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.extensions

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.toStackTraceString() = StringWriter().apply { this@toStackTraceString.printStackTrace(PrintWriter(this)) }.toString()

fun <E : Enum<E>> Enum<E>.friendlyName() = this.name
    .split("_")
    .map {
        if (it.length > 1) it[0].toUpperCase() + it.substring(1).toLowerCase() else it
    }
    .joinToString(" ")

fun String.toEnumName() = this.toUpperCase().replace(' ', '_')
