/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface Attachment : Parcelable {
    @Parcelize
    data class Media(val localMedia: LocalMedia, val compressIfPossible: Boolean) : Attachment
}
