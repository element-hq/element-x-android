/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlin.reflect.KClass

/**
 * Container that declares the [MediaItemPresenterFactory] map multi binding.
 *
 * Its sole purpose is to support the case of an empty map multibinding.
 */
@BindingContainer
@ContributesTo(RoomScope::class)
interface MediaItemPresenterFactoriesBindingContainer {
    @Multibinds
    fun multiBindMediaItemPresenterFactories(): @JvmSuppressWildcards Map<KClass<out MediaItem.Event>, MediaItemPresenterFactory<*, *>>
}
