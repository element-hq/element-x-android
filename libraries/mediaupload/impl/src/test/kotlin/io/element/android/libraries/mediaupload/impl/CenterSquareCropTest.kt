/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.LanczosResample
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

@OptIn(UnstableApi::class)
class CenterSquareCropTest : RobolectricTest() {
    @Test
    fun `landscape frame is cropped to the short side`() {
        val output = CenterSquareCrop().configure(1920, 1080)

        assertThat(output.width).isEqualTo(1080)
        assertThat(output.height).isEqualTo(1080)
        assertThat(CenterSquareCrop().isNoOp(1920, 1080)).isFalse()
    }

    @Test
    fun `portrait frame is cropped to the short side`() {
        val output = CenterSquareCrop().configure(1080, 1920)

        assertThat(output.width).isEqualTo(1080)
        assertThat(output.height).isEqualTo(1080)
    }

    @Test
    fun `square frame is a no-op`() {
        val output = CenterSquareCrop().configure(720, 720)

        assertThat(output.width).isEqualTo(720)
        assertThat(output.height).isEqualTo(720)
        assertThat(CenterSquareCrop().isNoOp(720, 720)).isTrue()
    }

    @Test
    fun `lanczos is used only when the cropped side is larger than the cap`() {
        val downscale = squareCropEffects(maxSide = 512, croppedSide = 1080)
        val keepNative = squareCropEffects(maxSide = 512, croppedSide = 480)

        assertThat(downscale[0]).isInstanceOf(CenterSquareCrop::class.java)
        assertThat(downscale[1]).isInstanceOf(LanczosResample::class.java)
        assertThat(keepNative).hasSize(1)
        assertThat(keepNative[0]).isInstanceOf(CenterSquareCrop::class.java)
    }
}
