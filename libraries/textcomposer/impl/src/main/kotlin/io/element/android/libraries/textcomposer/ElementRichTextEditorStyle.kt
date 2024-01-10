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

package io.element.android.libraries.textcomposer

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.bgSubtleTertiary
import io.element.android.wysiwyg.compose.RichTextEditorDefaults
import io.element.android.wysiwyg.compose.RichTextEditorStyle

object ElementRichTextEditorStyle {
    @Composable
    fun composerStyle(
        hasFocus: Boolean,
    ): RichTextEditorStyle {
        val baseStyle = common()
        return baseStyle.copy(
            text = baseStyle.text.copy(
                color = if (hasFocus) {
                    ElementTheme.materialColors.primary
                } else {
                    ElementTheme.materialColors.secondary
                },
                lineHeight = TextUnit.Unspecified,
                includeFontPadding = true,
            )
        )
    }

    @Composable
    fun textStyle(): RichTextEditorStyle {
        return common()
    }

    @Composable
    private fun common(): RichTextEditorStyle {
        val colors = ElementTheme.colors
        val codeCornerRadius = 4.dp
        val codeBorderWidth = 1.dp
        return RichTextEditorDefaults.style(
            text = RichTextEditorDefaults.textStyle(
                color = LocalTextStyle.current.color.takeIf { it.isSpecified } ?: LocalContentColor.current,
                fontStyle = LocalTextStyle.current.fontStyle,
                lineHeight = LocalTextStyle.current.lineHeight,
                includeFontPadding = false,
            ),
            cursor = RichTextEditorDefaults.cursorStyle(
                color = colors.iconAccentTertiary,
            ),
            link = RichTextEditorDefaults.linkStyle(
                color = colors.textLinkExternal,
            ),
            codeBlock = RichTextEditorDefaults.codeBlockStyle(
                leadingMargin = 8.dp,
                background = RichTextEditorDefaults.codeBlockBackgroundStyle(
                    color = colors.bgSubtleTertiary,
                    borderColor = colors.borderInteractiveSecondary,
                    cornerRadius = codeCornerRadius,
                    borderWidth = codeBorderWidth,
                )
            ),
            inlineCode = RichTextEditorDefaults.inlineCodeStyle(
                verticalPadding = 0.dp,
                background = RichTextEditorDefaults.inlineCodeBackgroundStyle(
                    color = colors.bgSubtleTertiary,
                    borderColor = colors.borderInteractiveSecondary,
                    cornerRadius = codeCornerRadius,
                    borderWidth = codeBorderWidth,
                )
            ),
        )
    }
}
