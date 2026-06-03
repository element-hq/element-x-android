/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.media.aMediaSource
import org.junit.Test

class MediaSourceTest {
    @Test
    fun `safeUrl removes the fragment part in MXC urls`() {
        val mediaSource = aMediaSource(url = "mxc://matrix.org/url#fragment")
        assertThat(mediaSource.safeUrl).isEqualTo("mxc://matrix.org/url")
    }

    @Test
    fun `safeUrl keeps the fragment part in a non-MXC url`() {
        val mediaSource = aMediaSource(url = "https://matrix.org/url#fragment")
        assertThat(mediaSource.safeUrl).isEqualTo("https://matrix.org/url#fragment")
    }
}
