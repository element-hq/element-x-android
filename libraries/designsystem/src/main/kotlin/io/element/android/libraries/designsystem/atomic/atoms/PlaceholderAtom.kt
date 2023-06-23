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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.roomListPlaceHolder

@Composable
fun PlaceholderAtom(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = ElementTheme.colors.roomListPlaceHolder(),
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                color = color,
                shape = RoundedCornerShape(size = height / 2)
            )
    )
}

@Preview
@Composable
internal fun PlaceholderAtomLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun PlaceholderAtomDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    // Use a Red background to see the shape
    Box(modifier = Modifier.background(color = Color.Red)) {
        PlaceholderAtom(
            width = 80.dp,
            height = 12.dp
        )
    }
}
