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

package io.element.android.features.messages.timeline.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.AggregatedReactionProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun MessagesReactionButton(reaction: AggregatedReaction, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(corner = CornerSize(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO `reaction.isHighlighted` is not used.
            Text(text = reaction.key, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = reaction.count, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
internal fun MessagesReactionButtonLightPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) =
    ElementPreviewLight { ContentToPreview(reaction) }

@Preview
@Composable
internal fun MessagesReactionButtonDarkPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) =
    ElementPreviewDark { ContentToPreview(reaction) }

@Composable
private fun ContentToPreview(reaction: AggregatedReaction) {
    MessagesReactionButton(reaction)
}
