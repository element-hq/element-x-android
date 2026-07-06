/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.features.contentscanner.api.ContentScannerService
import io.element.android.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import io.element.android.features.contentscanner.impl.DefaultContentScannerService
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object ContentScannerModule {
    @Provides
    @SingleIn(SessionScope::class)
    fun providesContentScannerService(
        contentScanner: ContentScanner?,
        eventContentValidationCache: EventContentValidationCache,
        @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    ): ContentScannerService {
        return if (contentScanner != null) {
            DefaultContentScannerService(
                contentScanner = contentScanner,
                eventContentValidationCache = eventContentValidationCache,
                sessionCoroutineScope = sessionCoroutineScope
            )
        } else {
            NoopContentScannerService()
        }
    }
}
