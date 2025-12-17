/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

interface LanguageTagProvider {
    @Composable
    fun provideLanguageTag(): String?
}

@ContributesBinding(AppScope::class)
class DefaultLanguageTagProvider : LanguageTagProvider {
    @Composable
    override fun provideLanguageTag(): String? {
        return LocalConfiguration.current.locales.get(0)?.toLanguageTag()
    }
}
