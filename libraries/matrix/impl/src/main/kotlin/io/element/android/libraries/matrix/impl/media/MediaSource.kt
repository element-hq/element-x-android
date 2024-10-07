/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.MediaSource
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

fun RustMediaSource.map(): MediaSource = use {
    MediaSource(it.url(), it.toJson())
}
