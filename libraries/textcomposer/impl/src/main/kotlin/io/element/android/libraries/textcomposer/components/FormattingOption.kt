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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.theme.compound.generated.SemanticColors

@Composable
internal fun FormattingOption(
    state: FormattingOptionState,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    colors: SemanticColors = ElementTheme.colors,
) {
    val backgroundColor = when (state) {
        FormattingOptionState.Selected -> colors.bgActionPrimaryRest
        FormattingOptionState.Default,
        FormattingOptionState.Disabled -> Color.Transparent
    }

    val foregroundColor = when (state) {
        FormattingOptionState.Selected -> colors.iconOnSolidPrimary
        FormattingOptionState.Default -> colors.iconPrimary
        FormattingOptionState.Disabled -> colors.iconDisabled
    }
    Box(
        modifier = modifier
            .clickable { onClick() }
            .size(44.dp.applyScaleUp())
            .background(backgroundColor, shape = RoundedCornerShape(4.dp.applyScaleUp()))
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = foregroundColor,
        )
    }
}

