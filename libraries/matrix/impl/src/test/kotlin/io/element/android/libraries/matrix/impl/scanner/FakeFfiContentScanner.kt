/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.scanner

import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ContentScanner
import org.matrix.rustcomponents.sdk.MediaSource
import org.matrix.rustcomponents.sdk.NoHandle
import uniffi.matrix_sdk_contentscanner.MediaScanResponse

class FakeFfiContentScanner(
    private val scan: suspend (client: Client, mediaSource: MediaSource) -> MediaScanResponse = { _, _ ->
        MediaScanResponse(clean = true, info = "Just peachy")
    },
) : ContentScanner(NoHandle) {
    override suspend fun scan(client: Client, mediaSource: MediaSource): MediaScanResponse {
        return scan.invoke(client, mediaSource)
    }
}
