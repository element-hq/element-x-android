/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    val (textId, iconId) = when (content.data) {
        is UnableToDecryptContent.Data.MegolmV1AesSha2 -> {
            when (content.data.utdCause) {
                UtdCause.SentBeforeWeJoined -> {
                    CommonStrings.common_unable_to_decrypt_no_access to CompoundDrawables.ic_compound_block
                }
                UtdCause.VerificationViolation -> {
                    CommonStrings.common_unable_to_decrypt_verification_violation to CompoundDrawables.ic_compound_block
                }
                UtdCause.UnsignedDevice,
                UtdCause.UnknownDevice -> {
                    CommonStrings.common_unable_to_decrypt_insecure_device to CompoundDrawables.ic_compound_block
                }
                else -> {
                    CommonStrings.common_waiting_for_decryption_key to CompoundDrawables.ic_compound_time
                }
            }
        }
        else -> {
            // Should not happen, we only supports megolm in rooms
            CommonStrings.common_waiting_for_decryption_key to CompoundDrawables.ic_compound_time
        }
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
