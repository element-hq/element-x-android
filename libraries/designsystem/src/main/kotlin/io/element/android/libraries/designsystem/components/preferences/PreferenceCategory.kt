/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        PreferenceText(
            title = "Title",
            icon = CompoundIcons.ChatProblem(),
        )
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
