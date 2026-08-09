/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import org.junit.Test

class MediaWithProgressFractionTest {
    @Test
    fun `fraction is the ratio of the progress to the total`() {
        val sendState = LocalEventSendState.Sending.MediaWithProgress(index = 0, progress = 45, total = 100)
        assertThat(sendState.fraction()).isEqualTo(0.45f)
    }

    @Test
    fun `fraction is zero when nothing has been uploaded yet`() {
        val sendState = LocalEventSendState.Sending.MediaWithProgress(index = 0, progress = 0, total = 100)
        assertThat(sendState.fraction()).isEqualTo(0f)
    }

    @Test
    fun `fraction is zero when the total is unknown`() {
        val sendState = LocalEventSendState.Sending.MediaWithProgress(index = 0, progress = 45, total = 0)
        assertThat(sendState.fraction()).isEqualTo(0f)
    }

    @Test
    fun `fraction never exceeds one`() {
        val sendState = LocalEventSendState.Sending.MediaWithProgress(index = 0, progress = 150, total = 100)
        assertThat(sendState.fraction()).isEqualTo(1f)
    }
}
