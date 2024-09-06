/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.test.timeline

import androidx.compose.runtime.Composable
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.wysiwyg.utils.HtmlConverter

class FakeHtmlConverterProvider(
    private val transform: (String) -> CharSequence = { it },
) : HtmlConverterProvider {
    @Composable
    override fun Update(currentUserId: UserId) = Unit

    override fun provide(): HtmlConverter {
        return object : HtmlConverter {
            override fun fromHtmlToSpans(html: String): CharSequence {
                return transform(html)
            }
        }
    }
}
