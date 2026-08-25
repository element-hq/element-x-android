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
    /**
     * Starts scanning the given media, reporting the verdict of each one through [contentValidationState] rather than by returning.
     *
     * @param mediaSources the media to scan.
     * @param contentValidationState where the outcome of each scan is published for the UI to observe.
     */
    fun scan(mediaSources: List<MediaSource>, contentValidationState: ContentValidationState)
}
