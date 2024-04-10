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

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.modifiers.squareSize
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun NumberedListMolecule(index: Int, text: AnnotatedString) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemNumber(index = index)
        Text(text = text, style = ElementTheme.typography.fontBodyMdRegular, color = ElementTheme.colors.textPrimary)
    }
}

@Composable
private fun ItemNumber(
    index: Int,
) {
    val color = ElementTheme.colors.textPlaceholder
    Box(
        modifier = Modifier
            .border(1.dp, color, CircleShape)
            .squareSize()
    ) {
        Text(
            modifier = Modifier.padding(1.5.dp),
            text = index.toString(),
            style = ElementTheme.typography.fontBodySmRegular,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}
