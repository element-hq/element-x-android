/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.api

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState

/**
 * Service to perform security scans in the contents for a given media source.
 *
 * The default implementation (FOSS) always returns a valid state.
 * This will only process media when a [ContentScanner] is provided when building the client in Pro.
 */
fun interface ContentScannerService {
    fun scan(mediaSources: List<MediaSource>, contentValidationState: ContentValidationState)
}
