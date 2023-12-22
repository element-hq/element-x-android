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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChoiceSegmentedButtonRowScope.SegmentedButton(
    index: Int,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
) {
    SegmentedButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        enabled = enabled,
        shape = SegmentedButtonDefaults.itemShape(index = index, count = count),
        label = {
            Text(
                text = text,
                style = ElementTheme.typography.fontBodyMdMedium,
            )
        },
        colors = SegmentedButtonDefaults.colors(
            activeContainerColor = ElementTheme.materialColors.primary,
            activeContentColor = ElementTheme.materialColors.onPrimary,
            activeBorderColor = ElementTheme.materialColors.primary,
            inactiveContainerColor = ElementTheme.materialColors.surface,
            inactiveContentColor = ElementTheme.materialColors.onSurface,
            inactiveBorderColor = ElementTheme.materialColors.primary,
            disabledActiveContainerColor = ElementTheme.colors.bgActionPrimaryDisabled,
            disabledActiveContentColor = ElementTheme.colors.textOnSolidPrimary,
            disabledActiveBorderColor = ElementTheme.colors.bgActionPrimaryDisabled,
            disabledInactiveContainerColor = ElementTheme.materialColors.surface,
            disabledInactiveContentColor = ElementTheme.colors.textDisabled,
            disabledInactiveBorderColor = Color.Transparent,
        )
    )
}
