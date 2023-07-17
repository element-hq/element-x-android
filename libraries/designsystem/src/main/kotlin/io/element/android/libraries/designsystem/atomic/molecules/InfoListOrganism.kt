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

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.InfoListItemMolecule
import io.element.android.libraries.designsystem.atomic.atoms.InfoListItemPosition
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList

@Composable
fun InfoListOrganism(
    items: ImmutableList<InfoListItem>,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
    textStyle: TextStyle = LocalTextStyle.current,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
    ) {
        for ((index, item) in items.withIndex()) {
            val position = when {
                items.size == 1 -> InfoListItemPosition.Single
                index == 0 -> InfoListItemPosition.Top
                index == items.size - 1 -> InfoListItemPosition.Bottom
                else -> InfoListItemPosition.Middle
            }
            InfoListItemMolecule(
                message = { Text(item.message, style = textStyle) },
                icon = {
                    if (item.iconId != null) {
                        Icon(resourceId = item.iconId, contentDescription = null, tint = iconTint)
                    } else if (item.iconVector != null) {
                        Icon(imageVector = item.iconVector, contentDescription = null, tint = iconTint)
                    } else {
                        item.iconComposable()
                    }
                },
                position = position,
                backgroundColor = backgroundColor,
            )
        }
    }
}

data class InfoListItem(
    val message: String,
    @DrawableRes val iconId: Int? = null,
    val iconVector: ImageVector? = null,
    val iconComposable: @Composable () -> Unit = {},
)
