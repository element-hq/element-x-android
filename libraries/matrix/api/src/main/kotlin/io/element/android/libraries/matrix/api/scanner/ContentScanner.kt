/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.scanner

import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Component used to manually scan media content for potential security risks.
 *
 * While the Matrix SDK automatically scans media content we load if a content scanner instance is provided, this interface allows for scanning
 * some media sources in advance without actually loading their contents, which can be useful for pre-fetching scenarios.
 */
interface ContentScanner {
    /**
     * Manually scans the given [mediaSource] for potential security risks.
     *
     * @param mediaSource The media source to scan.
     * @return A [Result] containing a [Boolean] indicating whether the content is safe (true) or potentially unsafe (false).
     * If the scan fails, the [Result] will contain an exception.
     */
    suspend fun scan(mediaSource: MediaSource): Result<Boolean>
}
