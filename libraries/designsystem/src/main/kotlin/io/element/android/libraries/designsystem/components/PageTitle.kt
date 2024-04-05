/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton

/**
 * Compound component that displays a big icon, a title, an optional subtitle and an optional call to action component.
 *
 * @param title the title to display
 * @param iconStyle the style of the [BigIcon] to display
 * @param modifier the modifier to apply to this layout
 * @param subtitle the optional subtitle to display. It defaults to `null`
 * @param callToAction the optional call to action component to display. It defaults to `null`
 */
@Composable
fun PageTitle(
    title: AnnotatedString,
    iconStyle: BigIcon.Style,
    modifier: Modifier = Modifier,
    subtitle: AnnotatedString? = null,
    callToAction: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BigIcon(style = iconStyle)
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = ElementTheme.typography.fontHeadingMdBold,
                color = ElementTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            subtitle?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        callToAction?.invoke()
    }
}

/**
 * Compound component that displays a big icon, a title, an optional subtitle and an optional call to action component.
 *
 * @param title the title to display
 * @param iconStyle the style of the [BigIcon] to display
 * @param modifier the modifier to apply to this layout
 * @param subtitle the optional subtitle to display. It defaults to `null`
 * @param callToAction the optional call to action component to display. It defaults to `null`
 */
@Composable
fun PageTitle(
    title: String,
    iconStyle: BigIcon.Style,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    callToAction: @Composable (() -> Unit)? = null,
) = PageTitle(
    title = AnnotatedString(title),
    iconStyle = iconStyle,
    modifier = modifier,
    subtitle = subtitle?.let { AnnotatedString(it) },
    callToAction = callToAction
)

@PreviewsDayNight
@Composable
internal fun TitleWithIconFullPreview(@PreviewParameter(BigIconStylePreviewProvider::class) style: BigIcon.Style) {
    ElementPreview {
        PageTitle(
            modifier = Modifier.padding(top = 24.dp),
            title = AnnotatedString("Headline"),
            subtitle = AnnotatedString("Description goes here"),
            iconStyle = style,
            callToAction = {
                TextButton(text = "Learn more", onClick = {})
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TitleWithIconMinimalPreview() {
    ElementPreview {
        PageTitle(
            modifier = Modifier.padding(top = 24.dp),
            title = "Headline",
            iconStyle = BigIcon.Style.Default(CompoundIcons.CheckCircleSolid()),
        )
    }
}
