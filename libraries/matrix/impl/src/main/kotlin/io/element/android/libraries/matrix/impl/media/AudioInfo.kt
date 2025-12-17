/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.AudioInfo
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import org.matrix.rustcomponents.sdk.AudioInfo as RustAudioInfo

fun RustAudioInfo.map(): AudioInfo = AudioInfo(
    duration = duration?.toKotlinDuration(),
    size = size?.toLong(),
    mimetype = mimetype
)

fun AudioInfo.map(): RustAudioInfo = RustAudioInfo(
    duration = duration?.toJavaDuration(),
    size = size?.toULong(),
    mimetype = mimetype,
)
