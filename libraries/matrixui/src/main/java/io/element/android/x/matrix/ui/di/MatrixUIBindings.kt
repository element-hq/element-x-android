package io.element.android.x.matrix.ui.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.x.di.SessionScope
import io.element.android.x.matrix.ui.media.LoggedInImageLoaderFactory
import io.element.android.x.matrix.ui.media.NotLoggedInImageLoaderFactory

@ContributesTo(SessionScope::class)
interface MatrixUIBindings {
    fun loggedInImageLoaderFactory(): LoggedInImageLoaderFactory
    fun notLoggedInImageLoaderFactory(): NotLoggedInImageLoaderFactory
}
