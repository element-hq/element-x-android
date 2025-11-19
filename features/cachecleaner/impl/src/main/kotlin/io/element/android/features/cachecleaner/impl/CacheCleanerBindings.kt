/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.cachecleaner.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.cachecleaner.api.CacheCleaner

@ContributesTo(AppScope::class)
interface CacheCleanerBindings {
    fun cacheCleaner(): CacheCleaner
}
