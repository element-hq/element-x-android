/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import java.io.File

interface Encoder {
    fun init(file: File)

    fun encode(buffer: ShortArray, readSize: Int)

    fun release()
}
