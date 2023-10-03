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

package io.element.android.features.messages.impl.voicemessages.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.linc.audiowaveform.AudioWaveform
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
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
    var seekProgress: Float? by remember { mutableStateOf(null) }
    val scaledAmplitudes = remember(amplitudes) { amplitudes.scaleAmplitudes() }
    AudioWaveform(
        modifier = modifier,
        waveformBrush = SolidColor(ElementTheme.colors.iconQuaternary),
        progressBrush = SolidColor(ElementTheme.colors.iconSecondary),
        onProgressChangeFinished = {
            // This is to send just one onSeek callback after the user has finished seeking.
            // Otherwise the AudioWaveform library would send multiple callbacks while the user is seeking.
            val p = seekProgress!!
            seekProgress = null
            onSeek(p)
        },
        spikeWidth = 1.6.dp,
        spikeRadius = 0.8.dp,
        spikePadding = 3.dp,
        progress = seekProgress ?: progress,
        amplitudes = scaledAmplitudes,
        onProgressChange = { seekProgress = it },
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

/**
 * Scale amplitudes to fit in the waveform view.
 *
 * It seems amplitudes > 128 are clipped by the waveform library.
 * Workaround for https://github.com/lincollincol/compose-audiowaveform/issues/22
 *
 * TODO Voice messages: Remove this workaround when the waveform library is fixed.
 */
private fun ImmutableList<Int>.scaleAmplitudes(): List<Int> {
    val maxAmplitude = if (isEmpty()) 1 else maxOf { it }
    val scalingFactor = 128 / maxAmplitude.toFloat()
    return map { (it * scalingFactor).toInt() }
}
