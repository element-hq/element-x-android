/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import dev.zacsweers.metro.MapKey
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlin.reflect.KClass

/**
 * Annotation to add a factory of type [MediaItemPresenterFactory] to a
 * DI map multi binding keyed with a subclass of [MediaItem.Event].
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class MediaItemEventContentKey(val value: KClass<out MediaItem.Event>)
