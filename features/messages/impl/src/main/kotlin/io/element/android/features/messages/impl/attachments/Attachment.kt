/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import kotlinx.parcelize.Parcelize

@Immutable
sealed interface Attachment : Parcelable {
    @Parcelize
    data class Media(
        val localMedia: LocalMedia,
        // When true, the media was picked through the "Files" picker and should be
        // uploaded without image recompression; videos still use the highest available
        // / best-fit preset rather than an additional size-reduction optimization pass.
        // See https://github.com/element-hq/element-x-android/issues/6365
        val sendAsFile: Boolean = false,
    ) : Attachment
}
