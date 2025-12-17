/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.test.timeline

import androidx.compose.runtime.Composable
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.wysiwyg.utils.HtmlConverter

class FakeHtmlConverterProvider(
    private val transform: (String) -> CharSequence = { it },
) : HtmlConverterProvider {
    @Composable
    override fun Update() = Unit

    override fun provide(): HtmlConverter {
        return object : HtmlConverter {
            override fun fromHtmlToSpans(html: String): CharSequence {
                return transform(html)
            }
        }
    }
}
