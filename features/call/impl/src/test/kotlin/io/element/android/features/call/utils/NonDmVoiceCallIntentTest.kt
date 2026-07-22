/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.utils.withNonDmVoiceCallIntent
import org.junit.Test

class NonDmVoiceCallIntentTest {
    @Test
    fun `rewrites start_call to start_call_voice`() {
        val url = "https://call.example/#/?intent=start_call&roomId=!a:b"
        assertThat(url.withNonDmVoiceCallIntent())
            .isEqualTo("https://call.example/#/?intent=start_call_voice&roomId=!a:b")
    }

    @Test
    fun `rewrites join_existing to join_existing_voice`() {
        val url = "https://call.example/#/?intent=join_existing&roomId=!a:b"
        assertThat(url.withNonDmVoiceCallIntent())
            .isEqualTo("https://call.example/#/?intent=join_existing_voice&roomId=!a:b")
    }

    @Test
    fun `does not rewrite DM intents`() {
        val startDm = "https://call.example/#/?intent=start_call_dm&roomId=!a:b"
        val joinDm = "https://call.example/#/?intent=join_existing_dm&roomId=!a:b"
        assertThat(startDm.withNonDmVoiceCallIntent()).isEqualTo(startDm)
        assertThat(joinDm.withNonDmVoiceCallIntent()).isEqualTo(joinDm)
    }

    @Test
    fun `does not rewrite intents that are already voice`() {
        val url = "https://call.example/#/?intent=start_call_voice&roomId=!a:b"
        assertThat(url.withNonDmVoiceCallIntent()).isEqualTo(url)
    }
}
