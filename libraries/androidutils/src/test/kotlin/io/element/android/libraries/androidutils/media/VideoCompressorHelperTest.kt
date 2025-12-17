/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.media

import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoCompressorHelperTest {
    @Test
    fun `test getOutputSize`() {
        val helper = VideoCompressorHelper(maxSize = 720)

        // Landscape input
        var inputSize = Size(1920, 1080)
        var outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(720, 405))

        // Landscape input small height
        inputSize = Size(1920, 200)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(720, 75))

        // Portrait input
        inputSize = Size(1080, 1920)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(405, 720))

        // Portrait input small width
        inputSize = Size(200, 1920)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(75, 720))

        // Square input
        inputSize = Size(1000, 1000)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(720, 720))

        // Square input same size
        inputSize = Size(720, 720)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(720, 720))

        // Square input no downscaling
        inputSize = Size(240, 240)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(240, 240))

        // Small input landscape (no downscaling)
        inputSize = Size(640, 480)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(640, 480))

        // Small input portrait (no downscaling)
        inputSize = Size(480, 640)
        outputSize = helper.getOutputSize(inputSize)
        assertThat(outputSize).isEqualTo(Size(480, 640))
    }

    @Test
    fun `test calculateOptimalBitrate`() {
        val helper = VideoCompressorHelper(maxSize = 720)
        val inputSize = Size(1920, 1080)
        var bitrate = helper.calculateOptimalBitrate(inputSize, frameRate = 30)
        // Output size will be 720x405, so bitrate = 720*405*0.1*30 = 874800
        assertThat(bitrate).isEqualTo(874_800L)
        // Half frame rate, half bitrate
        bitrate = helper.calculateOptimalBitrate(inputSize, frameRate = 15)
        assertThat(bitrate).isEqualTo(437_400L)
    }
}
