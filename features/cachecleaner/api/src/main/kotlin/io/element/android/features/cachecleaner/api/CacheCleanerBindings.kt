/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.cachecleaner.api

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
interface CacheCleanerBindings {
    fun cacheCleaner(): CacheCleaner
}
