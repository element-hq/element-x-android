/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.network.useragent

import android.os.Build
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.SdkMetadata
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultUserAgentProvider @Inject constructor(
    private val buildMeta: BuildMeta,
    private val sdkMeta: SdkMetadata,
) : UserAgentProvider {
    private val userAgent: String by lazy { buildUserAgent() }

    override fun provide(): String = userAgent

    /**
     * Create an user agent with the application version.
     * Ex: Element X/1.5.0 (Xiaomi Mi 9T; Android 11; RKQ1.200826.002; Sdk c344b155c)
     */
    private fun buildUserAgent(): String {
        val appName = buildMeta.applicationName
        val appVersion = buildMeta.versionName
        val deviceManufacturer = Build.MANUFACTURER
        val deviceModel = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val deviceBuildId = Build.DISPLAY
        val matrixSdkVersion = sdkMeta.sdkGitSha

        return buildString {
            append(appName)
            append("/")
            append(appVersion)
            append(" (")
            append(deviceManufacturer)
            append(" ")
            append(deviceModel)
            append("; ")
            append("Android ")
            append(androidVersion)
            append("; ")
            append(deviceBuildId)
            append("; ")
            append("Sdk ")
            append(matrixSdkVersion)
            append(")")
        }
    }
}
