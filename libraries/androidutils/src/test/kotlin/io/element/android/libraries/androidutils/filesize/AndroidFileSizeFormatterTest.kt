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

import android.os.Build
import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidFileSizeFormatterTest {
    @Test
    fun `test api 24 long format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.N)
        assertThat(sut.format(1, useShortFormat = false)).isEqualTo("1.00B")
        assertThat(sut.format(1000, useShortFormat = false)).isEqualTo("0.98KB")
        assertThat(sut.format(1024, useShortFormat = false)).isEqualTo("1.00KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = false)).isEqualTo("1.00MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = false)).isEqualTo("1.00GB")
    }

    @Test
    fun `test api 26 long format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.O)
        assertThat(sut.format(1, useShortFormat = false)).isEqualTo("1.00B")
        assertThat(sut.format(1000, useShortFormat = false)).isEqualTo("0.98KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = false)).isEqualTo("0.95MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = false)).isEqualTo("0.93GB")
    }

    @Test
    fun `test api 24 short format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.N)
        assertThat(sut.format(1, useShortFormat = true)).isEqualTo("1.0B")
        assertThat(sut.format(1000, useShortFormat = true)).isEqualTo("0.98KB")
        assertThat(sut.format(1024, useShortFormat = true)).isEqualTo("1.0KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = true)).isEqualTo("1.0MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = true)).isEqualTo("1.0GB")
    }

    @Test
    fun `test api 26 short format`() {
        val sut = createAndroidFileSizeFormatter(sdkLevel = Build.VERSION_CODES.O)
        assertThat(sut.format(1, useShortFormat = true)).isEqualTo("1.0B")
        assertThat(sut.format(1000, useShortFormat = true)).isEqualTo("0.98KB")
        assertThat(sut.format(1024 * 1024, useShortFormat = true)).isEqualTo("0.95MB")
        assertThat(sut.format(1024 * 1024 * 1024, useShortFormat = true)).isEqualTo("0.93GB")
    }

    private fun createAndroidFileSizeFormatter(sdkLevel: Int) = AndroidFileSizeFormatter(
        context = RuntimeEnvironment.getApplication(),
        sdkIntProvider = FakeBuildVersionSdkIntProvider(sdkInt = sdkLevel)
    )
}
