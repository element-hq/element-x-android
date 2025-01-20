/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.audio

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.media.aWaveForm
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo

open class MediaInfoAudioProvider : PreviewParameterProvider<MediaInfo> {
    override val values: Sequence<MediaInfo>
        get() = sequenceOf(
            anAudioMediaInfo(),
            anAudioMediaInfo(
                waveForm = aWaveForm(),
            ),
        )
}
