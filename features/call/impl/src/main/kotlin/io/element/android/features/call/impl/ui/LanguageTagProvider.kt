/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface LanguageTagProvider {
    @Composable
    fun provideLanguageTag(): String?
}

@ContributesBinding(AppScope::class)
class DefaultLanguageTagProvider @Inject constructor() : LanguageTagProvider {
    @Composable
    override fun provideLanguageTag(): String? {
        return LocalConfiguration.current.locales.get(0)?.toLanguageTag()
    }
}
