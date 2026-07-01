/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl.di

import io.element.android.features.contentscanner.api.ContentScannerService
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Noop implementation of [ContentScannerService] that always returns success.
 *
 * This is used when the content scanner feature is not enabled or available.
 */
class NoopContentScannerService : ContentScannerService {
    override fun scan(eventId: EventId, mediaSource: MediaSource, updateState: (AsyncData<Boolean>) -> Unit) {
        // Always return success for the noop implementation
        updateState(AsyncData.Success(true))
    }
}
