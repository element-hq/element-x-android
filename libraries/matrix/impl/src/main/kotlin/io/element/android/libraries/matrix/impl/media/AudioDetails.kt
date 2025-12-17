/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
