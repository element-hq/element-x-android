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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemReactionsView(
    reactionsState: TimelineItemReactions,
    modifier: Modifier = Modifier,
) {
    if (reactionsState.reactions.isEmpty()) return
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = 2.dp,
        crossAxisSpacing = 8.dp,
    ) {
        reactionsState.reactions.forEach { reaction ->
            MessagesReactionButton(reaction = reaction)
        }
    }
}

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
            Text(text = reaction.key, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = reaction.count, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}
