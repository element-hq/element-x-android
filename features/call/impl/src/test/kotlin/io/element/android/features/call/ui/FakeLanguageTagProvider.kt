/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.ui

import androidx.compose.runtime.Composable
import io.element.android.features.call.impl.ui.LanguageTagProvider

class FakeLanguageTagProvider(private val languageTag: String?) : LanguageTagProvider {
    @Composable
    override fun provideLanguageTag() = languageTag
}
