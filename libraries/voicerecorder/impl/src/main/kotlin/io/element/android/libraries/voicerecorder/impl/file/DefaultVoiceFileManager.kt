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

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.hash.md5
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.RoomScope
import java.io.File
import java.util.UUID
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultVoiceFileManager @Inject constructor(
    @ApplicationContext context: Context,
    private val config: VoiceFileConfig,
) : VoiceFileManager {
    private val outputDirectory: File by lazy { ensureCacheDirectory(context) }
    override fun createFile(groupId: String): File {
        val fileName = "${UUID.randomUUID()}.${config.fileExt}"
        val groupDir = File(outputDirectory, groupId.md5()).apply {
            mkdirs()
        }
        return File(groupDir, fileName)
    }

    override fun deleteFile(file: File) {
        file.delete()
    }

    private fun ensureCacheDirectory(
        context: Context
    ): File = File(context.cacheDir, config.cacheSubdir)
        .apply(File::mkdirs)
}
