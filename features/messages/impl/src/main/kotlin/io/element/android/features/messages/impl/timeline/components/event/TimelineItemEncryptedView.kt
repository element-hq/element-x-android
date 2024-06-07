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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContentProvider
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UtdCause
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemEncryptedView(
    content: TimelineItemEncryptedContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMembershipUtd = (content.data as? UnableToDecryptContent.Data.MegolmV1AesSha2)?.utdCause == UtdCause.Membership
    val (textId, iconId) = if (isMembershipUtd) {
        CommonStrings.common_unable_to_decrypt_no_access to CompoundDrawables.ic_compound_block
    } else {
        CommonStrings.common_waiting_for_decryption_key to CompoundDrawables.ic_compound_time
    }
    TimelineItemInformativeView(
        text = stringResource(id = textId),
        iconDescription = stringResource(id = CommonStrings.dialog_title_warning),
        iconResourceId = iconId,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEncryptedViewPreview(
    @PreviewParameter(TimelineItemEncryptedContentProvider::class) content: TimelineItemEncryptedContent
) = ElementPreview {
    TimelineItemEncryptedView(
        content = content,
        onContentLayoutChange = {},
    )
}
