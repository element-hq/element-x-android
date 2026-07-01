/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.api

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner

/**
 * Service to perform security scans in the contents for a given event and media source.
 *
 * The default implementation does nothing, unless a [ContentScanner] is provided when building the client.
 */
interface ContentScannerService {
    fun scan(eventId: EventId, mediaSource: MediaSource, updateState: (AsyncData<Boolean>) -> Unit)
}
