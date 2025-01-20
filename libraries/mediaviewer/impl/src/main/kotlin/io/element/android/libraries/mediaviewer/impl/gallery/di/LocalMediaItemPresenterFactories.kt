/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides a [MediaItemPresenterFactories] to the composition.
 */
val LocalMediaItemPresenterFactories = staticCompositionLocalOf {
    MediaItemPresenterFactories(emptyMap())
}
