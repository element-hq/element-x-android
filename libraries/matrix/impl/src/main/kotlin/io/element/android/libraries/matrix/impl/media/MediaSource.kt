/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.media.MediaSource
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

fun RustMediaSource.map(): MediaSource = use {
    MediaSource(it.url(), it.toJson())
}

internal fun MediaSource.toRustMediaSource(): RustMediaSource {
    val json = this.json
    return try {
        if (json != null) {
            RustMediaSource.fromJson(json)
        } else {
            RustMediaSource.fromUrl(safeUrl)
        }
    } catch (e: LinkageError) {
        // Used for tests, since we can't instantiate an actual `RustMediaSource` because the native library can't be loaded
        val isTest = runCatchingExceptions { Class.forName("org.junit.Test") }.isSuccess
        if (isTest) RustMediaSource(NoHandle) else throw e
    }
}
