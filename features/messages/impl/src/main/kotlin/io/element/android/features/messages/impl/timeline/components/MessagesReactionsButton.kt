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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
fun MessagesReactionsButton(
    onClick: () -> Unit,
    content: MessagesReactionsButtonContent,
    modifier: Modifier = Modifier,
) {
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
        when (content) {
            is MessagesReactionsButtonContent.Icon -> IconContent(imageVector = content.imageVector)
            is MessagesReactionsButtonContent.Text -> TextContent(text = content.text)
        }
    }
}

sealed class MessagesReactionsButtonContent {
    data class Text(val text: String) : MessagesReactionsButtonContent()
    data class Icon(val imageVector: ImageVector) : MessagesReactionsButtonContent()
}

private val reactionEmojiTextSize = 20.sp

@Composable
private fun TextContent(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    modifier = modifier
        .height(reactionEmojiTextSize.toDp()),
    text = text,
    style = ElementTextStyles.Regular.bodyMD
)

@Composable
private fun IconContent(
    imageVector: ImageVector,
    modifier: Modifier = Modifier
) = Icon(
    imageVector = imageVector,
    contentDescription = "Add emoji",
    tint = MaterialTheme.colorScheme.secondary,
    modifier = modifier
        .size(reactionEmojiTextSize.toDp())
)

@Preview
@Composable
internal fun MessagesReactionsButtonLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun MessagesReactionsButtonDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Row {
        MessagesReactionsButton(
            content = MessagesReactionsButtonContent.Icon(Icons.Outlined.AddReaction),
            onClick = {}
        )
        MessagesReactionsButton(
            content = MessagesReactionsButtonContent.Text("12 more"),
            onClick = {}
        )
    }
}
