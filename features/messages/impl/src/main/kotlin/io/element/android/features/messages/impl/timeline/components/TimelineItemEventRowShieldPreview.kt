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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowShieldPreview() = ElementPreview {
    Column {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Sender with a super long name that should ellipsize",
                isMine = true,
                content = aTimelineItemTextContent(
                    body = "Message sent from unsigned device"
                ),
                groupPosition = TimelineItemGroupPosition.First,
                messageShield = aCriticalShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Sender with a super long name that should ellipsize",
                content = aTimelineItemTextContent(
                    body = "Short Message with authenticity warning"
                ),
                groupPosition = TimelineItemGroupPosition.Middle,
                messageShield = aWarningShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemImageContent().copy(
                    aspectRatio = 2.5f
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                messageShield = aCriticalShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                content = aTimelineItemImageContent().copy(
                    aspectRatio = 2.5f
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                messageShield = aWarningShield()
            ),
        )
    }
}

private fun aWarningShield() = MessageShield.AuthenticityNotGuaranteed(isCritical = false)

internal fun aCriticalShield() = MessageShield.UnverifiedIdentity(isCritical = true)
