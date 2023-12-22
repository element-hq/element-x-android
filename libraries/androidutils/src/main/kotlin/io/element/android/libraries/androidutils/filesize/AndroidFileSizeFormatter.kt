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

package io.element.android.libraries.androidutils.filesize

import android.content.Context
import android.os.Build
import android.text.format.Formatter
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidFileSizeFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sdkIntProvider: BuildVersionSdkIntProvider,
) : FileSizeFormatter {
    override fun format(fileSize: Long, useShortFormat: Boolean): String {
        // Since Android O, the system considers that 1kB = 1000 bytes instead of 1024 bytes.
        // We want to avoid that.
        val normalizedSize = if (sdkIntProvider.get() <= Build.VERSION_CODES.N) {
            fileSize
        } else {
            // First convert the size
            when {
                fileSize < 1024 -> fileSize
                fileSize < 1024 * 1024 -> fileSize * 1000 / 1024
                fileSize < 1024 * 1024 * 1024 -> fileSize * 1000 / 1024 * 1000 / 1024
                else -> fileSize * 1000 / 1024 * 1000 / 1024 * 1000 / 1024
            }
        }

        return if (useShortFormat) {
            Formatter.formatShortFileSize(context, normalizedSize)
        } else {
            Formatter.formatFileSize(context, normalizedSize)
        }
    }
}
