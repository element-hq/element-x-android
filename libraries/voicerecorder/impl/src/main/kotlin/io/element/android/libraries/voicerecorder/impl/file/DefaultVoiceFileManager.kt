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
