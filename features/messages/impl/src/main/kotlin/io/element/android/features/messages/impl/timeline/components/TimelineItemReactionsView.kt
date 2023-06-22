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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.aTimelineItemReactions
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun TimelineItemReactionsView(
    reactionsState: TimelineItemReactions,
    modifier: Modifier = Modifier,
) {
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

@Preview
@Composable
internal fun TimelineItemReactionsViewLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun TimelineItemReactionsViewDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    TimelineItemReactionsView(
        reactionsState = aTimelineItemReactions()
    )
}
