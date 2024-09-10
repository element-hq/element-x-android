/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline

import androidx.compose.runtime.Composable
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.wysiwyg.utils.HtmlConverter

interface HtmlConverterProvider {
    @Composable
    fun Update(currentUserId: UserId)

    fun provide(): HtmlConverter
}
