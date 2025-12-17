/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.ui

import androidx.compose.runtime.Composable
import io.element.android.features.call.impl.ui.LanguageTagProvider

class FakeLanguageTagProvider(private val languageTag: String?) : LanguageTagProvider {
    @Composable
    override fun provideLanguageTag() = languageTag
}
