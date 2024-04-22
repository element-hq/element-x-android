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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

// Note: no need for light/dark variant for this preview
@Preview
@Composable
internal fun TimelineItemEventRowLongSenderNamePreview() = ElementPreviewLight {
    ATimelineItemEventRow(
        event = aTimelineItemEvent(
            senderDisambiguatedDisplayName = "a long sender display name to test single line and ellipsis at the end of the line",
        ),
    )
}
