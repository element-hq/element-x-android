package io.element.android.x.core.di

/**
 * A [DaggerComponentOwner] is anything that "owns" a Dagger Component.
 *
 */
interface DaggerComponentOwner {
    /** This is either a component, or a list of components. */
    val daggerComponent: Any
}