/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.ui.text.AnnotatedString
import io.element.android.libraries.matrix.api.core.EventId

data class PinnedMessagesBannerItem(
    val eventId: EventId,
    val formatted: AnnotatedString,
)
