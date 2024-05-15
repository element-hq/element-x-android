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

package io.element.android.libraries.designsystem.components.tooltip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import io.element.android.compound.theme.ElementTheme
import androidx.compose.material3.PlainTooltip as M3PlainTooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipScope.PlainTooltip(
    modifier: Modifier = Modifier,
    contentColor: Color = ElementTheme.colors.textOnSolidPrimary,
    containerColor: Color = ElementTheme.colors.bgActionPrimaryRest,
    shape: Shape = TooltipDefaults.plainTooltipContainerShape,
    content: @Composable () -> Unit,
) = M3PlainTooltip(
    modifier = modifier,
    contentColor = contentColor,
    containerColor = containerColor,
    shape = shape,
    content = content,
)
