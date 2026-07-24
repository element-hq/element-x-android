/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.attachments.preview.SendActionState
import io.element.android.features.messages.impl.attachments.preview.aMediaUploadInfo
import org.junit.Test

class SendActionStateTest {
    @Test
    fun `mediaUploadInfoList() should return the value from Uploading class`() {
        val data = listOf(aMediaUploadInfo())
        val state: SendActionState = SendActionState.Sending.Uploading(mediaInfos = data)
        assertThat(state.mediaUploadInfoList()).isEqualTo(data)
    }

    @Test
    fun `mediaUploadInfoList() should return the value from ReadyToUpload class`() {
        val data = listOf(aMediaUploadInfo())
        val state: SendActionState = SendActionState.Sending.ReadyToUpload(mediaInfos = data)
        assertThat(state.mediaUploadInfoList()).isEqualTo(data)
    }

    @Test
    fun `mediaUploadInfoList() should return the value from Failure class`() {
        val data = listOf(aMediaUploadInfo())
        val state: SendActionState = SendActionState.Failure(error = IllegalStateException("An error"), mediaInfos = data)
        assertThat(state.mediaUploadInfoList()).isEqualTo(data)
    }
}
