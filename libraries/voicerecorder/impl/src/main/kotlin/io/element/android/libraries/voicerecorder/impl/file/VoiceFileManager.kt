/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.file

import java.io.File

interface VoiceFileManager {
    fun createFile(): File

    fun deleteFile(file: File)
}
