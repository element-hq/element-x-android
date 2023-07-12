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

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InfoListItemAtom(
    message: @Composable () -> Unit,
    position: InfoListItemPosition,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val radius = 14.dp
    val backgroundShape = remember(position) {
        when (position) {
            InfoListItemPosition.Single -> RoundedCornerShape(radius)
            InfoListItemPosition.Top -> RoundedCornerShape(topStart = radius, topEnd = radius)
            InfoListItemPosition.Middle -> RoundedCornerShape(0.dp)
            InfoListItemPosition.Bottom -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = backgroundShape,
            )
            .padding(vertical = 12.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        icon()
        message()
    }
}

enum class InfoListItemPosition {
    Top,
    Middle,
    Bottom,
    Single,
}
