/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import android.content.Context
import android.content.ContextWrapper
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.di.DaggerComponentOwner

inline fun <reified T : Any> Node.optionalBindings() = optionalBindings(T::class.java)
inline fun <reified T : Any> Node.bindings() = bindings(T::class.java)
inline fun <reified T : Any> Context.bindings() = bindings(T::class.java)

fun <T : Any> Context.bindings(klass: Class<T>): T {
    // search dagger components in the context hierarchy
    return generateSequence(this) { (it as? ContextWrapper)?.baseContext }
        .plus(applicationContext)
        .filterIsInstance<DaggerComponentOwner>()
        .map { it.daggerComponent }
        .flatMap { if (it is Collection<*>) it else listOf(it) }
        .filterIsInstance(klass)
        .firstOrNull()
        ?: error("Unable to find bindings for ${klass.name}")
}

fun <T : Any> Node.optionalBindings(klass: Class<T>): T? {
    // search dagger components in node hierarchy
    return generateSequence(this, Node::parent)
        .filterIsInstance<DaggerComponentOwner>()
        .map { it.daggerComponent }
        .flatMap { if (it is Collection<*>) it else listOf(it) }
        .filterIsInstance(klass)
        .firstOrNull()
}

fun <T : Any> Node.bindings(klass: Class<T>): T {
    return optionalBindings(klass) ?: error("Unable to find bindings for ${klass.name}")
}
