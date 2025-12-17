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
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import org.junit.Test

class SendActionStateTest {
    @Test
    fun `mediaUploadInfo() should return the value from Uploading class`() {
        val mediaUploadInfo: MediaUploadInfo = aMediaUploadInfo()
        val state: SendActionState = SendActionState.Sending.Uploading(mediaUploadInfo = aMediaUploadInfo())
        assertThat(state.mediaUploadInfo()).isEqualTo(mediaUploadInfo)
    }

    @Test
    fun `mediaUploadInfo() should return the value from ReadyToUpload class`() {
        val mediaUploadInfo: MediaUploadInfo = aMediaUploadInfo()
        val state: SendActionState = SendActionState.Sending.ReadyToUpload(mediaInfo = aMediaUploadInfo())
        assertThat(state.mediaUploadInfo()).isEqualTo(mediaUploadInfo)
    }

    @Test
    fun `mediaUploadInfo() should return the value from Failure class`() {
        val mediaUploadInfo: MediaUploadInfo = aMediaUploadInfo()
        val state: SendActionState = SendActionState.Failure(error = IllegalStateException("An error"), mediaUploadInfo = aMediaUploadInfo())
        assertThat(state.mediaUploadInfo()).isEqualTo(mediaUploadInfo)
    }
}
