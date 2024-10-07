/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.file

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.hash.md5
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import java.io.File
import java.util.UUID
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultVoiceFileManager @Inject constructor(
    @CacheDirectory private val cacheDir: File,
    private val config: VoiceFileConfig,
    room: MatrixRoom,
) : VoiceFileManager {
    private val roomId: RoomId = room.roomId

    override fun createFile(): File {
        val fileName = "${UUID.randomUUID()}.${config.fileExt}"
        val outputDirectory = File(cacheDir, config.cacheSubdir)
        val roomDir = File(outputDirectory, roomId.value.md5())
            .apply(File::mkdirs)
        return File(roomDir, fileName)
    }

    override fun deleteFile(file: File) {
        file.delete()
    }
}
