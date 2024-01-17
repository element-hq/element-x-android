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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.iconSuccessPrimaryBackground
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
internal fun FormattingOption(
    state: FormattingOptionState,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (state) {
        FormattingOptionState.Selected -> ElementTheme.colors.iconSuccessPrimaryBackground
        FormattingOptionState.Default,
        FormattingOptionState.Disabled -> Color.Transparent
    }

    val foregroundColor = when (state) {
        FormattingOptionState.Selected -> ElementTheme.colors.iconSuccessPrimary
        FormattingOptionState.Default -> ElementTheme.colors.iconSecondary
        FormattingOptionState.Disabled -> ElementTheme.colors.iconDisabled
    }
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    radius = 20.dp,
                ),
            )
            .size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp),
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = foregroundColor,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun FormattingButtonPreview() = ElementPreview {
    Row {
        FormattingOption(
            state = FormattingOptionState.Default,
            onClick = { },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_bold),
            contentDescription = null,
        )
        FormattingOption(
            state = FormattingOptionState.Selected,
            onClick = { },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_italic),
            contentDescription = null,
        )
        FormattingOption(
            state = FormattingOptionState.Disabled,
            onClick = { },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_underline),
            contentDescription = null,
        )
    }
}
