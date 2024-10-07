/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class AudioDetails(
    val duration: Duration,
    val waveform: ImmutableList<Float>,
)
