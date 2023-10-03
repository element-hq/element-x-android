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

package io.element.android.features.messages.impl.timeline.voice

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.linc.audiowaveform.AudioWaveform
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.progressIndicatorTrackColor
import io.element.android.libraries.theme.ElementTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WaveformProgressIndicator(
    progress: Float,
    amplitudes: ImmutableList<Int>,
    modifier: Modifier = Modifier,
    onSeek: (progress: Float) -> Unit = {},
) {
    AudioWaveform(
        modifier = modifier,
        waveformBrush = SolidColor(ElementTheme.colors.progressIndicatorTrackColor),
        progressBrush = SolidColor(ElementTheme.colors.textSuccessPrimary),
        spikeWidth = 1.6.dp,
        spikeRadius = 0.8.dp,
        spikePadding = 3.dp,
        progress = progress,
        amplitudes = amplitudes,
        onProgressChange = onSeek,
    )
}

@PreviewsDayNight
@Composable
internal fun WaveformProgressIndicatorPreview() = ElementPreview {
    WaveformProgressIndicator(
        progress = 0.5f,
        amplitudes = persistentListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
    )
}
