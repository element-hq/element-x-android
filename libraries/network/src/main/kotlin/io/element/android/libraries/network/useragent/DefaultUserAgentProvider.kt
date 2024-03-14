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
