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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent

import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun TimelineItemEncryptedView(
    content: TimelineItemEncryptedContent,
    modifier: Modifier = Modifier
) {
    TimelineItemInformativeView(
        text = stringResource(id = StringR.string.common_decryption_error),
        iconDescription = stringResource(id = StringR.string.dialog_title_warning),
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
            data = UnableToDecryptContent.Data.Unknown
        )
    )
}
