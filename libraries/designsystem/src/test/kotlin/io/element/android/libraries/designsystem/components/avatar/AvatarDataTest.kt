/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AvatarDataTest {
    @Test
    fun `initial with text should get the first char, uppercased`() {
        val data = AvatarData("id", "test", null, AvatarSize.InviteSender)
        assertThat(data.initialLetter).isEqualTo("T")
    }

    @Test
    fun `initial with leading whitespace should get the first non-whitespace char, uppercased`() {
        val data = AvatarData("id", " test", null, AvatarSize.InviteSender)
        assertThat(data.initialLetter).isEqualTo("T")
    }

    @Test
    fun `initial with long emoji should get the full emoji`() {
        val data = AvatarData("id", "\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08 Test", null, AvatarSize.InviteSender)
        assertThat(data.initialLetter).isEqualTo("\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08")
    }

    @Test
    fun `initial with short emoji should get the emoji`() {
        val data = AvatarData("id", "✂ Test", null, AvatarSize.InviteSender)
        assertThat(data.initialLetter).isEqualTo("✂")
    }

    @Test
    fun `initial with a single letter should take that letter`() {
        val data = AvatarData("id", "T", null, AvatarSize.InviteSender)
        assertThat(data.initialLetter).isEqualTo("T")
    }
}
