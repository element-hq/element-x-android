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

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.AudioDetails
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import org.matrix.rustcomponents.sdk.UnstableAudioDetailsContent as RustAudioDetails

fun RustAudioDetails.map(): AudioDetails = AudioDetails(
    duration = duration.toKotlinDuration(),
    waveform = waveform.fromMSC3246range().toImmutableList(),
)

fun AudioDetails.map(): RustAudioDetails = RustAudioDetails(
    duration = duration.toJavaDuration(),
    waveform = waveform.toMSC3246range()
)

/**
 * Resizes the given [0;1024] int list as per unstable MSC3246 spec
 * to a [0;1] float list to be used for waveform rendering.
 *
 * https://github.com/matrix-org/matrix-spec-proposals/blob/travis/msc/audio-waveform/proposals/3246-audio-waveform.md
 */
internal fun List<UShort>.fromMSC3246range(): List<Float> = map { it.toInt() / 1024f }

/**
 * Resizes the given [0;1] float list as per unstable MSC3246 spec
 * to a [0;1024] int list to be used for waveform rendering.
 *
 * https://github.com/matrix-org/matrix-spec-proposals/blob/travis/msc/audio-waveform/proposals/3246-audio-waveform.md
 */
internal fun List<Float>.toMSC3246range(): List<UShort> = map { (it * 1024).toInt().toUShort() }
