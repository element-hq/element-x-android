/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.scanner

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.scanner.ContentScannerUrlProvider

/**
 * Default FOSS implementation of [ContentScannerUrlProvider] that returns `null` for the content scanner URL.
 */
@ContributesBinding(AppScope::class)
class DefaultContentScannerUrlProvider : ContentScannerUrlProvider {
    override suspend fun getContentScannerUrl(homeserver: String): Result<String?> = Result.success(null)
}
