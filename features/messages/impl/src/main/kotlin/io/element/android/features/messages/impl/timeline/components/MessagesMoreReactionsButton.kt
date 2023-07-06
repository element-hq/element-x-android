/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.theme.ElementTheme

@Composable
fun MessagesMoreReactionsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val buttonColor = ElementTheme.colors.bgSubtleSecondary
    Surface(
        modifier = modifier
            .background(Color.Transparent)
            // Outer border, same colour as background
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                shape = RoundedCornerShape(corner = CornerSize(14.dp))
            )
            .padding(vertical = 2.dp, horizontal = 2.dp)
            // Clip click indicator inside the outer border
            .clip(RoundedCornerShape(corner = CornerSize(12.dp)))
            .clickable(onClick = onClick)
            .background(buttonColor, RoundedCornerShape(corner = CornerSize(12.dp)))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        color = buttonColor
    ) {
            Icon(
                imageVector = Icons.Outlined.AddReaction,
                contentDescription = "Add emoji",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    // Same size as the line height of reaction emoji text
                    .size(with(LocalDensity.current) { 20.sp.toDp() })
            )
    }
}

@Preview
@Composable
internal fun MessagesMoreReactionsButtonLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun MessagesMoreReactionsButtonDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    MessagesMoreReactionsButton(onClick = {})
}
