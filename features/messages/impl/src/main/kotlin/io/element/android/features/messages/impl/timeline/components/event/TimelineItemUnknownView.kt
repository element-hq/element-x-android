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
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.preview.ElementPreviews
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemUnknownView(
    content: TimelineItemUnknownContent,
    extraPadding: ExtraPadding,
    modifier: Modifier = Modifier
) {
    TimelineItemInformativeView(
        text = stringResource(id = CommonStrings.common_unsupported_event),
        iconDescription = stringResource(id = CommonStrings.dialog_title_warning),
        icon = Icons.Default.Info,
        extraPadding = extraPadding,
        modifier = modifier
    )
}

@ElementPreviews
@Composable
internal fun TimelineItemUnknownViewLightPreview() {
    ElementPreview { ContentToPreview() }
}

@Composable
private fun ContentToPreview() {
    TimelineItemUnknownView(
        content = TimelineItemUnknownContent,
        extraPadding = noExtraPadding
    )
}
