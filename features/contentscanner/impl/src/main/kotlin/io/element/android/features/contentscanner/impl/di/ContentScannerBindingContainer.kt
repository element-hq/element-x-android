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
import io.element.android.features.contentscanner.impl.DefaultContentScannerService
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.RoomCoroutineScope
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(RoomScope::class)
object ContentScannerBindingContainer {
    @Provides
    @SingleIn(RoomScope::class)
    fun providesContentScannerService(
        contentScanner: ContentScanner?,
        @RoomCoroutineScope coroutineScope: CoroutineScope,
        coroutineDispatchers: CoroutineDispatchers,
    ): ContentScannerService {
        return if (contentScanner != null) {
            DefaultContentScannerService(
                contentScanner = contentScanner,
                coroutineScope = coroutineScope,
                coroutineDispatchers = coroutineDispatchers,
            )
        } else {
            AlwaysValidContentScannerService()
        }
    }
}
