/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.scanner

/**
 * Provides the URL of the content scanner service for a given homeserver, if any is set up.
 */
fun interface ContentScannerUrlProvider {
    /**
     * Returns the URL of the content scanner service for the given [homeserver], or `null` if no content scanner is set up.
     */
    suspend fun getContentScannerUrl(homeserver: String): Result<String?>
}
