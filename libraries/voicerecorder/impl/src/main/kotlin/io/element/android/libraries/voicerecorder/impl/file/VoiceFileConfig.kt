/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.file

/**
 * File configuration for voice recording.
 *
 * @property cacheSubdir the subdirectory in the cache dir to use.
 * @property fileExt the file extension for audio files.
 * @property mimeType the mime type of audio files.
 */
data class VoiceFileConfig(
    val cacheSubdir: String,
    val fileExt: String,
    val mimeType: String,
)
