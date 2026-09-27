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
import org.matrix.rustcomponents.sdk.NoHandle
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
}

private fun MediaSource.toRustMediaSource(): RustMediaSource {
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
