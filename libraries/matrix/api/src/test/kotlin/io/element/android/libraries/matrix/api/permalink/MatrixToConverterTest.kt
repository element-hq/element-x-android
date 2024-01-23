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

package io.element.android.libraries.matrix.api.permalink

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MatrixToConverterTest {
    @Test
    fun `converting a matrix-to url does nothing`() {
        val url = Uri.parse("https://matrix.to/#/#element-android:matrix.org")
        assertThat(MatrixToConverter.convert(url)).isEqualTo(url)
    }

    @Test
    fun `converting a url with a supported room path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/room/#element-android:matrix.org")
        assertThat(MatrixToConverter.convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/#element-android:matrix.org"))
    }

    @Test
    fun `converting a url with a supported user path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/user/@test:matrix.org")
        assertThat(MatrixToConverter.convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/@test:matrix.org"))
    }

    @Test
    fun `converting a url with a supported group path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/group/+group:matrix.org")
        assertThat(MatrixToConverter.convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/+group:matrix.org"))
    }

    @Test
    fun `converting an unsupported url returns null`() {
        val url = Uri.parse("https://element.io/")
        assertThat(MatrixToConverter.convert(url)).isNull()
    }
}
