package io.element.android.x.architecture

import android.content.Context
import android.content.ContextWrapper
import com.bumble.appyx.core.node.Node
import io.element.android.x.core.di.DaggerComponentOwner

inline fun <reified T : Any> Node.bindings() = bindings(T::class.java)
inline fun <reified T : Any> Context.bindings() = bindings(T::class.java)

/** Use no-arg extension function instead: [Context.bindings] */
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

/** Use no-arg extension function instead: [Node.bindings] */
fun <T : Any> Node.bindings(klass: Class<T>): T {
    // search dagger components in node hierarchy
    return generateSequence(this, Node::parent)
        .filterIsInstance<DaggerComponentOwner>()
        .map { it.daggerComponent }
        .flatMap { if (it is Collection<*>) it else listOf(it) }
        .filterIsInstance(klass)
        .firstOrNull()
        ?: error("Unable to find bindings for ${klass.name}")
}
