/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetailsDisambiguatedProvider

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowDisambiguatedPreview(
    @PreviewParameter(InReplyToDetailsDisambiguatedProvider::class) inReplyToDetails: InReplyToDetails,
) = ElementPreview {
    TimelineItemEventRowWithReplyContentToPreview(
        inReplyToDetails = inReplyToDetails,
        displayNameAmbiguous = true,
    )
}
