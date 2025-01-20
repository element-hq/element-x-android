/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import dagger.MapKey
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem
import kotlin.reflect.KClass

/**
 * Annotation to add a factory of type [MediaItemPresenterFactory] to a
 * Dagger map multi binding keyed with a subclass of [MediaItem.Event].
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class MediaItemEventContentKey(val value: KClass<out MediaItem.Event>)
