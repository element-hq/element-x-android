/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
