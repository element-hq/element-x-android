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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import org.matrix.rustcomponents.sdk.EncryptedMessage
import io.element.android.features.messages.timeline.model.event.TimelineItemEncryptedContent

@Composable
fun TimelineItemEncryptedView(
    content: TimelineItemEncryptedContent,
    modifier: Modifier = Modifier
) {
    TimelineItemInformativeView(
        text = "Decryption error",
        iconDescription = "Warning",
        icon = Icons.Default.Warning,
        modifier = modifier
    )
}

@Preview
@Composable
internal fun TimelineItemEncryptedViewLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun TimelineItemEncryptedViewDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    TimelineItemEncryptedView(
        content = TimelineItemEncryptedContent(
            encryptedMessage = EncryptedMessage.Unknown,
        )
    )
}
