/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.scanner

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ContentScanner as RustScanner
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

class RustContentScanner(
    private val client: Client,
    private val rustScanner: RustScanner,
) : ContentScanner {
    override suspend fun scan(mediaSource: MediaSource): Result<Boolean> {
        return runCatchingExceptions {
            rustScanner.scan(client, mediaSource.toRustMediaSource()).clean
        }
    }

    fun toRust(): RustScanner = rustScanner
}

private fun MediaSource.toRustMediaSource(): RustMediaSource {
    val json = this.json
    return if (json != null) {
        RustMediaSource.fromJson(json)
    } else {
        RustMediaSource.fromUrl(safeUrl)
    }
}
