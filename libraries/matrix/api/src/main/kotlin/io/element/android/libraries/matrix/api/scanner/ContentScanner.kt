/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.scanner

import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Component used to scan media content for potential security risks.
 */
interface ContentScanner {
    /**
     * Scans the given [mediaSource] for potential security risks.
     *
     * @param mediaSource The media source to scan.
     * @return A [Result] containing a [Boolean] indicating whether the content is safe (true) or potentially unsafe (false).
     * If the scan fails, the [Result] will contain an exception.
     */
    suspend fun scan(mediaSource: MediaSource): Result<Boolean>
}
