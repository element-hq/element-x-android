/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.filesize

import android.os.Build
import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidFileSizeFormatterTest {
    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `test api 24 long format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.N)
        assertThat(sut.format(1, useShortFormat = false)).isEqualTo("1 B")
        assertThat(sut.format(1000, useShortFormat = false)).isEqualTo("0.98 KB")
        assertThat(sut.format(1024, useShortFormat = false)).isEqualTo("1.00 KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = false)).isEqualTo("1.00 MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = false)).isEqualTo("1.00 GB")
    }

    @Config(sdk = [Build.VERSION_CODES.O])
    @Test
    fun `test api 26 long format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.O)
        assertThat(sut.format(1, useShortFormat = false)).isEqualTo("1 B")
        assertThat(sut.format(1000, useShortFormat = false)).isEqualTo("0.98 KB")
        assertThat(sut.format(1024, useShortFormat = false)).isEqualTo("1.00 KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = false)).isEqualTo("1.00 MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = false)).isEqualTo("1.00 GB")
    }

    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `test api 24 short format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.N)
        assertThat(sut.format(1, useShortFormat = true)).isEqualTo("1 B")
        assertThat(sut.format(1000, useShortFormat = true)).isEqualTo("0.98 KB")
        assertThat(sut.format(1024, useShortFormat = true)).isEqualTo("1.0 KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = true)).isEqualTo("1.0 MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = true)).isEqualTo("1.0 GB")
    }

    @Config(sdk = [Build.VERSION_CODES.O])
    @Test
    fun `test api 26 short format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.O)
        assertThat(sut.format(1, useShortFormat = true)).isEqualTo("1 B")
        assertThat(sut.format(1000, useShortFormat = true)).isEqualTo("0.98 KB")
        assertThat(sut.format(1024, useShortFormat = true)).isEqualTo("1.0 KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = true)).isEqualTo("1.0 MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = true)).isEqualTo("1.0 GB")
    }

    private fun createAndroidFileSizeFormatter(sdkLevel: Int) = AndroidFileSizeFormatter(
        context = RuntimeEnvironment.getApplication(),
        sdkIntProvider = FakeBuildVersionSdkIntProvider(sdkInt = sdkLevel)
    )
}
