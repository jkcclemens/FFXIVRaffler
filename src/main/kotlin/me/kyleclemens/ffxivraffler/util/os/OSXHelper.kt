/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package me.kyleclemens.ffxivraffler.util.os

import java.lang.reflect.Method
import java.lang.reflect.Proxy

class OSXHelper {

    val applicationClass = Class.forName("com.apple.eawt.Application")
    val application = this.applicationClass.getDeclaredMethod("getApplication")(null)

    fun getMethod(name: String, parameters: List<String>) = this.applicationClass.getDeclaredMethod(name, *parameters.map { Class.forName(it) }.toTypedArray())

    fun callMethod(name: String, parameters: List<String> = listOf(), vararg args: Any) = this.getMethod(name, parameters)(this.application, *args)

    fun proxyClass(className: String, methodHandler: (Any?, Method, Array<Any?>?) -> Any?): Any {
        val clazz = Class.forName(className)
        val proxy = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { any, method, arrayOfAnys -> methodHandler(any, method, arrayOfAnys) }
        return proxy
    }

}
