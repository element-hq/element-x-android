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

package io.element.android.features.messages.impl.sender

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.components.aProfileTimelineDetailsReady
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails

data class SenderNameData(
    val userId: UserId,
    val profileTimelineDetails: ProfileTimelineDetails,
    val senderNameMode: SenderNameMode,
)

open class SenderNameDataProvider : PreviewParameterProvider<SenderNameData> {
    override val values: Sequence<SenderNameData>
        get() = sequenceOf(
            SenderNameMode.Timeline(mainColor = Color.Red),
            SenderNameMode.Reply,
            SenderNameMode.ActionList,
        )
            .flatMap { senderNameMode ->
                sequenceOf(
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                    ),
                    aSenderNameData(
                        senderNameMode = senderNameMode,
                        displayNameAmbiguous = true,
                    ),
                    SenderNameData(
                        senderNameMode = senderNameMode,
                        userId = UserId("@alice:${senderNameMode.javaClass.simpleName.lowercase()}"),
                        profileTimelineDetails = ProfileTimelineDetails.Unavailable,
                    ),
                )
            }
}

private fun aSenderNameData(
    senderNameMode: SenderNameMode,
    displayNameAmbiguous: Boolean = false,
) = SenderNameData(
    userId = UserId("@alice:${senderNameMode.javaClass.simpleName.lowercase()}"),
    profileTimelineDetails = aProfileTimelineDetailsReady(
        displayName = "Alice ${senderNameMode.javaClass.simpleName}",
        displayNameAmbiguous = displayNameAmbiguous,
    ),
    senderNameMode = senderNameMode,
)
