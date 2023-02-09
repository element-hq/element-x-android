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

package io.element.android.libraries.designsystem.theme.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap

@Composable
internal fun ColorListDebugView(
    backgroundColor: Color,
    foregroundColor: Color,
    colors: ImmutableMap<String, Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = backgroundColor)
            .fillMaxWidth()
    ) {
        colors.keys.forEach { name ->
            val color = colors[name]!!
            ColorDebugView(backgroundColor = backgroundColor, foregroundColor = foregroundColor, name = name, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
