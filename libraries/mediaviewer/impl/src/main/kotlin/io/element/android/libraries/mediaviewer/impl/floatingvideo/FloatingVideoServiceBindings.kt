/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface FloatingVideoServiceBindings {
    fun inject(service: FloatingVideoService)
    fun videoDataRepository(): VideoDataRepository
}


