/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader

@Composable
fun PreferenceCategory(
    modifier: Modifier = Modifier,
    title: String? = null,
    showTopDivider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        if (title != null) {
            ListSectionHeader(
                title = title,
                hasDivider = showTopDivider,
            )
        } else if (showTopDivider) {
            PreferenceDivider()
        }
        content()
    }
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceCategoryPreview() = ElementThemedPreview {
    PreferenceCategory(
        title = "Category title",
    ) {
        PreferenceSwitch(
            title = "Switch",
            icon = CompoundIcons.Threads(),
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceSlide(
            title = "Slide",
            summary = "Summary",
            value = 0.75F,
            showIconAreaIfNoIcon = true,
            onValueChange = {},
        )
    }
}
