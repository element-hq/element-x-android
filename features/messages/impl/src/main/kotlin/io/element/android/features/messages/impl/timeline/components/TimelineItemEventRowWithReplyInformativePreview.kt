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
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.model.InReplyToDetails
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithReplyInformativePreview(
    @PreviewParameter(InReplyToDetailsInformativeProvider::class) inReplyToDetails: InReplyToDetails,
) = ElementPreview {
    TimelineItemEventRowWithReplyContentToPreview(inReplyToDetails)
}

class InReplyToDetailsInformativeProvider : InReplyToDetailsProvider() {
    override val values: Sequence<InReplyToDetails>
        get() = sequenceOf(
            RedactedContent,
            UnableToDecryptContent(UnableToDecryptContent.Data.Unknown),
        ).map {
            aInReplyToDetails(
                eventContent = it,
            )
        }
}
