/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.permalink

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultMatrixToConverterTest {
    @Test
    fun `converting a matrix-to url does nothing`() {
        val url = Uri.parse("https://matrix.to/#/#element-android:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(url)
    }

    @Test
    fun `converting a url with a supported room path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/room/#element-android:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/#element-android:matrix.org"))
    }

    @Test
    fun `converting a url with a supported user path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/user/@test:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/@test:matrix.org"))
    }

    @Test
    fun `converting a url with a supported group path returns a matrix-to url`() {
        val url = Uri.parse("https://riot.im/develop/#/group/+group:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/+group:matrix.org"))
    }

    @Test
    fun `converting an unsupported url returns null`() {
        val url = Uri.parse("https://element.io/")
        assertThat(DefaultMatrixToConverter().convert(url)).isNull()
    }

    @Test
    fun `converting url coming from the matrix-to website returns a matrix-to url for room case`() {
        val url = Uri.parse("element://room/#element-android:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/#element-android:matrix.org"))
    }

    @Test
    fun `converting url coming from the matrix-to website returns a matrix-to url for user case`() {
        val url = Uri.parse("element://user/@alice:matrix.org")
        assertThat(DefaultMatrixToConverter().convert(url)).isEqualTo(Uri.parse("https://matrix.to/#/@alice:matrix.org"))
    }
}
